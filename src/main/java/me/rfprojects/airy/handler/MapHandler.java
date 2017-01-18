package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class MapHandler implements Handler {

    private Serializer serializer;

    public MapHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Map<?, ?> map = (Map) object;
        buffer.putUnsignedVarint(map.size());
        serializer.registry().writeClass(buffer, object.getClass());

        Class<?> keyType = null, valueType = null;
        boolean isKeyFinal = false, isValueFinal = false;
        if (generics.length == 2) {
            if (generics[0] instanceof Class) {
                keyType = (Class<?>) generics[0];
                isKeyFinal = Modifier.isFinal(keyType.getModifiers());
            }
            if (generics[1] instanceof Class) {
                valueType = (Class<?>) generics[1];
                isValueFinal = Modifier.isFinal(valueType.getModifiers());
            }
        }

        buffer.mark().asByteBuffer().putInt(-1);
        List<Integer> nullList = new ArrayList<>();
        int index = 1;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (!isKeyFinal) {
                Class<?> type = key.getClass();
                serializer.registry().writeClass(buffer, type != keyType ? type : null);
            }
            serializer.serialize(buffer, key, false);

            Object value = entry.getValue();
            if (value == null)
                nullList.add(index);
            else {
                if (!isValueFinal) {
                    Class<?> type = value.getClass();
                    serializer.registry().writeClass(buffer, type != valueType ? type : null);
                }
                serializer.serialize(buffer, value, false);
            }
            index++;
        }

        if (!nullList.isEmpty()) {
            int nullsAddress = buffer.position();
            for (int i : nullList)
                buffer.putUnsignedVarint(i);
            buffer.asByteBuffer().put((byte) 0);
            int position = buffer.position();
            buffer.reset().asByteBuffer().putInt(nullsAddress).position(position);
        }
        buffer.unmark();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        try {
            Class<?> keyType = null, valueType = null;
            boolean isKeyFinal = false, isValueFinal = false;
            if (generics.length == 2) {
                if (generics[0] instanceof Class) {
                    keyType = (Class<?>) generics[0];
                    isKeyFinal = Modifier.isFinal(keyType.getModifiers());
                }
                if (generics[1] instanceof Class) {
                    valueType = (Class<?>) generics[1];
                    isValueFinal = Modifier.isFinal(valueType.getModifiers());
                }
            }

            int size = (int) buffer.getUnsignedVarint();
            Class<?> mapType = serializer.registry().readClass(buffer, null);
            Map map;
            try {
                map = (Map) mapType.getConstructor(int.class).newInstance(size);
            } catch (NoSuchMethodException ignored) {
                map = (Map) mapType.newInstance();
            }

            int nullsAddress = buffer.asByteBuffer().getInt();
            Queue<Integer> nullQueue = null;
            if (nullsAddress > 0) {
                nullQueue = new ArrayDeque<>();
                buffer.mark().position(nullsAddress);
                int index;
                while ((index = (int) buffer.getUnsignedVarint()) != 0)
                    nullQueue.add(index);
                buffer.reset().unmark();
            }

            for (int i = 1; i <= size; i++) {
                Class<?> type = keyType;
                if (!isKeyFinal)
                    type = serializer.registry().readClass(buffer, type);
                Object key = serializer.deserialize(buffer, type);

                if (nullQueue != null && !nullQueue.isEmpty() && nullQueue.peek() == i) {
                    map.put(key, null);
                    nullQueue.remove();
                } else {
                    type = valueType;
                    if (!isValueFinal)
                        type = serializer.registry().readClass(buffer, valueType);
                    map.put(key, serializer.deserialize(buffer, type));
                }
            }
            return map;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
