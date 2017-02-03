package me.rfprojects.airy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import me.rfprojects.airy.serializer.OrderSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {

    private static final long NANOTIMES_PER_MILLISEC = 1000000;
    private Airy airy = new Airy(new OrderSerializer());
    private Project project;

    @Before
    public void project() throws MalformedURLException {
        airy.registryClass(Project.class); //optional
        project = new Project("airy", "me.rfprojects", "1.0.3", License.MIT,
                new Developer("zhangrongfan", 18667022962L, "610384825@qq.com",
                        new URL("https://github.com/zhang-rf/")));
    }

    @Test
    public void multiThread() throws InterruptedException {
        System.out.println();

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    airy.serialize(project);
                }
            }).start();
        }
        Thread.sleep(1000);

        System.out.println();
    }

    @Test
    public void airy() {
        System.out.println("airy...");

        long t1 = System.nanoTime();
        byte[] data = airy.serialize(project);
        long t2 = System.nanoTime();
        System.out.println("serialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms, size: " + data.length);

        t1 = System.nanoTime();
        Object object = airy.deserialize(data);
        t2 = System.nanoTime();
        System.out.println("deserialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms");

        System.out.println();
        assertEquals(project, object);
    }

    @Test
    public void javaSerializable() throws IOException, ClassNotFoundException {
        System.out.println("java serializable...");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        long t1 = System.nanoTime();
        outputStream.writeObject(project);
        outputStream.close();
        long t2 = System.nanoTime();
        System.out.println("serialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms, size: " + byteArrayOutputStream.size());

        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        t1 = System.nanoTime();
        inputStream.readObject();
        inputStream.close();
        t2 = System.nanoTime();
        System.out.println("deserialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms");
    }

    @Test
    public void kryo() {
        System.out.println("kryo...");

        Kryo kryo = new Kryo();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        long t1 = System.nanoTime();
        kryo.writeClassAndObject(output, project);
        output.close();
        long t2 = System.nanoTime();
        System.out.println("serialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms, size: " + byteArrayOutputStream.size());

        Input input = new Input(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        t1 = System.nanoTime();
        Object object = kryo.readClassAndObject(input);
        input.close();
        t2 = System.nanoTime();
        System.out.println("deserialization: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms");

        System.out.println();
        assertEquals(project, object);
    }
}
