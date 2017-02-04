package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ReferencedSerializer extends AbstractSerializer {

    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new IdentityHashMapTLSR<>();
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new IdentityHashMapTLSR<>();

    @Override
    protected void serialize$Surface(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
        try {
            super.serialize$Surface(buffer, object, writeClass, reference, generics);
        } finally {
            objectMap().clear();
        }
    }

    @Override
    protected Object deserialize$Surface(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
        try {
            return super.deserialize$Surface(buffer, type, reference, generics);
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
