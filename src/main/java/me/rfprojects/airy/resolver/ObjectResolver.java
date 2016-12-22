package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Type;

public abstract class ObjectResolver {

    protected Serializer serializer;
    private ThreadLocal<Boolean> threadLocalSkipCheck = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public ObjectResolver(Serializer serializer) {
        this.serializer = serializer;
    }

    public boolean skipCheck() {
        return threadLocalSkipCheck.get();
    }

    public void skipCheck(boolean skipCheck) {
        threadLocalSkipCheck.set(skipCheck);
    }

    public abstract boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes);

    public abstract Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes);
}
