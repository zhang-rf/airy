package me.rfprojects.airy.handler.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;

import java.lang.reflect.Type;

public class IntegerHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == int.class || type == Integer.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putVarint((int) object);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return (int) buffer.getVarint();
    }
}
