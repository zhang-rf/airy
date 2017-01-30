package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.internal.ReflectionUtils;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class ArrayHandler implements Handler {

    private ClassRegistry registry;
    private Serializer serializer;

    public ArrayHandler(ClassRegistry registry, Serializer serializer) {
        this.registry = registry;
        this.serializer = serializer;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return type != null && type.isArray();
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        boolean isArrayReference = ReflectionUtils.isArray(reference);
        reference = ReflectionUtils.getComponentType(reference);
        Class<?> componentType = ReflectionUtils.getComponentType(object.getClass());
        if (reference == null || !isArrayReference || (!reference.isPrimitive() && !Modifier.isFinal(reference.getModifiers())))
            registry.writeClass(buffer, componentType != reference ? componentType : null);

        Object array = object;
        if (!isArrayReference) {
            int dimension = 1;
            Class<?> nextClass = array.getClass();
            while ((nextClass = nextClass.getComponentType()).isArray())
                dimension++;
            buffer.putUnsignedVarint(dimension);
        }
        do {
            int length = Array.getLength(array);
            buffer.putUnsignedVarint(length);

            for (int i = 0; i < length; i++) {
                array = Array.get(array, i);
                if (array != null)
                    break;
            }
        } while (array != null && array.getClass().isArray());
        deepIterate(buffer, object, 1, componentType);
        buffer.asByteBuffer().put((byte) 0);
    }

    private int deepIterate(NioBuffer buffer, Object array, int indexer, Class<?> componentType) {
        boolean isArray = array.getClass().getComponentType().isArray();
        boolean isFinal = !isArray && (componentType.isPrimitive() || Modifier.isFinal(componentType.getModifiers()));
        for (int i = 0, length = Array.getLength(array); i < length; i++, indexer++) {
            if (isArray) {
                Object subArray = Array.get(array, i);
                if (subArray != null)
                    indexer = deepIterate(buffer, subArray, indexer, componentType);
            } else {
                Object value = Array.get(array, i);
                if (value != Null.get(componentType)) {
                    buffer.putUnsignedVarint(indexer);
                    if (!isFinal) {
                        Class<?> type = value.getClass();
                        registry.writeClass(buffer, type != componentType ? type : null);
                    }
                    serializer.serialize(buffer, value, false, null);
                }
            }
        }
        return indexer - 1;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        boolean isArrayReference = ReflectionUtils.isArray(reference);
        Class<?> componentType = ReflectionUtils.getComponentType(reference);
        if (reference == null || !isArrayReference || (!reference.isPrimitive() && !Modifier.isFinal(reference.getModifiers())))
            componentType = registry.readClass(buffer, componentType);

        int dimension = 1;
        if (!isArrayReference)
            dimension = (int) buffer.getUnsignedVarint();
        else {
            while ((reference = reference.getComponentType()).isArray())
                dimension++;
        }
        int[] dimensions = new int[dimension];
        for (int i = 0; i < dimension; i++)
            dimensions[i] = (int) buffer.getUnsignedVarint();

        int indexer;
        Object array = Array.newInstance(componentType, dimensions);
        boolean isFinal = componentType.isPrimitive() || Modifier.isFinal(componentType.getModifiers());
        while ((indexer = (int) buffer.getUnsignedVarint()) > 0) {
            Class<?> type = isFinal ? componentType : registry.readClass(buffer, componentType);
            setArray(array, dimensions, indexer, serializer.deserialize(buffer, type, null));
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
}
