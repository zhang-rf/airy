package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.resolver.Resolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderedSerializer extends ReferencedSerializer implements StructedSerializer, Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return false;
    }

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            if (serializingDepth().getAndIncrement() == 0) {
                getRegistry().writeClass(buffer, writeClass ? object.getClass() : null);
                if (!getResolverChain().writeObject(buffer, object, object.getClass()))
                    writeObject(buffer, object);
            } else
                writeObject(buffer, object, object.getClass());
        } finally {
            if (serializingDepth().decrementAndGet() == 0)
                objectMap().clear();
        }
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Class<?> type = object.getClass();
        if (reference == Object.class)
            getRegistry().writeClass(buffer, type);
        if (!getResolverChain().writeObject(buffer, object, reference != Object.class ? reference : object.getClass(), generics)) {
            if (reference != Object.class)
                getRegistry().writeClass(buffer, type != reference ? type : null);
            writeObject(buffer, object);
        }
        return true;
    }

    private void writeObject(NioBuffer buffer, Object object) {
        try {
            Map<Object, Integer> objectMap = objectMap();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();
            Class<?> superType = object.getClass();
            List<Integer> addressList = null;
            do {
                Field[] fields = superType.getDeclaredFields();
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
                        writeObject(buffer, value, referenceType, getGenericTypes(field.getGenericType()));
                    }
                }
            } while ((superType = superType.getSuperclass()) != Object.class);

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
            if (deserializingDepth().getAndIncrement() == 0) {
                type = (Class<T>) getRegistry().readClass(buffer, type);
                if (type == null)
                    throw new UnknownClassException();
                Object instance = getResolverChain().readObject(buffer, type);
                if (instance == null)
                    instance = readObject(buffer, type);
                return (T) instance;
            } else
                return (T) readObject(buffer, type, new Type[0]);
        } finally {
            if (deserializingDepth().decrementAndGet() == 0)
                addressMap().clear();
        }
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        boolean classRead = false;
        if (reference == Object.class) {
            reference = getRegistry().readClass(buffer, null);
            classRead = true;
        }
        if (reference == null)
            throw new UnknownClassException();
        Object instance = getResolverChain().readObject(buffer, reference, generics);
        if (instance == null) {
            if (!classRead)
                reference = getRegistry().readClass(buffer, reference);
            instance = readObject(buffer, reference);
        }
        return instance;
    }

    private Object readObject(NioBuffer buffer, Class<?> type) {
        try {
            Map<Integer, Object> addressMap = addressMap();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);

            Constructor<?> defaultConstructor = type.getDeclaredConstructor();
            if (!defaultConstructor.isAccessible())
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
                    Object value = readObject(buffer, field.getType(), getGenericTypes(field.getGenericType()));
                    field.set(instance, value);
                    addressMap.put(address, value);
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

    @SuppressWarnings("Duplicates")
    @Override
    public Map<String, Integer> getStructMap(NioBuffer buffer, Class<?> type) {
        int headerAddress = buffer.asByteBuffer().getInt();
        int baseAddress = buffer.position();
        buffer.position(headerAddress);

        List<Field> fieldList = null;
        do {
            Field[] fields = type.getDeclaredFields();
            if (fieldList == null)
                fieldList = new ArrayList<>(Math.max(fields.length, 10));

            for (Field field : type.getDeclaredFields())
                if (isFieldSerializable(field))
                    fieldList.add(field);
        } while ((type = type.getSuperclass()) != Object.class);

        Map<String, Integer> structHeaderMap = new HashMap<>(fieldList.size());
        for (Field field : fieldList) {
            int offset = (int) buffer.getUnsignedVarint() - 1;
            if (offset >= 0) {
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                structHeaderMap.put(field.getName(), address);
            }
        }
        return structHeaderMap;
    }
}
