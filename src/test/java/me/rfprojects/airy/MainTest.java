package me.rfprojects.airy;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.map.ObjectMap;
import me.rfprojects.airy.resolver.EnumResolver;
import me.rfprojects.airy.resolver.StringResolver;
import me.rfprojects.airy.serializer.ImmortalSerializer;
import me.rfprojects.airy.serializer.Serializer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class MainTest {

    @Test
    public void test() {
        Serializer serializer = new ImmortalSerializer();
        serializer.getResolverChain().addResolver(new EnumResolver());
        serializer.getResolverChain().addResolver(new StringResolver());
        NioBuffer buffer = NioBuffer.allocate(1024);
        serializer.serialize(buffer, new Bean(new Bean.InnerBean(63)), false);
        byte[] bytes = new byte[buffer.position()];
        buffer.rewind().asByteBuffer().get(bytes);
        System.out.println(Arrays.toString(bytes));
        System.out.println(new String(bytes));

        System.out.println(serializer.deserialize(buffer.clear(), Bean.class));

        Map<String, Object> map = new ObjectMap<>(bytes, new ImmortalSerializer(), Bean.class);
        System.out.println(map.get("var1.var1"));
    }
}

class Bean {

    private Object var1;

    Bean() {
    }

    Bean(Object var1) {
        this.var1 = var1;
    }

    public int getVar1() {
        return (int) var1;
    }

    public Bean setVar1(int var1) {
        this.var1 = var1;
        return this;
    }

    static class InnerBean {

        private Object var1;

        public InnerBean() {
        }

        InnerBean(Object var1) {
            this.var1 = var1;
        }
    }
}