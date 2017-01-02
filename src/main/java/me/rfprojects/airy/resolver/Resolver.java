package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public interface Resolver {

    boolean checkType(Class<?> type);

    boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    Object readObject(NioBuffer buffer, Class<?> reference, Type... generics);
}
