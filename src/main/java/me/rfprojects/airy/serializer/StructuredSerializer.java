package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Field;

public interface StructuredSerializer extends Serializer {

    RandomAccessor[] getAccessors(NioBuffer buffer, Class<?> type);

    RandomAccessor getAccessor(NioBuffer buffer, Class<?> type, String name);

    interface RandomAccessor {

        int getAddress();

        Field getField();

        Object accessValue(NioBuffer buffer);
    }
}
