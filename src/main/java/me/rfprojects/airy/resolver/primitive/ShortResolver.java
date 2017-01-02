package me.rfprojects.airy.resolver.primitive;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.Resolver;

import java.lang.reflect.Type;

public class ShortResolver implements Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return type == short.class || type == Short.class;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putVarint((short) object);
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        return (short) buffer.getVarint();
    }
}
