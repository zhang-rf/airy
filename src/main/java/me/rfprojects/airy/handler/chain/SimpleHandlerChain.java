package me.rfprojects.airy.handler.chain;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;
import me.rfprojects.airy.handler.NoHandlerSupportsException;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleHandlerChain implements HandlerChain {

    private Set<Handler> handlerSet = new CopyOnWriteArraySet<>();
    private ConcurrentMap<Class<?>, Handler> handlerMap = new ConcurrentHashMap<>();

    public void appendHandler(Handler handler) {
        handlerSet.add(handler);
        handlerMap.clear();
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
        if (!supportsType(reference))
            throw new NoHandlerSupportsException();
        handlerMap.get(reference).write(buffer, object, reference, generics);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        if (!supportsType(reference))
            throw new NoHandlerSupportsException();
        return handlerMap.get(reference).read(buffer, reference, generics);
    }
}
