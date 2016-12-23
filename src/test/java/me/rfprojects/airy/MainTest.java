package me.rfprojects.airy;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.EnumResolver;
import me.rfprojects.airy.resolver.PrimitiveResolver;
import me.rfprojects.airy.resolver.StringResolver;
import me.rfprojects.airy.serializer.ConstClassSerializer;
import me.rfprojects.airy.serializer.Serializer;
import org.junit.Test;

import java.util.Arrays;

public class MainTest {

    @Test
    public void test() {
        Serializer serializer = new ConstClassSerializer();
        serializer.getResolverChain().addResolver(new PrimitiveResolver(serializer));
        serializer.getResolverChain().addResolver(new EnumResolver(serializer));
        serializer.getResolverChain().addResolver(new StringResolver(serializer));
        NioBuffer buffer = NioBuffer.allocate(1024);
        serializer.serialize(buffer, 63, false);
        byte[] bytes = new byte[buffer.position()];
        buffer.rewind().asByteBuffer().get(bytes);
        System.out.println(Arrays.toString(bytes));
        System.out.println(new String(bytes));

        System.out.println(serializer.deserialize(buffer.clear(), Integer.class));
    }
}

class Bean {

    private int var1;

    Bean() {
    }

    Bean(int var1) {
        this.var1 = var1;
    }

    public int getVar1() {
        return var1;
    }

    public Bean setVar1(int var1) {
        this.var1 = var1;
        return this;
    }
}