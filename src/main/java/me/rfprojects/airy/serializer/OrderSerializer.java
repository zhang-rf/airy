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

public class OrderSerializer extends AbstractReferencedStructuredSerializer implements StructuredSerializer {

    private ClassRegistry registry;

    public OrderSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        super(registry, handlerChain);
        this.registry = registry;
    }

    @Override
    protected void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        try {
            Map<Object, Integer> objectMap = objectMap();
            objectMap.put(object, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();
            List<Field> fieldList = getFieldList(object.getClass());
            Deque<Integer> addressDeque = new ArrayDeque<>(fieldList.size());
            for (Field field : fieldList) {
                if (!field.isAccessible())
                    field.setAccessible(true);
                Class<?> type = field.getType();
                Object value = field.get(object);
                if (value == Null.get(type))
                    addressDeque.push(0);
                else {
                    if (objectMap.containsKey(value))
                        addressDeque.push(-objectMap.get(value));
                    else {
                        int address = buffer.position();
                        addressDeque.push(address);
                        objectMap.put(value, address);
                        serialize(buffer, value, true, type, ReflectionUtils.getTypeArguments(field.getGenericType()));
                    }
                }
            }
            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            for (int address : addressDeque) {
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

            for (Field field : getFieldList(type)) {
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
                        Object value = deserialize(buffer, null, field.getType(), ReflectionUtils.getTypeArguments(field.getGenericType()));
                        field.set(instance, value);
                        addressMap.put(address, value);
                        buffer.reset().unmark();
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
            for (Field field : getFieldList(type)) {
                int offset = (int) buffer.getUnsignedVarint() - 1;
                accessorList.add(new Accessor(field, offset < 0 ? -1
                        : ((offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress))));
            }
            return (FieldAccessor[]) accessorList.toArray();
        } finally {
            buffer.reset().unmark();
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
}
