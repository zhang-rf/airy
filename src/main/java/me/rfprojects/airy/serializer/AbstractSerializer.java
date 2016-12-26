package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.resolver.ResolverChain;

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
}
