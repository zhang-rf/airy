package me.rfprojects.airy.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
import java.nio.ReadOnlyBufferException;
import java.util.ArrayDeque;
import java.util.Deque;

public final class NioBuffer implements Comparable<NioBuffer> {

    private final boolean isWrapped;
    private ByteBuffer byteBuffer;
    private Deque<Integer> markDeque;

    private NioBuffer(ByteBuffer byteBuffer, boolean isWrapped) {
        this.byteBuffer = byteBuffer;
        this.isWrapped = isWrapped;
    }

    public static NioBuffer allocateDirect(int capacity) {
        return new NioBuffer(ByteBuffer.allocateDirect(capacity), false);
    }

    public static NioBuffer allocate(int capacity) {
        return new NioBuffer(ByteBuffer.allocate(capacity), false);
    }

    public static NioBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public static NioBuffer wrap(byte[] array, int offset, int length) {
        return new NioBuffer(ByteBuffer.wrap(array, offset, length), true);
    }

    public static int sizeofVarint(long value) {
        if (value < 0)
            value = (value << 1) ^ (value >> 63);
        int size = 1;
        while ((value >>>= 7) != 0)
            size++;
        return size;
    }

    public NioBuffer revert() {
        return revert(1);
    }

    public NioBuffer revert(int length) {
        position(byteBuffer.position() - length);
        return this;
    }

    public NioBuffer skip() {
        return skip(1);
    }

    public NioBuffer skip(int length) {
        position(byteBuffer.position() + length);
        return this;
    }

    public NioBuffer zero() {
        return zero(1);
    }

    public NioBuffer zero(int length) {
        for (int i = 0; i < length; i++)
            byteBuffer.put((byte) 0);
        return this;
    }

    public NioBuffer capacity(int newCapacity) {
        if (isWrapped)
            throw new UnsupportedOperationException();
        if (isReadOnly())
            throw new ReadOnlyBufferException();
        int oldCapacity = capacity();
        if (newCapacity < oldCapacity)
            throw new IllegalArgumentException();

        if (newCapacity != oldCapacity) {
            if (isDirect())
                resizeDirect(newCapacity);
            else
                resize(newCapacity);
        }
        return this;
    }

    public NioBuffer putBoolean(boolean value) {
        byteBuffer.put((byte) (value ? 1 : 0));
        return this;
    }

    public boolean getBoolean() {
        return byteBuffer.get() == 1;
    }

    public NioBuffer putUnsignedVarint(long value) {
        do {
            byte b = (byte) (value & 0x7f);
            if ((value >>>= 7) != 0)
                b |= 0x80;
            byteBuffer.put(b);
        } while (value != 0);
        return this;
    }

    public long getUnsignedVarint() {
        long value = 0;
        int shift = 0;
        do {
            byte b = byteBuffer.get();
            value |= (b & 0x7fL) << (7 * shift++);
            if ((b & 0x80) == 0)
                return value;
        } while (true);
    }

    public NioBuffer putVarint(long value) {
        return putUnsignedVarint((value << 1) ^ (value >> (Long.SIZE - 1)));
    }

    public long getVarint() {
        long value = getUnsignedVarint();
        return (-(value & 1)) ^ (value >>> 1);
    }

    public NioBuffer putString(String src) {
        return putString(src, "UTF-8");
    }

