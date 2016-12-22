package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Type;

public class StringResolver extends ObjectResolver {

    private String charsetName;

    public StringResolver(Serializer serializer) {
        super(serializer);
        charsetName = "UTF-8";
    }

    public StringResolver(Serializer serializer, String charsetName) {
        super(serializer);
        this.charsetName = charsetName;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object obj, Class<?> referenceType, Type... genericTypes) {
        if (skipCheck() || obj.getClass() == String.class) {
            buffer.putString((String) obj, charsetName);
            return true;
        }
        return false;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> referenceType, Type... genericTypes) {
        if (skipCheck() || referenceType == String.class)
            return buffer.getString(charsetName);
        return null;
    }
}
