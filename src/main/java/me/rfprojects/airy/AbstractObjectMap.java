package me.rfprojects.airy;

import com.airy.object.resolver.ObjectResolver;
import com.airy.object.serializer.StructuredSerializer;
import com.airy.object.support.Nulls;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.airy.object.support.InternalUtils.readClass;

public abstract class AbstractObjectMap<T> implements Map<String, Object> {

    protected byte[] data;
    protected Class<?> clazz;
    private Serializer serializer = new Serializer();

    public AbstractObjectMap(byte[] data, Class<T> clazz) {
        this.data = Arrays.copyOf(data, data.length);
        this.clazz = clazz;
    }

    protected abstract Map<String, Object> getMap();

    protected Object get(NioBuffer buffer, Object key) {
        Object value = getMap().get(key);
        try {
            if (value == null) {
                Type referenceType = clazz;
                Label:
                for (String fieldName : ((String) key).split("\\.")) {
                    int headerAddress = buffer.asByteBuffer().getInt();
                    int baseAddress = buffer.position();
                    Class<?> class0 = readClass(buffer.position(headerAddress), (Class<?>) referenceType);
                    if (class0 == null)
                        throw new UnknownClassException();

                    Field field = null;
                    do {
                        for (Field field0 : class0.getDeclaredFields()) {
                            if (fieldName.equals(field0.getName())) {
                                field = field0;
                                referenceType = field.getGenericType();
                                break;
                            }
                        }
                    } while (field == null && (class0 = class0.getSuperclass()) != Object.class);
                    if (field == null)
                        throw new NoSuchFieldException();

                    short hashcode = (short) fieldName.hashCode();
                    int fieldSize = (int) buffer.getUnsignedVarint();
                    for (int i = 0; i < fieldSize; i++) {
                        if (buffer.asByteBuffer().getShort() == hashcode) {
                            int offset = (int) buffer.getUnsignedVarint();
                            int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                            buffer.position(address);
                            continue Label;
                        }
                        buffer.getUnsignedVarint();
                    }
                    if (referenceType instanceof Class)
                        value = Nulls.get((Class<?>) referenceType);
                    return value;
                }
                if (referenceType instanceof Class)
                    value = serializer.beforeDeserializing().deserialize(buffer, (Class<?>) referenceType);
                else
                    value = serializer.getMasterResolver().readObject(buffer, referenceType);
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            buffer.clear();
            getMap().put((String) key, value);
        }
    }

    @Override
    public int size() {
        return getMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getMap().containsValue(value);
    }

    @Override
    public abstract Object get(Object key);

    @Override
    public Object put(String key, Object value) {
        return getMap().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        getMap().putAll(m);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return getMap().keySet();
    }

    @Override
    public Collection<Object> values() {
        return getMap().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return getMap().entrySet();
    }

    private class Serializer extends StructuredSerializer {

        ObjectResolver getMasterResolver() {
            return masterResolver;
        }
    }
}
