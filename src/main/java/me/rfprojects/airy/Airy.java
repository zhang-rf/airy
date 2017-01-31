package me.rfprojects.airy;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.*;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.handler.chain.SimpleHandlerChain;
import me.rfprojects.airy.handler.primitive.*;
import me.rfprojects.airy.serializer.HashSerializer;
import me.rfprojects.airy.serializer.Serializer;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.util.Arrays;

public class Airy {

    private ClassRegistry registry;
    private HandlerChain handlerChain;
    private Serializer serializer;
    private ThreadLocalReference<NioBuffer> bufferReference = new ThreadLocalReference<NioBuffer>() {
        @Override
        protected Reference<NioBuffer> initialValue() {
            return new WeakReference<>(NioBuffer.allocate(8192));
        }
    };

    public Airy() {
        registry = new ClassRegistry();
        handlerChain = new SimpleHandlerChain();
        serializer = new HashSerializer();
        serializer.initialize(registry, handlerChain);
        appendDefaultHandlers();
    }

    public Airy(Serializer serializer) {
        registry = new ClassRegistry();
        handlerChain = new SimpleHandlerChain();
        this.serializer = serializer;
        serializer.initialize(registry, handlerChain);
        appendDefaultHandlers();
    }

    public Airy(ClassRegistry registry, HandlerChain handlerChain, Serializer serializer) {
        this.registry = registry;
        this.handlerChain = handlerChain;
        this.serializer = serializer;
    }

    public ClassRegistry getRegistry() {
        return registry;
    }

    public HandlerChain getHandlerChain() {
        return handlerChain;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public boolean appendHandler(Handler handler) {
        return handlerChain.appendHandler(handler);
    }

    public int registryClass(Class<?> type) {
        return registry.register(type);
    }

    public void appendDefaultHandlers() {
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

    public byte[] serialize(Object object) {
        NioBuffer buffer = bufferReference.get();
        int length = serialize(buffer, object, true);
        return Arrays.copyOf(buffer.array(), length);
    }

    public int serialize(Object object, byte[] dest) {
        return serialize(object, dest, 0, dest.length);
    }

    public int serialize(Object object, byte[] dest, int offset, int length) {
        return serialize(NioBuffer.wrap(dest, offset, length), object, true);
    }

    private int serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            serializer.serialize(buffer, object, writeClass, null);
            return buffer.position() - buffer.arrayOffset();
        } catch (BufferOverflowException e) {
            return serialize(buffer.clear().capacity(buffer.capacity() * 2), object, writeClass);
        } finally {
            buffer.clear();
        }
    }

    public Object deserialize(byte[] bytes) {
        return deserialize(bytes, null);
    }

    public <T> T deserialize(byte[] bytes, Class<T> type) {
        return deserialize(bytes, 0, bytes.length, type);
    }

    public Object deserialize(byte[] bytes, int offset, int length) {
        return deserialize(bytes, offset, length, null);
    }

    public <T> T deserialize(byte[] bytes, int offset, int length, Class<T> type) {
        return deserialize(NioBuffer.wrap(bytes, offset, length), type);
    }

    public Object deserialize(NioBuffer buffer) {
        return deserialize(buffer, null);
    }

    public <T> T deserialize(NioBuffer buffer, Class<T> type) {
        return serializer.deserialize(buffer, type, null);
    }
}
