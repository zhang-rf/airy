package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.util.TimeZone;

public class TimeZoneHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == TimeZone.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putString(((TimeZone) object).getID());
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return TimeZone.getTimeZone(buffer.getString());
    }
}
