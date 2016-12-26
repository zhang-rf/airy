package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static me.rfprojects.airy.internal.ClassUtil.readClass;
import static me.rfprojects.airy.internal.ClassUtil.writeClassName;
import static me.rfprojects.airy.internal.Misc.*;
import static me.rfprojects.airy.internal.Null.isNull;

public class ConstClassSerializer extends AbstractSerializer {

    private static final Object PRESENT = new Object();
    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<>(ThreadLocalReference.ReferenceType.SoftReference, IdentityHashMap.class);
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<>(ThreadLocalReference.ReferenceType.SoftReference, IdentityHashMap.class);

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClassName) {
        objectMapReference.get().clear();
        writeClassName(buffer, object.getClass(), getRegistry(), writeClassName);
        serialize0(buffer, object, null);
    }

    private void serialize0(NioBuffer buffer, Object object, Class<?> referenceType, Type... genericTypes) {
        if (referenceType != null && !isPrimitive(referenceType, getRegistry())) {
            Class<?> objectClass = object.getClass();
            writeClassName(buffer, objectClass, getRegistry(), referenceType != objectClass);
        }
        if (!getResolverChain().writeObject(buffer, object, referenceType, genericTypes))
            writeObject(buffer, object);
    }

    private void writeObject(NioBuffer buffer, Object object) {
        try {
            Map<Object, Integer> objectMap = objectMapReference.get();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            Class<?> objectClass = object.getClass();
            List<Integer> addressList = null;
            do {
                Field[] fields = objectClass.getDeclaredFields();
                if (addressList == null)
                    addressList = new ArrayList<>(Math.max(fields.length, 10));
                for (Field field : fields) {
                    addressList.add(0);
                    if (!isFieldSerializable(field))
                        continue;

                    if (!field.isAccessible())
                        field.setAccessible(true);
                    Class<?> referenceType = field.getType();
                    Object value = field.get(object);
                    if (isNull(value, referenceType))
                        continue;

                    if (objectMap.containsKey(value))
                        addressList.set(addressList.size() - 1, -objectMap.get(value));
                    else {
                        int address = buffer.position();
                        objectMap.put(value, address);
                        addressList.set(addressList.size() - 1, address);
                        serialize0(buffer, value, referenceType, getGenericTypes(field.getGenericType()));
                    }
                }
            } while ((objectClass = objectClass.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);

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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> clazz) {
        addressMapReference.get().clear();
        if (isPrimitive(clazz, getRegistry()))
            clazz = (Class<T>) readClass(buffer, clazz, getRegistry());
        return (T) deserialize0(buffer, clazz);
    }

    private Object deserialize0(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        if (!isPrimitive(referenceType, getRegistry()))
            if ((referenceType = readClass(buffer, referenceType, getRegistry())) == null)
                throw new UnknownClassException();
        Object instance = getResolverChain().readObject(buffer, referenceType, genericTypes);
        if (instance == null)
            instance = readObject(buffer, referenceType);
        return instance;
    }

    private Object readObject(NioBuffer buffer, Class<?> objectClass) {
        try {
            Map<Integer, Object> addressMap = addressMapReference.get();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);

            Constructor<?> defaultConstructor = objectClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            List<Field> fieldList = null;
            do {
                Field[] fields = objectClass.getDeclaredFields();
                if (fieldList == null)
                    fieldList = new ArrayList<>(Math.max(fields.length, 10));
                for (Field field : objectClass.getDeclaredFields())
                    if (isFieldSerializable(field))
                        fieldList.add(field);
            } while ((objectClass = objectClass.getSuperclass()) != Object.class);

            for (Field field : fieldList) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                if (offset < 0)
                    continue;

                if (!field.isAccessible())
                    field.setAccessible(true);
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                if (addressMap.containsKey(address)) {
                    Object value = addressMap.get(address);
                    field.set(instance, value == PRESENT ? instance : value);
                } else {
                    buffer.mark().position(address);
                    Object value = deserialize0(buffer, field.getType(), getGenericTypes(field.getGenericType()));
                    addressMap.put(address, value);
                    field.set(instance, value);
                    buffer.reset().unmark();
                }
            }
            return instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
