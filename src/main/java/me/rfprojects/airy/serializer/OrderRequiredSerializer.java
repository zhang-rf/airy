package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.AiryException;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.internal.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRequiredSerializer extends ReferencedSerializer implements StructuredSerializer {

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            super.serialize(buffer, object, writeClass);
        } finally {
            objectMap().clear();
        }
    }

    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> type) {
        try {
            return super.deserialize(buffer, type);
        } finally {
            addressMap().clear();
        }
    }

    @Override
    protected void writeObject(NioBuffer buffer, Object object) {
        try {
            Map<Object, Integer> objectMap = objectMap();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            Class<?> nextClass = object.getClass();
            List<Integer> addressList = null;
            do {
                Field[] fields = nextClass.getDeclaredFields();
                if (addressList == null)
                    addressList = new ArrayList<>(Math.max(fields.length, 10));

                for (Field field : fields) {
                    addressList.add(0);
                    if (!ReflectionUtils.isFieldSerializable(field))
                        continue;

                    if (!field.isAccessible())
                        field.setAccessible(true);
                    Class<?> type = field.getType();
                    Object value = field.get(object);
                    if (value != Null.get(type)) {
                        if (objectMap.containsKey(value))
                            addressList.set(addressList.size() - 1, -objectMap.get(value));
                        else {
                            int address = buffer.position();
                            objectMap.put(value, address);
                            addressList.set(addressList.size() - 1, address);
                            handlerChain().write(buffer, value, type, ReflectionUtils.getTypeArguments(field.getGenericType()));
                        }
                    }
                }
            } while ((nextClass = nextClass.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            for (int address : addressList) {
                buffer.putUnsignedVarint(address == 0 ? 0
                        : ((address > 0 ? address - baseAddress : -address + headerAddress) + 1));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object readObject(NioBuffer buffer, Class<?> type) {
        try {
            Map<Integer, Object> addressMap = addressMap();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), this);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);

            Constructor<?> defaultConstructor = type.getDeclaredConstructor();
            if (!defaultConstructor.isAccessible())
                defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            List<Field> fieldList = getFieldList(type);
            for (Field field : fieldList) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                if (offset >= 0) {
                    if (!field.isAccessible())
                        field.setAccessible(true);

                    int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                    if (addressMap.containsKey(address)) {
                        Object value = addressMap.get(address);
                        field.set(instance, value == this ? instance : value);
                    } else {
                        buffer.mark().position(address);
                        Object value = handlerChain().read(buffer, field.getType(), ReflectionUtils.getTypeArguments(field.getGenericType()));
                        field.set(instance, value);
                        buffer.reset().unmark();
                        addressMap.put(address, value);
                    }
                }
            }
            return instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Field> getFieldList(Class<?> type) {
        List<Field> fieldList = null;
        do {
            Field[] fields = type.getDeclaredFields();
            if (fieldList == null)
                fieldList = new ArrayList<>(Math.max(fields.length, 10));

            for (Field field : type.getDeclaredFields())
                if (ReflectionUtils.isFieldSerializable(field))
                    fieldList.add(field);
        } while ((type = type.getSuperclass()) != Object.class);
        return fieldList;
    }

    @Override
    public RandomAccessor[] getAccessors(NioBuffer buffer, Class<?> type) {
        try {
            buffer.mark();
            if ((type = registry().readClass(buffer, type)) == null)
                throw new UnknownClassException();

            List<RandomAccessor> randomAccessorList = new ArrayList<>();
            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);
            List<Field> fieldList = getFieldList(type);
            for (Field field : fieldList) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                if (offset >= 0) {
                    int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                    randomAccessorList.add(new Accessor(address, field));
                }
            }
            return (RandomAccessor[]) randomAccessorList.toArray();
        } finally {
            buffer.reset().unmark();
        }
    }

    @Override
    public RandomAccessor getAccessor(NioBuffer buffer, Class<?> type, String name) {
        try {
            buffer.mark();
            if ((type = registry().readClass(buffer, type)) == null)
                throw new UnknownClassException();

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);
            List<Field> fieldList = getFieldList(type);
            for (Field field : fieldList) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                if (field.getName().equals(name)) {
                    if (offset < 0)
                        return new Accessor(-1, field);
                    else {
                        int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                        return new Accessor(address, field);
                    }
                }
            }
            throw new AiryException(new NoSuchFieldException());
        } finally {
            buffer.reset().unmark();
        }
    }

    private class Accessor implements RandomAccessor {

        private int address;
        private Field field;

        Accessor(int address, Field field) {
            this.address = address;
            this.field = field;
        }

        @Override
        public int getAddress() {
            return address;
        }

        @Override
        public Field getField() {
            return field;
        }

        @Override
        public Object accessValue(NioBuffer buffer) {
            if (address < 0)
                return null;
            return handlerChain().read(buffer.position(address), field.getType(),
                    ReflectionUtils.getTypeArguments(field.getGenericType()));
        }
    }
}
