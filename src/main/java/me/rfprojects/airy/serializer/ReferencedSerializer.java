package me.rfprojects.airy.serializer;

import me.rfprojects.airy.util.LocalReference;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ReferencedSerializer extends AbstractSerializer {

    protected static final Object PRESENT = new Object();
    private LocalReference<Map<Object, Integer>> objectMapReference = new IdentityHashMapLR<>();
    private LocalReference<Map<Integer, Object>> addressMapReference = new IdentityHashMapLR<>();

    protected Map<Object, Integer> objectMap() {
        return objectMapReference.get();
    }

    protected Map<Integer, Object> addressMap() {
        return addressMapReference.get();
    }

    private static class IdentityHashMapLR<K, V> extends LocalReference<Map<K, V>> {

        @Override
        protected Reference<? extends Map<K, V>> initialValue() {
            return new SoftReference<>(new IdentityHashMap<K, V>());
        }
    }
}
