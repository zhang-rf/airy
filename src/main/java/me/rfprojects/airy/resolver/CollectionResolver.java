package me.rfprojects.airy.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import static com.airy.object.support.InternalUtils.*;

public class CollectionResolver extends ObjectResolver {

    public CollectionResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        Class<?> clazz = obj.getClass();
        if (Collection.class.isAssignableFrom(clazz)) {
            Type componentType = null;
            if (referenceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) referenceType;
                componentType = parameterizedType.getActualTypeArguments()[0];
                if (!(componentType instanceof Class) || componentType == Object.class)
                    componentType = null;
            }
            boolean isPrimitive = componentType != null && isPrimitive((Class<?>) componentType);
            writeClassName(buffer, clazz);
            if (!isPrimitive) {
                if (componentType != null)
                    writeClassName(buffer, (Class<?>) componentType);
                else
                    buffer.putString("");
            }

            Collection<?> collection = (Collection<?>) obj;
            buffer.putUnsignedVarint(collection.size());
            for (Object item : collection) {
                Class<?> itemClass = item.getClass();
                if (!isPrimitive) {
                    if (itemClass != componentType)
                        writeClassName(buffer, itemClass);
                    else
                        buffer.putString("");
                }
                serializer.serialize(buffer, item, false);
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object readObject(NioBuffer buffer, Type referenceType) {
        try {
            if (referenceType == null)
                return null;

            ParameterizedType parameterizedType = null;
            if (referenceType instanceof Class && !Collection.class.isAssignableFrom((Class<?>) referenceType))
                return null;
            else if (referenceType instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) referenceType;
                if (!Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
                    return null;
            }

            Type componentType = null;
            if (parameterizedType != null) {
                componentType = parameterizedType.getActualTypeArguments()[0];
                if (!(componentType instanceof Class))
                    componentType = null;
            }
            boolean isPrimitive = componentType != null && isPrimitive((Class<?>) componentType);

            Collection collection = (Collection) readClass(buffer, null).newInstance();
            if (!isPrimitive)
                componentType = readClass(buffer, (Class<?>) componentType);
            int size = (int) buffer.getUnsignedVarint();
            for (int i = 0; i < size; i++) {
                if (isPrimitive)
                    collection.add(serializer.deserialize(buffer, (Class<?>) componentType));
                else
                    collection.add(serializer.deserialize(buffer, readClass(buffer, (Class<?>) componentType)));
            }
            return collection;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
