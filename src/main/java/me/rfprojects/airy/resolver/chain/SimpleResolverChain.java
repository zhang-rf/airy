package me.rfprojects.airy.resolver.chain;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.Resolver;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleResolverChain implements ResolverChain {

    private Set<Resolver> resolverSet = new CopyOnWriteArraySet<>();
    private ConcurrentMap<Class<?>, Resolver> resolverMap = new ConcurrentHashMap<>();

    public void addResolver(Resolver resolver) {
        resolverSet.add(resolver);
        resolverMap.clear();
    }

    @Override
    public boolean checkType(Class<?> type) {
        return false;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        Resolver mappedResolver = resolverMap.get(reference);
        if (mappedResolver != null)
            return mappedResolver != this && mappedResolver.writeObject(buffer, object, reference);
        else {
            for (Resolver resolver : resolverSet) {
                if (resolver.checkType(reference)) {
                    resolver.writeObject(buffer, object, reference);
                    resolverMap.put(reference, resolver);
                    return true;
                }
            }
            resolverMap.put(reference, this);
            return false;
        }
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        Resolver mappedResolver = resolverMap.get(reference);
        if (mappedResolver != null)
            return mappedResolver != this ? mappedResolver.readObject(buffer, reference) : null;
        else {
            for (Resolver resolver : resolverSet) {
                if (resolver.checkType(reference)) {
                    Object instance = resolver.readObject(buffer, reference);
                    resolverMap.put(reference, resolver);
                    return instance;
                }
            }
            resolverMap.put(reference, this);
            return null;
        }
    }
}
