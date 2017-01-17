package me.rfprojects.airy.handler.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;

import java.lang.reflect.Type;

public class BooleanHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.asByteBuffer().put((byte) ((boolean) object ? 1 : 0));
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return buffer.asByteBuffer().get() != 0;
    }
}
