package me.rfprojects.airy.resolver;

import com.airy.core.NioBuffer;
import com.airy.object.serializer.Serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static com.airy.object.support.InternalUtils.*;

public class MapResolver extends ObjectResolver {

    public MapResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Type referenceType, Object obj) {
        Class<?> clazz = obj.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            Type keyType = null, valueType = null;
            if (referenceType instanceof ParameterizedType) {
                Type[] typeArguments = ((ParameterizedType) referenceType).getActualTypeArguments();
                keyType = typeArguments[0];
                if (!(keyType instanceof Class) || keyType == Object.class)
                    keyType = null;
                valueType = typeArguments[1];
                if (!(valueType instanceof Class) || valueType == Object.class)
                    valueType = null;
            }
            boolean isKeyPrimitive = keyType != null && isPrimitive((Class<?>) keyType);
            boolean isValuePrimitive = valueType != null && isPrimitive((Class<?>) valueType);
            writeClassName(buffer, clazz);
            if (!isKeyPrimitive) {
                if (keyType != null)
                    writeClassName(buffer, (Class<?>) keyType);
                else
                    buffer.putString("");
            }
            if (!isValuePrimitive) {
                if (valueType != null)
                    writeClassName(buffer, (Class<?>) valueType);
                else
                    buffer.putString("");
            }

            Map<?, ?> map = (Map<?, ?>) obj;
            buffer.putUnsignedVarint(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                Class<?> keyClass = key.getClass();
                Class<?> valueClass = value.getClass();

                if (!isKeyPrimitive) {
                    if (keyClass != keyType)
                        writeClassName(buffer, keyClass);
                    else
                        buffer.putString("");
                }
                if (!isValuePrimitive) {
                    if (valueClass != valueType)
                        writeClassName(buffer, valueClass);
                    else
                        buffer.putString("");
                }
                serializer.serialize(buffer, key, false);
                serializer.serialize(buffer, value, false);
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
            if (referenceType instanceof Class && !Map.class.isAssignableFrom((Class<?>) referenceType))
                return null;
            else if (referenceType instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) referenceType;
                if (!Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
                    return null;
            }

            Type keyType = null, valueType = null;
            if (parameterizedType != null) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                keyType = typeArguments[0];
                if (!(keyType instanceof Class) || keyType == Object.class)
                    keyType = null;
                valueType = typeArguments[1];
                if (!(valueType instanceof Class) || valueType == Object.class)
                    valueType = null;
            }
            boolean isKeyPrimitive = keyType != null && isPrimitive((Class<?>) keyType);
            boolean isValuePrimitive = valueType != null && isPrimitive((Class<?>) valueType);

            Map map = (Map) readClass(buffer, null).newInstance();
            if (!isKeyPrimitive)
                keyType = readClass(buffer, (Class<?>) keyType);
            if (!isValuePrimitive)
                valueType = readClass(buffer, (Class<?>) valueType);
            int size = (int) buffer.getUnsignedVarint();
            for (int i = 0; i < size; i++) {
                Class keyClass = (Class) keyType;
                Class valueClass = (Class) valueType;
                if (!isKeyPrimitive)
                    keyClass = readClass(buffer, keyClass);
                if (!isValuePrimitive)
                    valueClass = readClass(buffer, valueClass);
                map.put(serializer.deserialize(buffer, keyClass), serializer.deserialize(buffer, valueClass));
            }
            return map;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
