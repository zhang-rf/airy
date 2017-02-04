package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public interface Handler {

    boolean supportsType(Class<?> type);

    void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    Object read(NioBuffer buffer, Class<?> reference, Type... generics);
}
