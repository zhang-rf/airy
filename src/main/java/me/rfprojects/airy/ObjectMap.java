package me.rfprojects.airy;

import me.rfprojects.airy.core.NioBuffer;

import java.util.HashMap;
import java.util.Map;

public class ObjectMap<T> extends AbstractObjectMap<T> {

    private Map<String, Object> map = new HashMap<>();
    private NioBuffer buffer;

    public ObjectMap(byte[] data) {
        this(data, null);
    }

    public ObjectMap(byte[] data, Class<T> clazz) {
        super(data, clazz);
        buffer = NioBuffer.wrap(this.data);
    }

    @Override
    protected Map<String, Object> getMap() {
        return map;
    }

    @Override
    public Object get(Object key) {
        return get(buffer, key);
    }
}
