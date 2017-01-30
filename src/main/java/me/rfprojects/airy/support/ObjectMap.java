package me.rfprojects.airy.support;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.StructuredSerializer;

import java.util.*;

public class ObjectMap implements Map<String, Object> {

    private byte[] data;
    private StructuredSerializer serializer;
    private Class<?> type;

    private NioBuffer buffer;
    private Map<String, Object> map;

    public ObjectMap(byte[] data) {
        this(data, null, null);
    }

    public ObjectMap(byte[] data, StructuredSerializer serializer) {
        this(data, serializer, null);
    }

    public ObjectMap(byte[] data, StructuredSerializer serializer, Class<?> type) {
        this.data = Objects.requireNonNull(data);
        this.serializer = serializer;
        this.type = type;
    }

    public StructuredSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(StructuredSerializer serializer) {
        this.serializer = serializer;
    }

    protected byte[] getData() {
        return data;
    }

    protected NioBuffer buffer() {
        if (buffer == null)
            buffer = NioBuffer.wrap(data);
        return buffer;
    }

    protected Map<String, Object> map() {
        if (map == null)
            map = new HashMap<>();
        return map;
    }

    @Override
    public Object get(Object key) {
        Object value = map().get(key);
        try {
            if (value == null) {
                Class<?> type = this.type;
                String[] fieldNames = ((String) key).split("\\.");
                StructuredSerializer.FieldAccessor accessor = null;
                for (String fieldName : fieldNames) {
                    accessor = serializer.getAccessor(buffer(), type, fieldName);
                    type = accessor.getField().getType();
                    buffer().position(accessor.getAddress());
                }
                put((String) key, value = accessor.accessValue(buffer()));
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            buffer().clear();
        }
    }

    @Override
    public int size() {
        return map().size();
    }

    @Override
    public boolean isEmpty() {
        return map().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map().containsValue(value);
    }

    @Override
    public Object put(String key, Object value) {
        return map().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map().putAll(m);
    }

    @Override
    public void clear() {
        map().clear();
    }

    @Override
    public Set<String> keySet() {
        return map().keySet();
    }

    @Override
    public Collection<Object> values() {
        return map().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map().entrySet();
    }
}
