package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractSerializer implements Serializer {

    private ClassRegistry registry;
    private HandlerChain handlerChain;

    public AbstractSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        this.registry = Objects.requireNonNull(registry);
        this.handlerChain = Objects.requireNonNull(handlerChain);
    }

    protected abstract void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    protected abstract Object readObject(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics);

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
        if (handlerChain.supportsType(reference))
            handlerChain.write(buffer, object, reference, generics);
        else {
            Class<?> type = object.getClass();
            if (type == Object.class) {
                if (writeClass)
                    registry.writeClass(buffer, Object.class);
            } else if (handlerChain.supportsType(type)) {
                if (writeClass)
                    registry.writeClass(buffer, type);
                handlerChain.write(buffer, object, reference, generics);
            } else {
                if (writeClass)
                    registry.writeClass(buffer, type != reference ? type : null);
                writeObject(buffer, object, reference, generics);
            }
            if (!writeClass && reference == null)
                registry.writeClass(buffer, null);
        }
    }

    @Override
    public Object deserialize(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
        if (handlerChain.supportsType(reference))
            return handlerChain.read(buffer, reference, generics);
        else {
            type = registry.readClass(buffer, type == null ? reference : type);
            if (type == null)
                throw new UnknownClassException();
            else if (type == Object.class)
                return new Object();
            else if (handlerChain.supportsType(type))
                return handlerChain.read(buffer, reference, generics);
            else
                return readObject(buffer, type, reference, generics);
        }
    }
}
