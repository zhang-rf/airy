package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.Resolver;

public interface Serializer {

    void addResolver(Resolver resolver);

    ClassRegistry getRegistry();

    void serialize(NioBuffer buffer, Object object, boolean writeClass);

    <T> T deserialize(NioBuffer buffer, Class<T> type);
}
