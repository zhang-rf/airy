package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PrimitiveResolver extends ObjectResolver {

    private static Map<Class<?>, ObjectResolver> subResolvers = new HashMap<>();

    static {
        ObjectResolver subResolver;
        subResolver = new BooleanResolver();
        subResolvers.put(boolean.class, subResolver);
        subResolvers.put(Boolean.class, subResolver);
        subResolver = new CharacterResolver();
        subResolvers.put(char.class, subResolver);
        subResolvers.put(Character.class, subResolver);
        subResolver = new ByteResolver();
        subResolvers.put(byte.class, subResolver);
        subResolvers.put(Byte.class, subResolver);
        subResolver = new ShortResolver();
        subResolvers.put(short.class, subResolver);
        subResolvers.put(Short.class, subResolver);
        subResolver = new IntegerResolver();
        subResolvers.put(int.class, subResolver);
        subResolvers.put(Integer.class, subResolver);
        subResolver = new LongResolver();
        subResolvers.put(long.class, subResolver);
        subResolvers.put(Long.class, subResolver);
        subResolver = new FloatResolver();
        subResolvers.put(float.class, subResolver);
        subResolvers.put(Float.class, subResolver);
        subResolver = new DoubleResolver();
        subResolvers.put(double.class, subResolver);
        subResolvers.put(Double.class, subResolver);
    }

    public PrimitiveResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
        ObjectResolver resolver = subResolvers.get(obj.getClass());
        return resolver != null && resolver.writeObject(buffer, obj, referenceType, genericTypes);
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        ObjectResolver resolver = subResolvers.get(referenceType);
        return resolver != null ? resolver.readObject(buffer, referenceType, genericTypes) : null;
    }

    private static class BooleanResolver extends ObjectResolver {

        public BooleanResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.asByteBuffer().put((byte) ((boolean) obj ? 1 : 0));
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return buffer.asByteBuffer().get() != 0;
        }
    }

    private static class CharacterResolver extends ObjectResolver {

        public CharacterResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putVarint((char) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return (char) buffer.getVarint();
        }
    }

    private static class ByteResolver extends ObjectResolver {

        public ByteResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putVarint((byte) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return (byte) buffer.getVarint();
        }
    }

    private static class ShortResolver extends ObjectResolver {

        public ShortResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putVarint((short) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return (short) buffer.getVarint();
        }
    }

    private static class IntegerResolver extends ObjectResolver {

        public IntegerResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putVarint((int) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return (int) buffer.getVarint();
        }
    }

    private static class LongResolver extends ObjectResolver {

        public LongResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putVarint((long) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return buffer.getVarint();
        }
    }

    private static class FloatResolver extends ObjectResolver {

        public FloatResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putFloat((float) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return buffer.getFloat();
        }
    }

    private static class DoubleResolver extends ObjectResolver {

        public DoubleResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            buffer.putDouble((double) obj);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            return buffer.getDouble();
        }
    }
}
