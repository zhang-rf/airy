package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.internal.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class HashSerializer extends AbstractReferencedStructuredSerializer implements StructuredSerializer {

    private ClassRegistry registry;

    @Override
    public boolean initialize(ClassRegistry registry, HandlerChain handlerChain) {
        this.registry = registry;
        return super.initialize(registry, handlerChain);
    }

    @Override
    protected void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        try {
            Map<Object, Integer> objectMap = objectMap();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();
            Class<?> nextClass = object.getClass();
            Map<Field, Integer> fieldAddressMap = null;
            do {
                Field[] fields = nextClass.getDeclaredFields();
                if (fieldAddressMap == null)
                    fieldAddressMap = new HashMap<>(Math.max(fields.length, 16));

                for (Field field : fields) {
                    if (!ReflectionUtils.isFieldSerializable(field))
                        continue;

                    if (!field.isAccessible())
                        field.setAccessible(true);
                    Class<?> type = field.getType();
                    Object value = field.get(object);
                    if (!(Objects.equals(value, Null.get(type)))) {
                        if (objectMap.containsKey(value))
                            fieldAddressMap.put(field, -objectMap.get(value));
                        else {
                            int address = buffer.position();
                            fieldAddressMap.put(field, address);
                            objectMap.put(value, address);
                            serialize(buffer, value, true, type, ReflectionUtils.getTypeArguments(field.getGenericType()));
                        }
                    }
                }
            } while ((nextClass = nextClass.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            buffer.putUnsignedVarint(fieldAddressMap.size());
            for (Map.Entry<Field, Integer> fieldEntry : fieldAddressMap.entrySet()) {
                buffer.asByteBuffer().putShort((short) fieldEntry.getKey().getName().hashCode());
                int address = fieldEntry.getValue();
                buffer.putUnsignedVarint(address > 0 ? address - baseAddress : -address + headerAddress);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object readObject(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
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

            int fieldSize = (int) buffer.getUnsignedVarint();
            Map<Short, Field> fieldMap = getFieldMap(type, fieldSize);
            for (int i = 0; i < fieldSize; i++) {
                Field field = fieldMap.get(buffer.asByteBuffer().getShort());
                int offset = (int) buffer.getUnsignedVarint();
                if (field == null)
                    continue;

                if (!field.isAccessible())
                    field.setAccessible(true);
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                if (addressMap.containsKey(address)) {
                    Object value = addressMap.get(address);
                    field.set(instance, value == this ? instance : value);
                } else {
                    buffer.mark().position(address);
                    Object value = deserialize(buffer, null, field.getType(), ReflectionUtils.getTypeArguments(field.getGenericType()));
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

    @Override
    public FieldAccessor[] getAccessors(NioBuffer buffer, Class<?> type) {
        buffer.mark();
        try {
            if ((type = registry.readClass(buffer, type)) == null)
                throw new UnknownClassException();

            List<FieldAccessor> accessorList = new ArrayList<>();
            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();
            buffer.position(headerAddress);

            int fieldSize = (int) buffer.getUnsignedVarint();
            Map<Short, Field> fieldMap = getFieldMap(type, fieldSize);
            for (int i = 0; i < fieldSize; i++) {
                Field field = fieldMap.remove(buffer.asByteBuffer().getShort());
                int offset = (int) buffer.getUnsignedVarint();
                if (field != null) {
                    int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                    accessorList.add(new Accessor(field, address));
                }
            }
            for (Field field : fieldMap.values())
                accessorList.add(new Accessor(field, -1));
            return accessorList.toArray(new FieldAccessor[accessorList.size()]);
        } finally {
            buffer.reset().unmark();
        }
    }

    private Map<Short, Field> getFieldMap(Class<?> type, int fieldSize) {
        Map<Short, Field> fieldMap = new HashMap<>(fieldSize);
        do {
            for (Field field : type.getDeclaredFields()) {
                if (ReflectionUtils.isFieldSerializable(field))
                    fieldMap.put((short) field.getName().hashCode(), field);
            }
        } while ((type = type.getSuperclass()) != Object.class);
        return fieldMap;
    }
}
