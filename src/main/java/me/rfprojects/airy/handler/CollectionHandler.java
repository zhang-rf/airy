package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;

public class CollectionHandler implements Handler {

    private ClassRegistry registry;
    private Serializer serializer;

    public CollectionHandler(ClassRegistry registry, Serializer serializer) {
        this.registry = registry;
        this.serializer = serializer;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Class<?> componentType = null;
        boolean isFinalType = false;
        if (generics.length == 1 && generics[0] instanceof Class) {
            componentType = (Class<?>) generics[0];
            isFinalType = Modifier.isFinal(componentType.getModifiers());
        }

        Collection<?> collection = (Collection<?>) object;
        registry.writeClass(buffer, collection.getClass());
        buffer.putUnsignedVarint(collection.size());

        boolean containsNull = collection.contains(null);
        buffer.putBoolean(containsNull);
        for (Object item : collection) {
            if (item == null)
                registry.writeClass(buffer, Collection.class);
            else {
                if (!isFinalType || containsNull) {
                    Class<?> type = item.getClass();
                    registry.writeClass(buffer, type != componentType ? type : null);
                }
                serializer.serialize(buffer, item, false, null);
            }
        }
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

            Class<?> collectionType = registry.readClass(buffer, null);
            int size = (int) buffer.getUnsignedVarint();
            Collection collection;
            try {
                collection = (Collection) collectionType.getConstructor(int.class).newInstance(size);
            } catch (NoSuchMethodException ignored) {
                collection = (Collection) collectionType.newInstance();
            }

            boolean containsNull = buffer.getBoolean();
            for (int i = 1; i <= size; i++) {
                Class<?> type = componentType;
                if (!isFinal || containsNull)
                    type = registry.readClass(buffer, componentType);
                collection.add(type != Collection.class ? serializer.deserialize(buffer, type, null) : null);
            }
            return collection;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
