package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class BytesHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == byte[].class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        byte[] bytes = (byte[]) object;
        buffer.putUnsignedVarint(bytes.length);
        buffer.asByteBuffer().put(bytes);
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
        buffer.asByteBuffer().get(bytes);
        return bytes;
    }
}
