package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.util.ThreadLocalInteger;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static me.rfprojects.airy.internal.Misc.getGenericTypes;
import static me.rfprojects.airy.internal.Misc.isFieldSerializable;

public class ConstClassSerializer extends AbstractSerializer {

    private static final Object PRESENT = new Object();
    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalInteger serializingDepthLocal = new ThreadLocalInteger();
    private ThreadLocalInteger deserializingDepthLocal = new ThreadLocalInteger();

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            serializingDepthLocal.incrementAndGet();
            if (writeClass)
                getRegistry().writeClass(buffer, object.getClass());
            else if (serializingDepthLocal.get() == 1)
                getRegistry().writeClass(buffer, null);
            if (!getResolverChain().writeObject(buffer, object, null))
                writeObject(buffer, object, false);
        } finally {
            if (serializingDepthLocal.decrementAndGet() == 0)
                objectMapReference.get().clear();
        }
    }

    private void writeObject(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            Map<Object, Integer> objectMap = objectMapReference.get();
            objectMap.put(object, buffer.position());

            Class<?> type = object.getClass();
            if (writeClass)
                getRegistry().writeClass(buffer, type);
            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            List<Integer> addressList = null;
            do {
                Field[] fields = type.getDeclaredFields();
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
                    if (Null.isNull(value, referenceType))
                        continue;

                    if (objectMap.containsKey(value))
                        addressList.set(addressList.size() - 1, -objectMap.get(value));
                    else {
                        int address = buffer.position();
                        objectMap.put(value, address);
                        addressList.set(addressList.size() - 1, address);

                        Class<?> valueType = value.getClass();
                        if (valueType != referenceType && getRegistry().isPrimitive(valueType))
                            getRegistry().writeClass(buffer, valueType);
                        if (!getResolverChain().writeObject(buffer, object, referenceType, getGenericTypes(field.getGenericType())))
                            writeObject(buffer, object, valueType != referenceType);
                    }
                }
            } while ((type = type.getSuperclass()) != Object.class);

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
    public <T> T deserialize(NioBuffer buffer, Class<T> type) {
        try {
            deserializingDepthLocal.incrementAndGet();
            if (deserializingDepthLocal.get() == 1)
                type = (Class<T>) getRegistry().readClass(buffer, type);
            if (type == null)
                throw new UnknownClassException();
            Object instance = getResolverChain().readObject(buffer, type);
            if (instance == null)
                instance = readObject(buffer, type);
            return (T) instance;
        } finally {
            if (deserializingDepthLocal.decrementAndGet() == 0)
                addressMapReference.get().clear();
        }
    }

    private Object readObject(NioBuffer buffer, Class<?> type) {
        try {
            Map<Integer, Object> addressMap = addressMapReference.get();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);

            Constructor<?> defaultConstructor = type.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            List<Field> fieldList = null;
            do {
                Field[] fields = type.getDeclaredFields();
                if (fieldList == null)
                    fieldList = new ArrayList<>(Math.max(fields.length, 10));

                for (Field field : type.getDeclaredFields())
                    if (isFieldSerializable(field))
                        fieldList.add(field);
            } while ((type = type.getSuperclass()) != Object.class);

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
                    Class<?> referenceType = field.getType();
                    if (!getRegistry().isPrimitive(referenceType))
                        referenceType = getRegistry().readClass(buffer, referenceType);
                    Object value = getResolverChain().readObject(buffer, referenceType, getGenericTypes(field.getGenericType()));
                    if (instance == null)
                        instance = readObject(buffer, referenceType);
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
