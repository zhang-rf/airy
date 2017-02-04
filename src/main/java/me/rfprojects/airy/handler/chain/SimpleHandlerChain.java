package me.rfprojects.airy.handler.chain;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleHandlerChain implements HandlerChain {

    private Set<Handler> handlerSet = new CopyOnWriteArraySet<>();
    private ConcurrentMap<Class<?>, Handler> handlerMap = new ConcurrentHashMap<>();
    private ThreadLocal<Handler> currentHandler = new ThreadLocal<Handler>() {
        @Override
        protected Handler initialValue() {
            throw new UnsupportedOperationException();
        }
    };

    @Override
    public boolean appendHandler(Handler handler) {
        boolean appended = false;
        if (handler != null) {
            appended = handlerSet.add(handler);
            if (appended)
                handlerMap.clear();
        }
        return appended;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        try {
            if (type == null || type == Object.class)
                return false;

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
        } finally {
            currentHandler.remove();
            if (type != null && type != Object.class) {
                Handler handler = handlerMap.get(type);
                if (handler != null && handler != this)
                    currentHandler.set(handler);
            }
        }
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        currentHandler.get().write(buffer, object, reference, generics);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return currentHandler.get().read(buffer, reference, generics);
    }
}
