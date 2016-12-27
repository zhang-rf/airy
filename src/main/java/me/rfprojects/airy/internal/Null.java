package me.rfprojects.airy.internal;

import java.util.HashMap;
import java.util.Map;

public class Null {

    private static Map<Class<?>, Object> nullMap = new HashMap<>();

    static {
        nullMap.put(boolean.class, false);
        nullMap.put(char.class, '0');
        nullMap.put(byte.class, (byte) 0);
        nullMap.put(short.class, (short) 0);
        nullMap.put(int.class, 0);
        nullMap.put(long.class, 0L);
        nullMap.put(float.class, 0.0F);
        nullMap.put(double.class, 0.0);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        return (T) nullMap.get(type);
    }

    public static boolean isNull(Object value, Class<?> type) {
        return value == null || value.equals(get(type));
    }
}
