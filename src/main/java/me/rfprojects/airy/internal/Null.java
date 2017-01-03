package me.rfprojects.airy.internal;

import java.util.HashMap;
import java.util.Map;

public class Null {

    private static final Map<Class<?>, Object> NULLS = new HashMap<>();

    static {
        NULLS.put(boolean.class, false);
        NULLS.put(char.class, '0');
        NULLS.put(byte.class, (byte) 0);
        NULLS.put(short.class, (short) 0);
        NULLS.put(int.class, 0);
        NULLS.put(long.class, 0L);
        NULLS.put(float.class, 0.0F);
        NULLS.put(double.class, 0.0);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        return (T) NULLS.get(type);
    }

    public static boolean isNull(Object value, Class<?> type) {
        return value == null || value.equals(get(type));
    }
}
