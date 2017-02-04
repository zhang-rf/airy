package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractSerializer implements Serializer {

    private ClassRegistry registry;
    private HandlerChain handlerChain;
    private boolean initialized;
    private ThreadLocal<Boolean> sRecursionFlag = new ThreadLocal<>();
    private ThreadLocal<Boolean> desRecursionFlag = new ThreadLocal<>();

    protected abstract void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    protected abstract Object readObject(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics);

    @Override
    public boolean initialize(ClassRegistry registry, HandlerChain handlerChain) {
        if (!initialized) {
            this.registry = Objects.requireNonNull(registry);
            this.handlerChain = Objects.requireNonNull(handlerChain);
            initialized = true;
            return true;
        }
        return false;
    }

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
        if (!initialized)
            throw new NotInitializedException();
        if (sRecursionFlag.get() != Boolean.TRUE)
            serialize$Surface(buffer, object, writeClass, reference, generics);
        else
            serialize$Recursion(buffer, object, writeClass, reference, generics);
    }

    protected void serialize$Surface(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
        sRecursionFlag.set(Boolean.TRUE);
        try {
            if (!writeClass)
                registry.writeClass(buffer, null);
            serialize(buffer, object, writeClass, reference, generics);
        } finally {
            sRecursionFlag.remove();
        }
    }

    protected void serialize$Recursion(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {
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
                handlerChain.write(buffer, object, type, generics);
            } else {
                if (writeClass)
                    registry.writeClass(buffer, type != reference ? type : null);
                writeObject(buffer, object, reference, generics);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> type, Class<?> reference, Type... generics) {
        if (!initialized)
            throw new NotInitializedException();
        if (desRecursionFlag.get() != Boolean.TRUE)
            return (T) deserialize$Surface(buffer, type, reference, generics);
        return (T) deserialize$Recursion(buffer, type, reference, generics);
    }

    protected Object deserialize$Surface(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
        desRecursionFlag.set(Boolean.TRUE);
        try {
            if ((type = registry.readClass(buffer, type)) == null)
                throw new UnknownClassException();
            return deserialize(buffer, type, reference, generics);
        } finally {
            desRecursionFlag.remove();
        }
    }

    protected Object deserialize$Recursion(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics) {
        if (handlerChain.supportsType(reference))
            return handlerChain.read(buffer, reference, generics);
        else {
            if (type == null)
                type = registry.readClass(buffer, reference);
            if (type == Object.class)
                return new Object();
            else if (handlerChain.supportsType(type))
                return handlerChain.read(buffer, type, generics);
            else
                return readObject(buffer, type, reference, generics);
        }
    }
}
