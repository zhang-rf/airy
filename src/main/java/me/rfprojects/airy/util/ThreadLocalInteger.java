package me.rfprojects.airy.util;

import java.io.Serializable;

public class ThreadLocalInteger extends Number implements Serializable {

    private static final long serialVersionUID = 6214790243416807050L;
    private transient ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public final int get() {
        return threadLocal.get();
    }

    public final void set(int newValue) {
        threadLocal.set(newValue);
    }

    public final int getAndSet(int newValue) {
        int value = threadLocal.get();
        threadLocal.set(newValue);
        return value;
    }

    public final int getAndIncrement() {
        int value = threadLocal.get();
        threadLocal.set(value + 1);
        return value;
    }

    public final int getAndDecrement() {
        int value = threadLocal.get();
        threadLocal.set(value - 1);
        return value;
    }

    public final int getAndAdd(int delta) {
        int value = threadLocal.get();
        threadLocal.set(value + delta);
        return value;
    }

    public final int incrementAndGet() {
        int value = threadLocal.get() + 1;
        threadLocal.set(value);
        return value;
    }

    public final int decrementAndGet() {
        int value = threadLocal.get() - 1;
        threadLocal.set(value);
        return value;
    }

    public final int addAndGet(int delta) {
        int value = threadLocal.get() + delta;
        threadLocal.set(value);
        return value;
    }

    @Override
    public int intValue() {
        return get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return (double) get();
    }

    @Override
    public String toString() {
        return Integer.toString(get());
    }
}
