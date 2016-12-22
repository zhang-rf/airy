package me.rfprojects.airy.util;

import java.lang.ref.Reference;

public abstract class ThreadLocalReference<T> {

    private ThreadLocal<Reference<T>> threadLocalReference = new ThreadLocal<Reference<T>>() {

        @Override
        protected Reference<T> initialValue() {
            return ThreadLocalReference.this.initialValue();
        }
    };

    protected abstract Reference<T> initialValue();

    public T get() {
        T instance = threadLocalReference.get().get();
        if (instance == null) {
            threadLocalReference.remove();
            instance = threadLocalReference.get().get();
        }
        return instance;
    }

    public void set(Reference<T> reference) {
        threadLocalReference.set(reference);
    }

    public void remove() {
        threadLocalReference.remove();
    }
}
