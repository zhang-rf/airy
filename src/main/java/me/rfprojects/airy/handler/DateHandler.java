package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class DateHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return Date.class.isAssignableFrom(type);
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Class<?> type = object.getClass();
        if (type == Timestamp.class)
            buffer.asByteBuffer().put((byte) 1);
        else if (type == java.sql.Date.class)
            buffer.asByteBuffer().put((byte) 2);
        else if (type == Time.class)
            buffer.asByteBuffer().put((byte) 3);
        else
            buffer.asByteBuffer().put((byte) 0);
        buffer.putUnsignedVarint(((Date) object).getTime());
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        switch (buffer.asByteBuffer().get()) {
            case 1:
                return new Timestamp(buffer.getUnsignedVarint());
            case 2:
                return new java.sql.Date(buffer.getUnsignedVarint());
            case 3:
                return new Time(buffer.getUnsignedVarint());
            default:
                return new Date(buffer.getUnsignedVarint());
        }
    }
}
