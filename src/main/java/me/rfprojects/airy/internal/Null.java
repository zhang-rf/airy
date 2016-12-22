package me.rfprojects.airy.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Null {

    private static final Map<Class<?>, Object> NULLS;

    static {
        Map<Class<?>, Object> map = new HashMap<>();
        map.put(boolean.class, false);
        map.put(char.class, '0');
        map.put(byte.class, (byte) 0);
        map.put(short.class, (short) 0);
        map.put(int.class, 0);
        map.put(long.class, 0L);
        map.put(float.class, 0.0F);
        map.put(double.class, 0.0);
        NULLS = Collections.unmodifiableMap(map);
    }

    public static boolean isNull(Object obj, Class<?> referenceType) {
        return obj == null || obj.equals(get(referenceType));
    }

    public static Object get(Class<?> type) {
        return NULLS.get(type);
    }
}
