package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.resolver.chain.ResolverChain;
import me.rfprojects.airy.resolver.chain.SimpleResolverChain;
import me.rfprojects.airy.resolver.primitive.*;
import me.rfprojects.airy.util.ThreadLocalInteger;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ReferencedSerializer implements Serializer {

    protected static final Object PRESENT = new Object();

    private ClassRegistry registry = new ClassRegistry();
    private ResolverChain resolverChain = new SimpleResolverChain();

    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalInteger serializingDepthLocal = new ThreadLocalInteger();
    private ThreadLocalInteger deserializingDepthLocal = new ThreadLocalInteger();

    protected ReferencedSerializer() {
        resolverChain.addResolver(new BooleanResolver());
        resolverChain.addResolver(new CharacterResolver());
        resolverChain.addResolver(new ByteResolver());
        resolverChain.addResolver(new ShortResolver());
        resolverChain.addResolver(new IntegerResolver());
        resolverChain.addResolver(new LongResolver());
        resolverChain.addResolver(new FloatResolver());
        resolverChain.addResolver(new DoubleResolver());
    }

    @Override
    public ClassRegistry getRegistry() {
        return registry;
    }

    @Override
    public ResolverChain getResolverChain() {
        return resolverChain;
    }

    protected Map<Object, Integer> objectMap() {
        return objectMapReference.get();
    }

    protected Map<Integer, Object> addressMap() {
        return addressMapReference.get();
    }

    protected ThreadLocalInteger serializingDepth() {
        return serializingDepthLocal;
    }

    protected ThreadLocalInteger deserializingDepth() {
        return deserializingDepthLocal;
    }

    protected boolean isFieldSerializable(Field field) {
        int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers));
    }

    protected Type[] getGenericTypes(Type parameterizedType) {
        return parameterizedType instanceof ParameterizedType ? ((ParameterizedType) parameterizedType).getActualTypeArguments() : new Type[0];
    }
}
