package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class CollectionHandler implements Handler {

    private Serializer serializer;

    public CollectionHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Collection<?> collection = (Collection<?>) object;
        buffer.putUnsignedVarint(collection.size());
        serializer.registry().writeClass(buffer, object.getClass());

        Class<?> componentType = null;
        boolean isFinal = false;
        if (generics.length == 1 && generics[0] instanceof Class) {
            componentType = (Class<?>) generics[0];
            isFinal = Modifier.isFinal(componentType.getModifiers());
        }

        buffer.mark().asByteBuffer().putInt(-1);
        List<Integer> nullList = new ArrayList<>();
        int index = 1;
        for (Object item : collection) {
            if (item == null)
                nullList.add(index);
            else {
                if (!isFinal) {
                    Class<?> type = item.getClass();
                    serializer.registry().writeClass(buffer, type != componentType ? type : null);
                }
                serializer.serialize(buffer, item, false);
            }
            index++;
        }

        if (!nullList.isEmpty()) {
            int nullsAddress = buffer.position();
            for (int i : nullList)
                buffer.putUnsignedVarint(i);
            buffer.asByteBuffer().put((byte) 0);
            int position = buffer.position();
            buffer.reset().asByteBuffer().putInt(nullsAddress).position(position);
        }
        buffer.unmark();
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        try {
            Class<?> componentType = null;
            boolean isFinal = false;
            if (generics.length == 1 && generics[0] instanceof Class) {
                componentType = (Class<?>) generics[0];
                isFinal = Modifier.isFinal(componentType.getModifiers());
            }

            int size = (int) buffer.getUnsignedVarint();
            Class<?> collectionType = serializer.registry().readClass(buffer, null);
            Collection collection;
            try {
                collection = (Collection) collectionType.getConstructor(int.class).newInstance(size);
            } catch (NoSuchMethodException ignored) {
                collection = (Collection) collectionType.newInstance();
            }

            int nullsAddress = buffer.asByteBuffer().getInt();
            Queue<Integer> nullQueue = null;
            if (nullsAddress > 0) {
                nullQueue = new ArrayDeque<>();
                buffer.mark().position(nullsAddress);
                int index;
                while ((index = (int) buffer.getUnsignedVarint()) != 0)
                    nullQueue.add(index);
                buffer.reset().unmark();
            }

            for (int i = 1; i <= size; i++) {
                if (nullQueue != null && !nullQueue.isEmpty() && nullQueue.peek() == i) {
                    collection.add(null);
                    nullQueue.remove();
                } else {
                    Class<?> type = componentType;
                    if (!isFinal)
                        type = serializer.registry().readClass(buffer, componentType);
                    collection.add(serializer.deserialize(buffer, type));
                }
            }
            return collection;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
