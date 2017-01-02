package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.resolver.Resolver;
import me.rfprojects.airy.resolver.ResolverChain;
import me.rfprojects.airy.resolver.primitive.*;
import me.rfprojects.airy.util.ThreadLocalInteger;
import me.rfprojects.airy.util.ThreadLocalReference;

import java.lang.ref.SoftReference;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ReferencedSerializer implements Serializer {

    protected static final Object PRESENT = new Object();

    private ClassRegistry registry = new ClassRegistry();
    private ResolverChain resolverChain = new ResolverChain();

    private ThreadLocalReference<Map<Object, Integer>> objectMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalReference<Map<Integer, Object>> addressMapReference = new ThreadLocalReference<>(SoftReference.class, IdentityHashMap.class);
    private ThreadLocalInteger serializingDepthLocal = new ThreadLocalInteger();
    private ThreadLocalInteger deserializingDepthLocal = new ThreadLocalInteger();

    protected ReferencedSerializer() {
        addResolver(new BooleanResolver());
        addResolver(new CharacterResolver());
        addResolver(new ByteResolver());
        addResolver(new ShortResolver());
        addResolver(new IntegerResolver());
        addResolver(new LongResolver());
        addResolver(new FloatResolver());
        addResolver(new DoubleResolver());
    }

    @Override
    public void addResolver(Resolver resolver) {
        resolverChain.addResolver(resolver);
    }

    @Override
    public ClassRegistry getRegistry() {
        return registry;
    }

    protected ResolverChain resolverChain() {
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
}
