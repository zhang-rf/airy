package me.rfprojects.airy.util;

import java.lang.ref.Reference;

public class ThreadLocalReference<T> {

    private Class<? extends Reference> referenceType;
    private Class<?> valueType;
    private ThreadLocal<Reference<T>> threadLocalReference = new ThreadLocal<Reference<T>>() {

        @Override
        protected Reference<T> initialValue() {
            return ThreadLocalReference.this.initialValue();
        }
    };

    protected ThreadLocalReference() {
    }

    public ThreadLocalReference(Class<? extends Reference> referenceType, Class<?> valueType) {
        this.referenceType = referenceType;
        this.valueType = valueType;
    }

    @SuppressWarnings("unchecked")
    protected Reference<T> initialValue() {
        if (referenceType == null || valueType == null)
            throw new AbstractMethodError();
        try {
            return referenceType.getConstructor(Object.class).newInstance(valueType.newInstance());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
