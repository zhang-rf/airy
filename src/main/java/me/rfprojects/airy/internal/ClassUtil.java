package me.rfprojects.airy.internal;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassUtil {

    private static ConcurrentMap<String, Class<?>> classMap = new ConcurrentHashMap<>();

    public static Class<?> readClass(NioBuffer buffer, Class<?> defaultClass, ClassRegistry registry) {
        try {
            int classId = (int) buffer.getUnsignedVarint();
            if (classId > 0)
                return registry.getClass(classId);
            else {
                String className = buffer.getString();
                if (!"".equals(className)) {
                    Class<?> clazz = classMap.get(className);
                    if (clazz == null) {
                        clazz = Class.forName(className);
                        classMap.put(className, clazz);
                    }
                    return clazz;
                }
            }
            return defaultClass;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeClassName(NioBuffer buffer, Class<?> clazz, ClassRegistry registry, boolean condition) {
        if (condition) {
            int classId = registry.idOf(clazz);
            buffer.putUnsignedVarint(classId);
            if (classId == 0)
                buffer.putString(clazz.getName());
        } else {
            buffer.asByteBuffer().put((byte) 0);
            buffer.putString("");
        }
    }
}
