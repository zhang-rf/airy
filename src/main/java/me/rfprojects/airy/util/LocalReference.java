package me.rfprojects.airy.util;

import java.lang.ref.Reference;

public abstract class LocalReference<T> {

    private ThreadLocal<Reference<? extends T>> reference = new ThreadLocal<Reference<? extends T>>() {

        @Override
        protected Reference<? extends T> initialValue() {
            return LocalReference.this.initialValue();
        }
    };

    protected abstract Reference<? extends T> initialValue();

    public T get() {
        T instance = reference.get().get();
        if (instance == null) {
            reference.remove();
            instance = reference.get().get();
        }
        return instance;
    }

    public void set(Reference<? extends T> reference) {
        this.reference.set(reference);
    }

    public void remove() {
        reference.remove();
    }
}
