package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.AiryException;
import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.handler.chain.HandlerChain;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.internal.ReflectionUtils;

import java.lang.reflect.Field;

public abstract class AbstractReferencedStructuredSerializer extends ReferencedSerializer implements StructuredSerializer {

    public AbstractReferencedStructuredSerializer(ClassRegistry registry, HandlerChain handlerChain) {
        super(registry, handlerChain);
    }

    @Override
    public FieldAccessor getAccessor(NioBuffer buffer, Class<?> type, String name) {
        FieldAccessor[] accessors = getAccessors(buffer, type);
        for (FieldAccessor accessor : accessors) {
            if (accessor.getField().getName().equals(name))
                return accessor;
        }
        throw new AiryException(new NoSuchFieldException());
    }

    protected class Accessor implements FieldAccessor {

        private final Field field;
        private final int address;

        protected Accessor(Field field, int address) {
            this.field = field;
            this.address = address;
        }

        @Override
        public Field getField() {
            return field;
        }

        @Override
        public int getAddress() {
            return address;
        }

        @Override
        public Object accessValue(NioBuffer buffer) {
            buffer.mark();
            try {
                Class<?> type = field.getType();
                return address < 0 ? Null.get(type)
                        : deserialize$Recursion(buffer.position(address), null,
                        type, ReflectionUtils.getTypeArguments(field.getGenericType()));
            } finally {
                buffer.reset().unmark();
            }
        }
    }
}
