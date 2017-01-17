package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class StringHandler implements Handler {

    private String charsetName;

    public StringHandler() {
        this("UTF-8");
    }

    public StringHandler(String charsetName) {
        this.charsetName = charsetName;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return type == String.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putString((String) object, charsetName);
        return true;
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        return buffer.getString(charsetName);
    }
}
