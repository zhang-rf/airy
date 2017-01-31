package me.rfprojects.airy.support;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.*;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.handler.chain.SimpleHandlerChain;
import me.rfprojects.airy.handler.primitive.*;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.serializer.HashSerializer;
import me.rfprojects.airy.serializer.Serializer;
import me.rfprojects.airy.serializer.StructuredSerializer;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectMap implements Map<String, Object> {

    private byte[] data;
    private StructuredSerializer serializer;
    private Class<?> type;

    private NioBuffer buffer;
    private Map<String, Object> map;

    public ObjectMap(byte[] data) {
        this(data, (Class<?>) null);
    }

    public ObjectMap(byte[] data, Class<?> type) {
        this.data = Objects.requireNonNull(data);
        ClassRegistry registry = new ClassRegistry();
        HandlerChain handlerChain = new SimpleHandlerChain();
        StructuredSerializer serializer = new HashSerializer();
        serializer.initialize(registry, handlerChain);
        appendDefaultHandlers(handlerChain, registry, serializer);
        this.serializer = serializer;
        this.type = type;
    }

    public ObjectMap(byte[] data, StructuredSerializer serializer) {
        this(data, serializer, null);
    }

    public ObjectMap(byte[] data, StructuredSerializer serializer, Class<?> type) {
        this.data = Objects.requireNonNull(data);
        this.serializer = serializer;
        this.type = type;
    }

    private void appendDefaultHandlers(HandlerChain handlerChain, ClassRegistry registry, Serializer serializer) {
        handlerChain.appendHandler(new BooleanHandler());
        handlerChain.appendHandler(new CharacterHandler());
        handlerChain.appendHandler(new ByteHandler());
        handlerChain.appendHandler(new ShortHandler());
        handlerChain.appendHandler(new IntegerHandler());
        handlerChain.appendHandler(new LongHandler());
        handlerChain.appendHandler(new FloatHandler());
        handlerChain.appendHandler(new DoubleHandler());

        handlerChain.appendHandler(new StringHandler());
        handlerChain.appendHandler(new EnumHandler());
        handlerChain.appendHandler(new BytesHandler());
        handlerChain.appendHandler(new ArrayHandler(registry, serializer));
        handlerChain.appendHandler(new CollectionHandler(registry, serializer));
        handlerChain.appendHandler(new MapHandler(registry, serializer));

        handlerChain.appendHandler(new BigIntegerHandler());
        handlerChain.appendHandler(new BigDecimalHandler());
        handlerChain.appendHandler(new DateHandler());
        handlerChain.appendHandler(new TimeZoneHandler());
        handlerChain.appendHandler(new CalendarHandler());
        handlerChain.appendHandler(new UrlHandler());
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
                StructuredSerializer.FieldAccessor[] accessors = serializer.getAccessors(buffer, type);
                String prefix = "";
                String[] fieldNames = ((String) key).split("\\.");
                for (String fieldName : fieldNames) {
                    StructuredSerializer.FieldAccessor theAccessor = null;
                    for (StructuredSerializer.FieldAccessor accessor : accessors) {
                        Field field = accessor.getField();
                        String name = field.getName();
                        map().put(prefix + name, accessor);
                        if (name.equals(fieldName)) {
                            theAccessor = accessor;
                            prefix += fieldName + '.';
                        }
                    }
                    if (theAccessor == null)
                        return null;
                    else {
                        type = theAccessor.getField().getType();
                        int address = theAccessor.getAddress();
                        if (address < 0)
                            return Null.get(type);
                        buffer().position(address);
                        accessors = serializer.getAccessors(buffer, type);
                    }
                }
                value = map().get(key);
            }
            if (value instanceof StructuredSerializer.FieldAccessor) {
                value = ((StructuredSerializer.FieldAccessor) value).accessValue(buffer);
                map().put((String) key, value);
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
