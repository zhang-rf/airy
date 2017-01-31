package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;

import java.lang.reflect.Type;

public interface Serializer {

    boolean initialize(ClassRegistry registry, HandlerChain handlerChain);

    void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics);

    <T> T deserialize(NioBuffer buffer, Class<T> type, Class<?> reference, Type... generics);
}
