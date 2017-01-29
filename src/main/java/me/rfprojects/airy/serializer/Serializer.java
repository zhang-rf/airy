package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public interface Serializer {

    void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics);

    Object deserialize(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics);
}
