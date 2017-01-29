package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractSerializer implements Serializer {

    private ClassRegistry registry;
    private HandlerChain handlerChain;
    private ThreadLocal<Boolean> sRecursionFlag = new ThreadLocal<>();
    private ThreadLocal<Boolean> desRecursionFlag = new ThreadLocal<>();

    public AbstractSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        this.registry = Objects.requireNonNull(registry);
        this.handlerChain = Objects.requireNonNull(handlerChain);
    }

    protected abstract void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    protected abstract Object readObject(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics);

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
        if (sRecursionFlag.get() != Boolean.TRUE) {
            serialize(buffer, object, writeClass);
            return;
        }

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
        }
    }

    protected void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        sRecursionFlag.set(Boolean.TRUE);
        try {
            if (!writeClass)
                registry.writeClass(buffer, null);
            serialize(buffer, object, writeClass, null);
        } finally {
            sRecursionFlag.remove();
        }
    }

    @Override
    public Object deserialize(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
        if (desRecursionFlag.get() != Boolean.TRUE)
            return deserialize(buffer, type);

        if (handlerChain.supportsType(reference))
            return handlerChain.read(buffer, reference, generics);
        else {
            if (type == null)
                type = registry.readClass(buffer, reference);
            if (type == Object.class)
                return new Object();
            else if (handlerChain.supportsType(type))
                return handlerChain.read(buffer, reference, generics);
            else
                return readObject(buffer, type, reference, generics);
        }
    }

    protected Object deserialize(NioBuffer buffer, Class<?> type) {
        desRecursionFlag.set(Boolean.TRUE);
        try {
            if ((type = registry.readClass(buffer, type)) == null)
                throw new UnknownClassException();
            return deserialize(buffer, type, null);
        } finally {
            desRecursionFlag.remove();
        }
    }
}
