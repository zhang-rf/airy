package me.rfprojects.airy.support;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.StructedSerializer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentObjectMap extends ObjectMap {

    private volatile ConcurrentMap<String, Object> map;
    private volatile ThreadLocal<NioBuffer> bufferLocal;

    public ConcurrentObjectMap(byte[] data) {
        super(data);
    }

    public ConcurrentObjectMap(byte[] data, StructedSerializer serializer) {
        super(data, serializer);
    }

    public ConcurrentObjectMap(byte[] data, StructedSerializer serializer, Class<?> type) {
        super(data, serializer, type);
    }

    @Override
    protected Map<String, Object> map() {
        if (map == null)
            synchronized (this) {
                if (map == null)
                    map = new ConcurrentHashMap<>();
            }
        return map;
    }

    @Override
    protected NioBuffer buffer() {
        if (bufferLocal == null)
            synchronized (PooledThreadLocalBuffer.class) {
                if (bufferLocal == null)
                    bufferLocal = new PooledThreadLocalBuffer(8);
            }
        return bufferLocal.get();
    }

    @Override
    public Object get(Object key) {
        Object value = map().get(key);
        if (value == null) {
            try {
                value = super.get(key);
            } finally {
                bufferLocal.remove();
            }
        }
        return value;
    }

    private class PooledThreadLocalBuffer extends ThreadLocal<NioBuffer> {

        private int capacity;
        private BlockingQueue<Reference<NioBuffer>> blockingQueue;
        private volatile AtomicInteger counter = new AtomicInteger();

        PooledThreadLocalBuffer(int capacity) {
            this.capacity = capacity;
            blockingQueue = new ArrayBlockingQueue<>(capacity);
        }

        @Override
        protected NioBuffer initialValue() {
            try {
                if (counter != null) {
                    if (counter.getAndIncrement() >= capacity)
                        counter = null;
                    return NioBuffer.wrap(getData());
                } else {
                    NioBuffer buffer = blockingQueue.take().get();
                    if (buffer == null)
                        buffer = NioBuffer.wrap(getData());
                    return buffer;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            try {
                blockingQueue.put(new WeakReference<>(get()));
                super.remove();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
