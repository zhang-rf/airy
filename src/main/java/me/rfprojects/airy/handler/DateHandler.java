package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.util.Date;

public class DateHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == Date.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putUnsignedVarint(((Date) object).getTime());
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return new Date(buffer.getUnsignedVarint());
    }
}
