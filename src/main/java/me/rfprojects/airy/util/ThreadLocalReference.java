package me.rfprojects.airy.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class ThreadLocalReference<T> {

    private ReferenceType referenceType;
    private Class<?> valueClass;
    private ThreadLocal<Reference<T>> threadLocalReference = new ThreadLocal<Reference<T>>() {

        @Override
        protected Reference<T> initialValue() {
            return ThreadLocalReference.this.initialValue();
        }
    };

    protected ThreadLocalReference() {
    }

    public ThreadLocalReference(ReferenceType referenceType, Class<?> valueClass) {
        this.referenceType = referenceType;
        this.valueClass = valueClass;
    }

    @SuppressWarnings("unchecked")
    protected Reference<T> initialValue() {
        if (referenceType == null || valueClass == null)
            throw new AbstractMethodError();
        try {
            switch (referenceType) {
                case SoftReference:
                    return SoftReference.class.getConstructor(Object.class).newInstance(valueClass.newInstance());
                case WeakReference:
                    return WeakReference.class.getConstructor(Object.class).newInstance(valueClass.newInstance());
                case PhantomReference:
                    return PhantomReference.class.getConstructor(Object.class).newInstance(valueClass.newInstance());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
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

    public enum ReferenceType {
        SoftReference,
        WeakReference,
        PhantomReference
    }
}
