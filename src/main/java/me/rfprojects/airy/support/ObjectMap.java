package me.rfprojects.airy.support;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.resolver.Resolver;
import me.rfprojects.airy.serializer.StructedSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ObjectMap implements Map<String, Object> {

    private byte[] data;
    private StructedSerializer serializer;
    private Class<?> type;

    private NioBuffer buffer;
    private Map<String, Object> map;

    public ObjectMap(byte[] data) {
        this(data, null, null);
    }

    public ObjectMap(byte[] data, StructedSerializer serializer) {
        this(data, serializer, null);
    }

    public ObjectMap(byte[] data, StructedSerializer serializer, Class<?> type) {
        this.data = Objects.requireNonNull(data);
        this.serializer = serializer;
        this.type = type;
    }

    public StructedSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(StructedSerializer serializer) {
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
                for (String fieldName : fieldNames) {
                    type = serializer.getRegistry().readClass(buffer(), type);
                    if (type == null)
                        throw new UnknownClassException();
                    Map<String, Integer> structMap = serializer.getStructMap(buffer(), type);
                    Integer address = structMap.get(fieldName);
                    if (address == null)
                        return null;
                    buffer().position(address);
                }

                Field field = null;
                do {
                    try {
                        field = type.getDeclaredField(fieldNames[fieldNames.length - 1]);
                        break;
                    } catch (NoSuchFieldException e) {
                        type = type.getSuperclass();
                        if (type == Object.class)
                            throw e;
                    }
                } while (true);
                put((String) key, value = ((Resolver) serializer).readObject(buffer(), field.getType(), getGenericTypes(field.getGenericType())));
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

    private Type[] getGenericTypes(Type parameterizedType) {
        return parameterizedType instanceof ParameterizedType ? ((ParameterizedType) parameterizedType).getActualTypeArguments() : new Type[0];
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
