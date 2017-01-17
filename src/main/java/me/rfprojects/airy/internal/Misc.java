package me.rfprojects.airy.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Misc {

    public static Class<?> getComponentType(Class<?> type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }

    public static boolean isFieldSerializable(Field field) {
        int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    public static Type[] getGenericTypes(Type parameterizedType) {
        return parameterizedType instanceof ParameterizedType ? ((ParameterizedType) parameterizedType).getActualTypeArguments() : new Type[0];
    }
}
