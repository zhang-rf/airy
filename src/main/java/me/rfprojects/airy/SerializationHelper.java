package me.rfprojects.airy;

import com.airy.object.serializer.Serializer;
import com.airy.object.serializer.StructuredSerializer;
import com.airy.util.ThreadLocalReference;
import me.rfprojects.airy.core.NioBuffer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SerializationHelper {

    private static ThreadLocalReference<NioBuffer> sharedBuffer = new ThreadLocalReference<NioBuffer>() {

        @Override
        protected Reference<NioBuffer> initialValue() {
            return new WeakReference<>(NioBuffer.allocate(8192));
        }
    };
    private static ConcurrentMap<Class<? extends Serializer>, SerializationHelper> helperMap = new ConcurrentHashMap<>();

    private Serializer serializer;

    private SerializationHelper(Serializer serializer) {
        this.serializer = serializer;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static SerializationHelper getHelper(Class<? extends Serializer> serializerClass) {
        try {
            if (!helperMap.containsKey(serializerClass)) {
                synchronized (serializerClass) {
                    if (!helperMap.containsKey(serializerClass))
                        helperMap.put(serializerClass, new SerializationHelper(serializerClass.newInstance()));
                }
            }
            return helperMap.get(serializerClass);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] serialize(Object obj) {
        NioBuffer buffer = sharedBuffer.get();
        int length = serialize(buffer, obj);
        return Arrays.copyOf(buffer.array(), length);
    }

    public int serialize(Object obj, byte[] dest) {
        return serialize(obj, dest, 0, dest.length);
    }

    public int serialize(Object obj, byte[] dest, int offset, int length) {
        return serialize(NioBuffer.wrap(dest, offset, length), obj);
    }

    private int serialize(NioBuffer buffer, Object obj) {
        try {
            if (serializer instanceof StructuredSerializer)
                ((StructuredSerializer) serializer).beforeSerializing();
            serializer.serialize(buffer, obj, true);
            return buffer.position() - buffer.arrayOffset();
        } catch (BufferOverflowException e) {
            return serialize(buffer.clear().capacity(buffer.capacity() * 2), obj);
        } finally {
            buffer.clear();
        }
    }

    public Object deserialize(byte[] bytes) {
        return deserialize(bytes, 0, bytes.length);
    }

    public Object deserialize(byte[] bytes, int offset, int length) {
        return deserialize(NioBuffer.wrap(bytes, offset, length));
    }

    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return clazz.cast(deserialize(bytes));
    }

    public <T> T deserialize(byte[] bytes, int offset, int length, Class<T> clazz) {
        return clazz.cast(deserialize(bytes, offset, length));
    }

    private Object deserialize(NioBuffer buffer) {
        if (serializer instanceof StructuredSerializer)
            ((StructuredSerializer) serializer).beforeDeserializing();
        return serializer.deserialize(buffer, null);
    }
}
