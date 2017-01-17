package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;

public interface Serializer {

    ClassRegistry registry();

    HandlerChain handlerChain();

    void serialize(NioBuffer buffer, Object object, boolean writeClass);

    <T> T deserialize(NioBuffer buffer, Class<T> type);
}
