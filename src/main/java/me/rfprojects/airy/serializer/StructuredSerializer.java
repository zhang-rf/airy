package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Field;

public interface StructuredSerializer extends Serializer {

    FieldAccessor[] getAccessors(NioBuffer buffer, Class<?> type);

    FieldAccessor getAccessor(NioBuffer buffer, Class<?> type, String name);

    interface FieldAccessor {

        Field getField();

        int getAddress();

        Object accessValue(NioBuffer buffer);
    }
}
