package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ReferencedSerializer extends AbstractSerializer {

    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new IdentityHashMapTLSR<>();
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new IdentityHashMapTLSR<>();

    public ReferencedSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        super(registry, handlerChain);
    }

    @Override
    protected void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            super.serialize(buffer, object, writeClass);
        } finally {
            objectMap().clear();
        }
    }

    @Override
    protected Object deserialize(NioBuffer buffer, Class<?> type) {
        try {
            return super.deserialize(buffer, type);
        } finally {
            addressMap().clear();
        }
    }

    protected Map<Object, Integer> objectMap() {
        return objectMapReference.get();
    }

    protected Map<Integer, Object> addressMap() {
        return addressMapReference.get();
    }

    private static class IdentityHashMapTLSR<K, V> extends ThreadLocalReference<Map<K, V>> {

        @Override
        protected Reference<? extends Map<K, V>> initialValue() {
            return new SoftReference<>(new IdentityHashMap<K, V>());
        }
    }
}
