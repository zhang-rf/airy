package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import static me.rfprojects.airy.internal.ClassUtil.readClass;
import static me.rfprojects.airy.internal.ClassUtil.writeClassName;
import static me.rfprojects.airy.internal.Misc.isPrimitive;
import static me.rfprojects.airy.internal.Null.isNull;

public class ArrayResolver extends ObjectResolver {

    private static ObjectResolver byteArrayResolver = new ByteArrayResolver();

    public ArrayResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
        if (obj.getClass() == byte[].class)
            return byteArrayResolver.writeObject(buffer, obj, referenceType, genericTypes);

        Class<?> clazz = obj.getClass();
        if (skipCheck() || clazz.isArray()) {
            Class<?> componentType = getComponentType(clazz);
            writeClassName(buffer, componentType, serializer.getRegistry(), clazz != referenceType);

            Object array = obj;
            do {
                int length = Array.getLength(array);
                buffer.putUnsignedVarint(length);

                for (int i = 0; i < length; i++) {
                    array = Array.get(array, i);
                    if (array != null)
                        break;
                }
            } while (array != null && array.getClass().isArray());
            deepIterate(buffer, obj, 1, componentType);
            buffer.asByteBuffer().put((byte) 0);
            return true;
        }
        return false;
    }

    private Class<?> getComponentType(Class<?> type) {
        while (type.isArray())
            type = type.getComponentType();
        return type;
    }

    private int deepIterate(NioBuffer buffer, Object array, int indexer, Class<?> componentType) {
        boolean isArray = array.getClass().getComponentType().isArray();
        boolean isPrimitive = !isArray && isPrimitive(componentType, serializer.getRegistry());
        for (int i = 0, length = Array.getLength(array); i < length; i++, indexer++) {
            if (isArray) {
                Object subArray = Array.get(array, i);
                if (subArray != null)
                    indexer = deepIterate(buffer, subArray, indexer, componentType);
            } else {
                Object value = Array.get(array, i);
                if (!isNull(value)) {
                    buffer.putUnsignedVarint(indexer);
                    if (!isPrimitive) {
                        Class<?> clazz = value.getClass();
                        writeClassName(buffer, value.getClass(), serializer.getRegistry(), clazz != componentType);
                    }
                    serializer.serialize(buffer, value, false);
                }
            }
        }
        return indexer - 1;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        if (referenceType == byte[].class)
            return byteArrayResolver.readObject(buffer, referenceType, genericTypes);

        if (!skipCheck() && !referenceType.isArray())
            return null;

        int dimension = 1;
        Class<?> componentType = referenceType;
        while ((componentType = componentType.getComponentType()).isArray())
            dimension++;

        componentType = readClass(buffer, componentType, serializer.getRegistry());
        int[] dimensions = new int[dimension];
        for (int i = 0; i < dimension; i++)
            dimensions[i] = (int) buffer.getUnsignedVarint();
        Object array = Array.newInstance(componentType, dimensions);
        boolean isPrimitive = isPrimitive(componentType, serializer.getRegistry());

        int indexer;
        while ((indexer = (int) buffer.getUnsignedVarint()) != 0) {
            Class<?> clazz = isPrimitive ? componentType : readClass(buffer, componentType, serializer.getRegistry());
            setArray(array, dimensions, indexer, serializer.deserialize(buffer, clazz));
        }
        return array;
    }

    private void setArray(Object array, int[] dimensions, int indexer, Object value) {
        int dimension = dimensions.length;
        int[] indexes = new int[dimension];

        for (int i = 0; i < dimension; i++) {
            double factor = 1.0;
            for (int f = dimension - 1; f > i; f--)
                factor *= dimensions[f];
            indexes[i] = (int) Math.ceil(indexer / factor);
            indexer -= (indexes[i] - 1) * factor;
        }

        for (int i = 0; i < dimension - 1; i++)
            array = Array.get(array, indexes[i] - 1);
        Array.set(array, indexes[dimension - 1] - 1, value);
    }

    private static class ByteArrayResolver extends ObjectResolver {

        public ByteArrayResolver() {
            super(null);
        }

        @Override
        public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
            byte[] bytes = (byte[]) obj;
            buffer.putUnsignedVarint(bytes.length);
            buffer.asByteBuffer().put(bytes);
            return true;
        }

        @Override
        public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
            byte[] bytes = new byte[(int) buffer.getUnsignedVarint()];
            buffer.asByteBuffer().get(bytes);
            return bytes;
        }
    }
}
