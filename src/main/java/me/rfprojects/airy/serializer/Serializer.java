package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public interface Serializer {

    void serialize(NioBuffer buffer, Object object, boolean writeClass);

    void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics);

    <T> T deserialize(NioBuffer buffer, Class<T> type);

    <T> T deserialize(NioBuffer buffer, Class<T> type, Class<?> reference, Type... generics);
}
