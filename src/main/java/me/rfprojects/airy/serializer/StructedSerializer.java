package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.resolver.*;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.rfprojects.airy.internal.ClassUtil.readClass;
import static me.rfprojects.airy.internal.ClassUtil.writeClassName;
import static me.rfprojects.airy.internal.Null.isNull;

public class StructedSerializer implements Serializer {

    private static final Object PRESENT = new Object();
    private ClassRegistry registry = new ClassRegistry();
    private ResolverChain resolverChain = new ResolverChain();
    private ThreadLocalReference<? extends Map<Object, Integer>> objectMapReference = new IdentityHashMapTlsr<Object, Integer>();
    private ThreadLocalReference<? extends Map<Integer, Object>> addressMapReference = new IdentityHashMapTlsr<Integer, Object>();
    private ThreadLocal<Boolean> tlIsConstant = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public StructedSerializer() {
        resolverChain.addResolver(new PrimitiveResolver(this));
        resolverChain.addResolver(new EnumResolver(this));
        resolverChain.addResolver(new StringResolver(this));
        resolverChain.addResolver(new ArrayResolver(this));
    }

    public void setConstant(boolean constant) {
        tlIsConstant.set(constant);
    }

    @Override
    public ClassRegistry getRegistry() {
        return registry;
    }

    @Override
    public ResolverChain getResolverChain() {
        return resolverChain;
    }

    public StructedSerializer beforeSerializing() {
        objectMapReference.get().clear();
        return this;
    }

    public StructedSerializer beforeDeserializing() {
        addressMapReference.get().clear();
        return this;
    }

    @Override
    public void serialize(NioBuffer buffer, Object obj, boolean writeClassName) {
        writeClassName(buffer, obj.getClass(), registry, writeClassName);
        if (!resolverChain.writeObject(buffer, obj, null))
            serialize0(buffer, obj, writeClassName);
    }

    private void serialize0(NioBuffer buffer, Object obj, boolean writeClassName) {
        try {
            Map<Object, Integer> objectMap = objectMapReference.get();
            objectMap.put(obj, buffer.position());

            buffer.mark().skip(4);
            int baseAddress = buffer.position();

            Class<?> clazz = obj.getClass();
            Map<Field, Integer> fieldMap;
            if (tlIsConstant.get())
                fieldMap = new LinkedHashMap<>();
            else
                fieldMap = new HashMap<>();
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (tlIsConstant.get())
                        fieldMap.put(field, 0);
                    if (!isFieldSerializable(field))
                        continue;

                    field.setAccessible(true);
                    Class<?> referenceType = field.getType();
                    Object value = field.get(obj);
                    if (isNull(value, referenceType))
                        continue;

                    if (objectMap.containsKey(value))
                        fieldMap.put(field, -objectMap.get(value));
                    else {
                        int address = buffer.position();
                        fieldMap.put(field, address);
                        objectMap.put(value, address);

                        Class<?> valueType = value.getClass();
                        if (!tlIsConstant.get() || referenceType == Object.class)
                            writeClassName(buffer, valueType, registry, true);

                        Type genericType = field.getGenericType();
                        Type[] genericTypes = null;
                        if (genericType instanceof ParameterizedType)
                            genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
                        if (!resolverChain.writeObject(buffer, value, referenceType, genericTypes))
                            serialize0(buffer, value, !tlIsConstant.get() || valueType != referenceType);
                    }
                }
            } while ((clazz = clazz.getSuperclass()) != Object.class);

            int headerAddress = buffer.position();
            buffer.reset().unmark().asByteBuffer().putInt(headerAddress).position(headerAddress);
            writeClassName(buffer, obj.getClass(), registry, writeClassName);

            buffer.putUnsignedVarint(fieldMap.size());
            int index = -1;
            for (Map.Entry<Field, Integer> fieldEntry : fieldMap.entrySet()) {
                index++;
                int address = fieldEntry.getValue();
                if (address != 0) {
                    if (tlIsConstant.get())
                        buffer.putUnsignedVarint(index);
                    else
                        buffer.asByteBuffer().putShort((short) fieldEntry.getKey().getName().hashCode());
                    buffer.putUnsignedVarint(address > 0 ? address - baseAddress : -address + headerAddress);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isFieldSerializable(Field field) {
        int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(NioBuffer buffer, Class<T> clazz) {
        Object instance = resolverChain.readObject(buffer, clazz);
        if (instance == null)
            instance = deserialize0(buffer, clazz);
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize0(NioBuffer buffer, Class<T> clazz) {
        try {
            Map<Integer, Object> addressMap = addressMapReference.get();
            if (!addressMap.containsKey(buffer.position()))
                addressMap.put(buffer.position(), PRESENT);

            int headerAddress = buffer.asByteBuffer().getInt();
            int baseAddress = buffer.position();

            buffer.position(headerAddress);
            if ((clazz = (Class) readClass(buffer, clazz, registry)) == null)
                throw new UnknownClassException();
            Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            Object instance = defaultConstructor.newInstance();

            int fieldSize = (int) buffer.getUnsignedVarint();
            Map<Short, Field> hashcodeMap = new HashMap<>(fieldSize);
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (isFieldSerializable(field))
                        hashcodeMap.put((short) field.getName().hashCode(), field);
                }
            } while ((clazz = (Class) clazz.getSuperclass()) != Object.class);

            for (int i = 0; i < fieldSize; i++) {
                Field field = hashcodeMap.get(buffer.asByteBuffer().getShort());
                int offset = (int) buffer.getUnsignedVarint();
                if (field == null)
                    continue;

                field.setAccessible(true);
                int address = (offset < headerAddress) ? (baseAddress + offset) : (offset - headerAddress);
                if (addressMap.containsKey(address)) {
                    Object value = addressMap.get(address);
                    field.set(instance, value == PRESENT ? instance : value);
                } else {
                    buffer.mark().position(address);
                    Type referenceType = field.getGenericType();
                    Object value = resolverChain.readObject(buffer, referenceType);
                    if (value == null) {
                        if (referenceType instanceof Class)
                            value = deserialize0(buffer, (Class<?>) referenceType);
                        else if (referenceType instanceof ParameterizedType)
                            value = deserialize0(buffer, (Class<?>) ((ParameterizedType) referenceType).getRawType());
                        else
                            value = deserialize0(buffer, null);
                    }
                    field.set(instance, value);
                    addressMap.put(address, value);
                    buffer.reset().unmark();
                }
            }
            return (T) instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class IdentityHashMapTlsr<K, V> extends ThreadLocalReference<IdentityHashMap<K, V>> {

        @Override
        protected Reference<IdentityHashMap<K, V>> initialValue() {
            return new SoftReference<>(new IdentityHashMap<K, V>());
        }
    }
}
