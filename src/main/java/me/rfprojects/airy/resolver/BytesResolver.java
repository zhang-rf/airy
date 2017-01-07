package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class BytesResolver implements Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return type == byte[].class;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        byte[] bytes = (byte[]) object;
        buffer.putUnsignedVarint(bytes.length);
        buffer.asByteBuffer().put(bytes);
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
        buffer.asByteBuffer().get(bytes);
        return bytes;
    }
}
