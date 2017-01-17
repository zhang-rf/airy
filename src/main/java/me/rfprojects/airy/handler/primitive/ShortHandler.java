package me.rfprojects.airy.handler.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;

import java.lang.reflect.Type;

public class ShortHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == short.class || type == Short.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putVarint((short) object);
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return (short) buffer.getVarint();
    }
}
