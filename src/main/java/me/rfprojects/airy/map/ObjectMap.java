package me.rfprojects.airy.map;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.serializer.StructedSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ObjectMap<T> implements Map<String, Object> {

    private NioBuffer buffer;
    private StructedSerializer serializer;
    private Class<?> type;
    private Map<String, Object> map;

    public ObjectMap(byte[] rawData, StructedSerializer serializer) {
        this(rawData, serializer, null);
    }

    public ObjectMap(byte[] rawData, StructedSerializer serializer, Class<T> type) {
        this.buffer = NioBuffer.wrap(Arrays.copyOf(rawData, rawData.length));
        this.serializer = serializer;
        this.type = type;
    }

    public void setSerializer(StructedSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Object get(Object key) {
        if (map == null)
            map = new HashMap<>();

        Object value = map.get(key);
        try {
            if (value == null) {
                Class<?> type = this.type;
                String[] fieldNames = ((String) key).split("\\.");
                for (int i = 0; i < fieldNames.length; i++) {
                    type = serializer.getRegistry().readClass(buffer, type);
                    if (type == null)
                        throw new UnknownClassException();
                    buffer.position(serializer.getStructHeader(buffer, type).get(fieldNames[i]));
                    if (i == fieldNames.length - 1) {
                        Field field = type.getDeclaredField(fieldNames[i]);
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        Class<?> reference = field.getType();
                        if (reference == Object.class)
                            reference = serializer.getRegistry().readClass(buffer.mark(), reference);
                        value = serializer.getResolverChain().readObject(buffer, reference, getGenericTypes(field.getGenericType()));
                        if (value == null)
                            value = serializer.deserialize(buffer.reset().unmark(), type);
                        map.put((String) key, value);
                    }
                }
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            buffer.clear();
        }
    }

    private Type[] getGenericTypes(Type parameterizedType) {
        return parameterizedType instanceof ParameterizedType ? ((ParameterizedType) parameterizedType).getActualTypeArguments() : new Type[0];
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}
