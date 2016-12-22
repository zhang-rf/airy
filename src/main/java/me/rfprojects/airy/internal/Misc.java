package me.rfprojects.airy.internal;

import me.rfprojects.airy.core.ClassRegistry;

public class Misc {

    public static boolean isPrimitive(Class<?> type, ClassRegistry registry) {
        return registry.isPrimitive(type) || type.isEnum() || type == String.class;
    }
}
