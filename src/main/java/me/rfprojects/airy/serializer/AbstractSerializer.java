package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.core.UnknownClassException;
import me.rfprojects.airy.resolver.ResolverChain;

import static me.rfprojects.airy.internal.ClassUtil.readClass;
import static me.rfprojects.airy.internal.ClassUtil.writeClassName;

public abstract class AbstractSerializer implements Serializer {

    private ClassRegistry registry = new ClassRegistry();
    private ResolverChain resolverChain = new ResolverChain();

    @Override
    public ClassRegistry getRegistry() {
        return registry;
    }

    @Override
    public ResolverChain getResolverChain() {
        return resolverChain;
    }

    @Override
    public void serialize(NioBuffer buffer, Object object, boolean writeClassName) {
        writeClassName(buffer, object.getClass(), registry, writeClassName);
        if (!resolverChain.writeObject(buffer, object, null))
            serialize0(buffer, object, writeClassName);
    }

    protected abstract void serialize0(NioBuffer buffer, Object object, boolean writeClassName);

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(NioBuffer buffer, Class<T> clazz) {
        if ((clazz = (Class<T>) readClass(buffer, clazz, registry)) == null)
            throw new UnknownClassException();
        Object instance = resolverChain.readObject(buffer, clazz);
        if (instance == null)
            instance = deserialize0(buffer, clazz);
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    protected abstract <T> T deserialize0(NioBuffer buffer, Class<T> clazz);
}
