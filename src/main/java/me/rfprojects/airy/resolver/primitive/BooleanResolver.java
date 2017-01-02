package me.rfprojects.airy.resolver.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.Resolver;

import java.lang.reflect.Type;

public class BooleanResolver implements Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.asByteBuffer().put((byte) ((boolean) object ? 1 : 0));
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        return buffer.asByteBuffer().get() != 0;
    }
}
