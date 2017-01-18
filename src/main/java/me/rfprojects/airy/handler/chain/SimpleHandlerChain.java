package me.rfprojects.airy.handler.chain;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;
import me.rfprojects.airy.handler.NoHandlerSupportsException;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleHandlerChain implements HandlerChain {

    private Set<Handler> handlerSet = new CopyOnWriteArraySet<>();
    private ConcurrentMap<Class<?>, Handler> handlerMap = new ConcurrentHashMap<>();

    public boolean appendHandler(Handler handler) {
        Objects.requireNonNull(handler);
        boolean appended = handlerSet.add(handler);
        if (appended)
            handlerMap.clear();
        return appended;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        Handler mappedHandler = handlerMap.get(type);
        if (mappedHandler != null)
            return mappedHandler != this;
        else {
            for (Handler handler : handlerSet) {
                if (handler.supportsType(type)) {
                    handlerMap.put(type, handler);
                    return true;
                }
            }
            handlerMap.put(type, this);
            return false;
        }
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Class<?> type = reference != null ? reference : object.getClass();
        if (!supportsType(type))
            throw new NoHandlerSupportsException();
        handlerMap.get(type).write(buffer, object, reference, generics);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        if (!supportsType(reference))
            throw new NoHandlerSupportsException();
        return handlerMap.get(reference).read(buffer, reference, generics);
    }
}
