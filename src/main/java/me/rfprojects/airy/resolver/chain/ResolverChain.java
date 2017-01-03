package me.rfprojects.airy.resolver.chain;

import me.rfprojects.airy.resolver.Resolver;

public interface ResolverChain extends Resolver {

    void addResolver(Resolver resolver);
}
