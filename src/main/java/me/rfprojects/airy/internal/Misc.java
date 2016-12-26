package me.rfprojects.airy.internal;

import me.rfprojects.airy.core.ClassRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Misc {

    public static boolean isPrimitive(Class<?> type, ClassRegistry registry) {
        return type != null && (registry.isPrimitive(type) || type.isEnum() || type == String.class);
    }

    public static boolean isFieldSerializable(Field field) {
        int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    public static Type[] getGenericTypes(Type parameterizedType) {
        Type[] genericTypes = null;
        if (parameterizedType instanceof ParameterizedType)
            genericTypes = ((ParameterizedType) parameterizedType).getActualTypeArguments();
        return genericTypes;
    }
}
