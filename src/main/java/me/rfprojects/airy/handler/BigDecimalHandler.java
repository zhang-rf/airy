package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BigDecimalHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == BigDecimal.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        BigDecimal bigDecimal = (BigDecimal) object;
        byte[] bytes = bigDecimal.unscaledValue().toByteArray();
        buffer.putUnsignedVarint(bytes.length).asByteBuffer().put(bytes);
        if (!Objects.equals(bigDecimal, BigDecimal.ZERO) && !Objects.equals(bigDecimal, BigDecimal.ONE) && !Objects.equals(bigDecimal, BigDecimal.TEN))
            buffer.putVarint(bigDecimal.scale());
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
        buffer.asByteBuffer().get(bytes);
        if (bytes.length == 1) {
            switch (bytes[0]) {
                case 0:
                    return BigDecimal.ZERO;
                case 1:
                    return BigDecimal.ONE;
                case 10:
                    return BigDecimal.TEN;
            }
        }
        return new BigDecimal(new BigInteger(bytes), (int) buffer.getVarint());
    }
}
