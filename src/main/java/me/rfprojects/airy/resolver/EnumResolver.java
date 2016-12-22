package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Type;

public class EnumResolver extends ObjectResolver {

    public EnumResolver(Serializer serializer) {
        super(serializer);
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
        if (skipCheck() || obj.getClass().isEnum()) {
            buffer.putUnsignedVarint(((Enum) obj).ordinal());
            return true;
        }
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        return skipCheck() || referenceType.isEnum() ? referenceType.getEnumConstants()[(int) buffer.getUnsignedVarint()] : null;
    }
}
