package me.rfprojects.airy;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;
import me.rfprojects.airy.util.LocalReference;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.util.Arrays;

public class Airy {

    private static LocalReference<NioBuffer> bufferReference = new LocalReference<NioBuffer>() {
        @Override
        protected Reference<NioBuffer> initialValue() {
            return new WeakReference<>(NioBuffer.allocate(8192));
        }
    };
    private Serializer serializer;

    public Airy(Serializer serializer) {
        this.serializer = serializer;
    }

    public byte[] serialize(Object object) {
        NioBuffer buffer = bufferReference.get();
        int length = serialize(buffer, object, true);
        return Arrays.copyOf(buffer.array(), length);
    }

    public int serialize(Object object, byte[] dest) {
        return serialize(object, dest, 0, dest.length);
    }

    public int serialize(Object object, byte[] dest, int offset, int length) {
        return serialize(NioBuffer.wrap(dest, offset, length), object, true);
    }

    private int serialize(NioBuffer buffer, Object object, boolean writeClass) {
        try {
            serializer.serialize(buffer, object, writeClass);
            return buffer.position() - buffer.arrayOffset();
        } catch (BufferOverflowException e) {
            return serialize(buffer.clear().capacity(buffer.capacity() * 2), object, writeClass);
        } finally {
            buffer.clear();
        }
    }

    public Object deserialize(byte[] bytes) {
        return deserialize(bytes, null);
    }

    public <T> T deserialize(byte[] bytes, Class<T> type) {
        return deserialize(bytes, 0, bytes.length, type);
    }

    public Object deserialize(byte[] bytes, int offset, int length) {
        return deserialize(bytes, offset, length, null);
    }

    public <T> T deserialize(byte[] bytes, int offset, int length, Class<T> type) {
        return serializer.deserialize(NioBuffer.wrap(bytes, offset, length), type);
    }
}
