package me.rfprojects.airy.handler;

import me.rfprojects.airy.core.AiryException;
import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlHandler implements Handler {

    @Override
    public boolean supportsType(Class<?> type) {
        return type == URL.class;
    }

    @Override
    public void write(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putString(((URL) object).toExternalForm());
    }

    @Override
    public Object read(NioBuffer buffer, Class<?> reference, Type... generics) {
        try {
            return new URL(buffer.getString());
        } catch (MalformedURLException e) {
            throw new AiryException(e);
        }
    }
}