    public NioBuffer putString(String src, String charsetName) {
        try {
            if ("".equals(src))
                byteBuffer.put((byte) 0);
            else {
                byte[] bytes = src.getBytes(charsetName);
                putUnsignedVarint(bytes.length);
                byteBuffer.put(bytes);
            }
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString() {
        return getString("UTF-8");
    }

    public String getString(String charsetName) {
        try {
            int length = (int) getUnsignedVarint();
            if (length == 0)
                return "";
            else {
                if (hasArray()) {
                    skip(length);
                    return new String(byteBuffer.array(), byteBuffer.position() - length, length, charsetName);
                } else {
                    byte[] bytes = new byte[length];
                    byteBuffer.get(bytes);
                    return new String(bytes, charsetName);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public NioBuffer putFloat(float value) {
        byteBuffer.putFloat(value);
        return this;
    }

    public float getFloat() {
        return byteBuffer.getFloat();
    }

    public NioBuffer putDouble(double value) {
        byteBuffer.putDouble(value);
        return this;
    }

    public double getDouble() {
        return byteBuffer.getDouble();
    }

    public NioBuffer slice() {
        return new NioBuffer(byteBuffer.slice(), isWrapped);
    }

    public NioBuffer duplicate() {
        return new NioBuffer(byteBuffer.duplicate(), isWrapped);
    }

    public NioBuffer compact() {
        byteBuffer.compact();
        return this;
    }

    public NioBuffer asReadOnlyBuffer() {
        return new NioBuffer(byteBuffer.asReadOnlyBuffer(), isWrapped);
    }

    public ByteBuffer asByteBuffer() {
        return byteBuffer;
    }

    public int capacity() {
        return byteBuffer.capacity();
    }

    public int position() {
        return byteBuffer.position();
    }

    public NioBuffer position(int newPosition) {
        byteBuffer.position(newPosition);
        return this;
    }

    public int limit() {
        return byteBuffer.limit();
    }

    public NioBuffer limit(int newLimit) {
        byteBuffer.limit(newLimit);
        return this;
    }

    public NioBuffer mark() {
        if (markDeque == null)
            markDeque = new ArrayDeque<>();
        markDeque.push(position());
        return this;
    }

    public NioBuffer unmark() {
        if (markDeque == null || markDeque.isEmpty())
            throw new InvalidMarkException();
        markDeque.pop();
        return this;
    }

    public NioBuffer reset() {
        if (markDeque == null || markDeque.isEmpty())
            throw new InvalidMarkException();
        return position(markDeque.peek());
    }

    public NioBuffer empty() {
        clear();
        while (hasRemaining())
            byteBuffer.put((byte) 0);
        return clear();
    }

    public NioBuffer clear() {
        byteBuffer.clear();
        return this;
    }

    public NioBuffer flip() {
        byteBuffer.flip();
        return this;
    }

    public NioBuffer rewind() {
        byteBuffer.rewind();
        return this;
    }

    public int remaining() {
        return byteBuffer.remaining();
    }

    public boolean hasRemaining() {
        return byteBuffer.hasRemaining();
    }

    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }

    public boolean hasArray() {
        return byteBuffer.hasArray();
    }

    public byte[] array() {
        return byteBuffer.array();
    }

    public int arrayOffset() {
        return byteBuffer.arrayOffset();
    }

    public boolean isDirect() {
        return byteBuffer.isDirect();
    }

    @Override
    public String toString() {
        return byteBuffer.toString();
    }

    @Override
    public int hashCode() {
        return byteBuffer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof NioBuffer && byteBuffer.equals(((NioBuffer) obj).asByteBuffer()));
    }

    @Override
    public int compareTo(NioBuffer that) {
        return byteBuffer.compareTo(that.asByteBuffer());
    }

    private Field getField(Class<?> clazz, String name) {
        Field field = null;
        do {
            try {
                field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
        return field;
    }

    private void resizeDirect(int capacity) {
        try {
            Field markField = Buffer.class.getDeclaredField("mark");
            markField.setAccessible(true);

            ByteBuffer newBuffer = ByteBuffer.allocateDirect(capacity);
            int position = byteBuffer.position();
            newBuffer.put((ByteBuffer) byteBuffer.position(0).limit(position)).position(position);
            markField.set(newBuffer, markField.get(byteBuffer));
            byteBuffer = newBuffer;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void resize(int capacity) {
        try {
            byte[] array = byteBuffer.array();
            Class<?> clazz = byteBuffer.getClass();
            Field arrayField = getField(clazz, "hb");
            if (arrayField == null || arrayField.get(byteBuffer) != array)
                throw new NoSuchFieldException("array");
            Field capacityField = getField(clazz, "capacity");
            if (capacityField == null)
                throw new NoSuchFieldException("capacity");

            byte[] newArray = new byte[capacity];
            System.arraycopy(array, 0, newArray, 0, byteBuffer.position());
            arrayField.set(byteBuffer, newArray);
            capacityField.set(byteBuffer, newArray.length);
            byteBuffer.limit(newArray.length);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
