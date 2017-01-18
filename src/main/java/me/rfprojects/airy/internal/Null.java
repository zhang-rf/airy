package me.rfprojects.airy.internal;

import java.util.HashMap;
import java.util.Map;

public class Null {

    private static final Map<Class<?>, Object> nulls = new HashMap<>();

    static {
        nulls.put(boolean.class, false);
        nulls.put(char.class, '0');
        nulls.put(byte.class, (byte) 0);
        nulls.put(short.class, (short) 0);
        nulls.put(int.class, 0);
        nulls.put(long.class, 0L);
        nulls.put(float.class, 0.0F);
        nulls.put(double.class, 0.0);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        return (T) nulls.get(type);
    }
}
