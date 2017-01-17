package me.rfprojects.airy.handler.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.Handler;

import java.lang.reflect.Type;

public class CharacterHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == char.class || type == Character.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putVarint((char) object);
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return (char) buffer.getVarint();
    }
}
