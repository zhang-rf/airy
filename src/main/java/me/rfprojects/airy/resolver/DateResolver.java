package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.util.Date;

public class DateResolver implements Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return type == Date.class;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putUnsignedVarint(((Date) object).getTime());
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        return new Date(buffer.getUnsignedVarint());
    }
}
