package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;

public class MapHandler implements Handler {

    private ClassRegistry registry;
    private Serializer serializer;

    public MapHandler(ClassRegistry registry, Serializer serializer) {
        this.registry = registry;
        this.serializer = serializer;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Class<?> keyType = null, valueType = null;
        boolean isFinalKeyType = false, isFinalValueType = false;
        if (generics.length == 2) {
            if (generics[0] instanceof Class) {
                keyType = (Class<?>) generics[0];
                isFinalKeyType = Modifier.isFinal(keyType.getModifiers());
            }
            if (generics[1] instanceof Class) {
                valueType = (Class<?>) generics[1];
                isFinalValueType = Modifier.isFinal(valueType.getModifiers());
            }
        }

        Map<?, ?> map = (Map) object;
        registry.writeClass(buffer, map.getClass());
        buffer.putUnsignedVarint(map.size());

        boolean containsNullKey = map.containsKey(null);
        boolean containsNullValue = map.containsValue(null);
        buffer.putBoolean(containsNullKey);
        buffer.putBoolean(containsNullValue);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (key == null)
                registry.writeClass(buffer, Map.class);
            else {
                if (!isFinalKeyType || containsNullKey) {
                    Class<?> type = key.getClass();
                    registry.writeClass(buffer, type != keyType ? type : null);
                }
                serializer.serialize(buffer, key, false, null);
            }

            Object value = entry.getValue();
            if (value == null)
                registry.writeClass(buffer, Map.class);
            else {
                if (!isFinalValueType || containsNullValue) {
                    Class<?> type = value.getClass();
                    registry.writeClass(buffer, type != valueType ? type : null);
                }
                serializer.serialize(buffer, value, false, null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        try {
            Class<?> keyType = null, valueType = null;
            boolean isFinalKeyType = false, isFinalValueType = false;
            if (generics.length == 2) {
                if (generics[0] instanceof Class) {
                    keyType = (Class<?>) generics[0];
                    isFinalKeyType = Modifier.isFinal(keyType.getModifiers());
                }
                if (generics[1] instanceof Class) {
                    valueType = (Class<?>) generics[1];
                    isFinalValueType = Modifier.isFinal(valueType.getModifiers());
                }
            }

            Class<?> mapType = registry.readClass(buffer, null);
            int size = (int) buffer.getUnsignedVarint();
            Map map;
            try {
                map = (Map) mapType.getConstructor(int.class).newInstance(size);
            } catch (NoSuchMethodException ignored) {
                map = (Map) mapType.newInstance();
            }

            boolean containsNullKey = buffer.getBoolean();
            boolean containsNullValue = buffer.getBoolean();
            for (int i = 1; i <= size; i++) {
                Class<?> type = keyType;
                if (!isFinalKeyType || containsNullKey)
                    type = registry.readClass(buffer, type);
                Object key = null;
                if (type != Map.class)
                    key = serializer.deserialize(buffer, type, null);

                type = valueType;
                if (!isFinalValueType || containsNullValue)
                    type = registry.readClass(buffer, type);
                Object value = null;
                if (type != Map.class)
                    value = serializer.deserialize(buffer, type, null);
                map.put(key, value);
            }
            return map;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
