package me.rfprojects.airy.internal;

import java.lang.reflect.*;

public class ReflectionUtils {

    public static boolean isFieldSerializable(Field field) {
        int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    public static Type[] getTypeArguments(Type type) {
        if (type instanceof GenericArrayType)
            type = ((GenericArrayType) type).getGenericComponentType();
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments();
        return new Type[0];
    }

    public static Class<?> getComponentType(Class<?> type) {
        while (type.isArray())
            type = type.getComponentType();
        return type;
    }
}
