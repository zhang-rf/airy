package me.rfprojects.airy.internal;

public class Misc {

    public static Class<?> getComponentType(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }
}
