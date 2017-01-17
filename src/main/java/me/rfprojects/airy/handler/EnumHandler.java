package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class EnumHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type != null && type.isEnum();
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putUnsignedVarint(((Enum) object).ordinal());
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return reference.getEnumConstants()[(int) buffer.getUnsignedVarint()];
    }
}
