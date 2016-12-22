package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResolverChain {

    private List<ObjectResolver> resolvers = new ArrayList<>();
    private ConcurrentMap<Class<?>, ObjectResolver> resolverMap = new ConcurrentHashMap<>();

    public void addResolver(ObjectResolver resolver) {
        resolvers.add(resolver);
    }

    public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
        Class<?> clazz = obj.getClass();
        ObjectResolver mappedResolver = resolverMap.get(clazz);
        if (mappedResolver != null) {
            mappedResolver.skipCheck(true);
            return mappedResolver.writeObject(buffer, obj, referenceType, genericTypes);
        }

        for (ObjectResolver resolver : resolvers) {
            resolver.skipCheck(false);
            if (resolver.writeObject(buffer, obj, referenceType, genericTypes)) {
                resolverMap.put(clazz, resolver);
                return true;
            }
        }
        return false;
    }

    public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        ObjectResolver mappedResolver = resolverMap.get(referenceType);
        if (mappedResolver != null) {
            mappedResolver.skipCheck(true);
            return mappedResolver.readObject(buffer, referenceType, genericTypes);
        }

        for (ObjectResolver resolver : resolvers) {
            resolver.skipCheck(false);
            Object instance = resolver.readObject(buffer, referenceType, genericTypes);
            if (instance != null) {
                resolverMap.put(referenceType, resolver);
                return instance;
            }
        }
        return null;
    }
}
