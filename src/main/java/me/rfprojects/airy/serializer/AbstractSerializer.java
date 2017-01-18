package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.EnumHandler;
import me.rfprojects.airy.handler.Handler;
import me.rfprojects.airy.handler.StringHandler;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.handler.chain.SimpleHandlerChain;
import me.rfprojects.airy.handler.primitive.*;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class AbstractSerializer implements Serializer {

    private ClassRegistry registry;
    private HandlerChain handlerChain;
    private HandlerChain wrapperHandlerChain = new WrapperHandlerChain();

    public AbstractSerializer() {
        this(new ClassRegistry());
    }

    public AbstractSerializer(ClassRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
        handlerChain = new SimpleHandlerChain();
        handlerChain.appendHandler(new BooleanHandler());
        handlerChain.appendHandler(new CharacterHandler());
        handlerChain.appendHandler(new ByteHandler());
        handlerChain.appendHandler(new ShortHandler());
        handlerChain.appendHandler(new IntegerHandler());
        handlerChain.appendHandler(new LongHandler());
        handlerChain.appendHandler(new FloatHandler());
        handlerChain.appendHandler(new DoubleHandler());
        handlerChain.appendHandler(new StringHandler());
        handlerChain.appendHandler(new EnumHandler());
    }

    public AbstractSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        this.registry = Objects.requireNonNull(registry);
        this.handlerChain = Objects.requireNonNull(handlerChain);
    }

    protected abstract void writeObject(NioBuffer buffer, Object object);

    protected abstract Object readObject(NioBuffer buffer, Class<?> type);

    @Override
    public ClassRegistry registry() {
        return registry;
    }

    @Override
    public HandlerChain handlerChain() {
        return wrapperHandlerChain;
    }

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClass) {
        Class<?> type = object.getClass();
        registry.writeClass(buffer, writeClass ? type : null);
        if (handlerChain.supportsType(type))
            handlerChain.write(buffer, object, null);
        else
            writeObject(buffer, object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(NioBuffer buffer, Class<T> type) {
        type = (Class<T>) registry.readClass(buffer, type);
        if (type == null)
            throw new UnknownClassException();
        if (handlerChain.supportsType(type))
            return (T) handlerChain.read(buffer, type);
        else
            return (T) readObject(buffer, type);
    }

    private class WrapperHandlerChain implements HandlerChain {

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
            Class<?> type = object.getClass();
            if (handlerChain.supportsType(reference)) {
                if (reference == Object.class)
                    registry.writeClass(buffer, type);
                handlerChain.write(buffer, object, reference, generics);
            } else {
                registry().writeClass(buffer, type != reference ? type : null);
                writeObject(buffer, object);
            }
        }

        @Override
        public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
            boolean classRead = false;
            if (reference == Object.class) {
                reference = registry.readClass(buffer, null);
                classRead = true;
            }
            if (handlerChain.supportsType(reference))
                return handlerChain.read(buffer, reference, generics);
            else {
                if (!classRead)
                    reference = registry.readClass(buffer, reference);
                return readObject(buffer, reference);
            }
        }
    }
}
