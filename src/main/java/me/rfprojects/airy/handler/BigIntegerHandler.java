package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.math.BigInteger;

public class BigIntegerHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == BigInteger.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        byte[] bytes = ((BigInteger) object).toByteArray();
        buffer.putUnsignedVarint(bytes.length).asByteBuffer().put(bytes);
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
        buffer.asByteBuffer().get(bytes);
        if (bytes.length == 1) {
            switch (bytes[0]) {
                case 0:
                    return BigInteger.ZERO;
                case 1:
                    return BigInteger.ONE;
                case 10:
                    return BigInteger.TEN;
            }
        }
        return new BigInteger(bytes);
    }
}
