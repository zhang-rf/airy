package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static me.rfprojects.airy.internal.ClassUtil.readClass;
import static me.rfprojects.airy.internal.ClassUtil.writeClassName;
import static me.rfprojects.airy.internal.Misc.getGenericTypes;
import static me.rfprojects.airy.internal.Misc.isFieldSerializable;
import static me.rfprojects.airy.internal.Null.isNull;

public class ConstClassSerializer extends AbstractSerializer {

    private static final Object PRESENT = new Object();
    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<>(ThreadLocalReference.ReferenceType.SoftReference, IdentityHashMap.class);
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<>(ThreadLocalReference.ReferenceType.SoftReference, IdentityHashMap.class);

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClassName) {
        objectMapReference.get().clear();
        super.serialize(buffer, object, writeClassName);
    }

    @Override
    protected void serialize0(NioBuffer buffer, Object object, boolean writeClassName) {
        try {
            Map<Object, Integer> objectMap = objectMapReference.get();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            Class<?> clazz = object.getClass();
            List<Integer> addressList = new ArrayList<>();
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!isFieldSerializable(field)) {
                        addressList.add(0);
                        continue;
                    }

                    field.setAccessible(true);
                    Class<?> referenceType = field.getType();
                    Object value = field.get(object);
                    if (isNull(value, referenceType)) {
                        addressList.add(0);
                        continue;
                    }

                    if (objectMap.containsKey(value))
                        addressList.add(-objectMap.get(value));
                    else {
                        int address = buffer.position();
                        objectMap.put(value, address);
                        addressList.add(address);

                        Class<?> valueType = value.getClass();
                        if (referenceType == Object.class)
                            writeClassName(buffer, valueType, getRegistry(), true);

                        if (!getResolverChain().writeObject(buffer, value, referenceType, getGenericTypes(field.getGenericType())))
                            serialize0(buffer, value, valueType != referenceType);
                    }
                }
            } while ((clazz = clazz.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            writeClassName(buffer, object.getClass(), getRegistry(), writeClassName);

            buffer.putUnsignedVarint(addressList.size());
            for (int address : addressList) {
                if (address == 0)
                    buffer.putUnsignedVarint(0);
                else
                    buffer.putUnsignedVarint((address > 0 ? address - baseAddress : -address + headerAddress) + 1);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> clazz) {
        addressMapReference.get().clear();
        return super.deserialize(buffer, clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T deserialize0(NioBuffer buffer, Class<T> clazz) {
        try {
            Map<Integer, Object> addressMap = addressMapReference.get();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();

            buffer.position(headerAddress);
            if ((clazz = (Class<T>) readClass(buffer, clazz, getRegistry())) == null)
                throw new UnknownClassException();
            Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            int fieldSize = (int) buffer.getUnsignedVarint();
            List<Field> fieldList = new ArrayList<>(fieldSize);
            do {
                for (Field field : clazz.getDeclaredFields())
                    if (isFieldSerializable(field))
                        fieldList.add(field);
            } while ((clazz = (Class) clazz.getSuperclass()) != Object.class);

            for (Field field : fieldList) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                if (offset < 0)
                    continue;

                field.setAccessible(true);
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                if (addressMap.containsKey(address)) {
                    Object value = addressMap.get(address);
                    field.set(instance, value == PRESENT ? instance : value);
                } else {
                    buffer.mark().position(address);
                    Class<?> referenceType = field.getType();
                    if (referenceType == Object.class)
                        referenceType = readClass(buffer, null, getRegistry());

                    Object value = getResolverChain().readObject(buffer, referenceType, getGenericTypes(field.getGenericType()));
                    if (value == null)
                        value = deserialize0(buffer, referenceType);
                    field.set(instance, value);
                    addressMap.put(address, value);
                    buffer.reset().unmark();
                }
            }
            return (T) instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
