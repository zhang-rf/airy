package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;
import me.rfprojects.airy.handler.chain.HandlerChain;

import java.lang.reflect.Type;

public abstract class AbstractSerializer implements Serializer, HandlerChain {

    private ClassRegistry registry;
    private HandlerChain handlerChain;

    public AbstractSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        this.registry = registry;
        this.handlerChain = handlerChain;
    }

    protected abstract void writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics);

    protected abstract Object readObject(NioBuffer buffer, Class<?> type, Class<?> reference, Type... generics);

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        serialize(buffer, object, writeClass, null);
    }

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass, Class<?> reference, Type... generics) {

    }

    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> type) {
        return deserialize(buffer, type, null);
    }

    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> type, Class<?> reference, Type... generics) {
        return null;
    }

    @Override
    public boolean appendHandler(Handler handler) {
        return handlerChain.appendHandler(handler);
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return true;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        if (handlerChain.supportsType(reference))
            handlerChain.write(buffer, object, reference, generics);
        else {
            Class<?> type = object.getClass();
            if (type == Object.class)
                registry.writeClass(buffer, Object.class);
            else if (handlerChain.supportsType(type)) {
                registry.writeClass(buffer, type);
                handlerChain.write(buffer, object, reference, generics);
            } else {
                registry.writeClass(buffer, type != reference ? type : null);
                writeObject(buffer, object, reference, generics);
            }
        }
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        if (handlerChain.supportsType(reference))
            return handlerChain.read(buffer, reference, generics);
        else {
            Class<?> type = registry.readClass(buffer, reference);
            if (type == Object.class)
                return new Object();
            else if (handlerChain.supportsType(type))
                return handlerChain.read(buffer, reference, generics);
            else
                return readObject(buffer, type, reference, generics);
        }
    }
}
