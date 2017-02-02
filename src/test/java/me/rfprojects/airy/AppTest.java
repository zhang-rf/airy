package me.rfprojects.airy;

import me.rfprojects.airy.serializer.Serializer;
import me.rfprojects.airy.support.ConcurrentObjectMap;

import java.net.MalformedURLException;
import java.util.Arrays;

public class AppTest {

    private static final long NANOTIMES_PER_MILLISEC = 1000000;
    private Airy airy;
    private Serializer serializer;
    private Me me;

    public static void main(String[] args) throws ClassNotFoundException, MalformedURLException {
        Airy airy = new Airy();
//        airy.registryClass(Bean.class);
        byte[] bytes = airy.serialize(new Bean(new Bean()));
        System.out.println(Arrays.toString(bytes));
        System.out.println(new String(bytes));
        System.out.println(airy.deserialize(bytes, null));
        System.out.println(new ConcurrentObjectMap(bytes).get("a"));
    }

    static class Bean {
        Object a;

        public Bean() {
        }

        public Bean(Object a) {
            this.a = a;
        }
    }
//    public AppTest() {
//        serializer = new OrderSerializer();
//        serializer.handlerChain().appendHandler(new StringHandler());
//        serializer.handlerChain().appendHandler(new EnumHandler());
//        serializer.handlerChain().appendHandler(new BytesHandler());
//        serializer.handlerChain().appendHandler(new ArrayHandler(serializer, registry));
//        serializer.handlerChain().appendHandler(new CollectionHandler(serializer));
//        serializer.handlerChain().appendHandler(new MapHandler(serializer));
//        serializer.handlerChain().appendHandler(new DateHandler());
//        airy = new Airy(serializer);
//    }
//
//    @Before
//    public void me() {
//        me = new Me();
//        me.setName("张荣帆")
//                .setGender(Gender.MALE)
//                .setMobile(18667022962L)
//                .setEmail("610384825@qq.com");
//
//        me.setMajor("软件工程")
//                .setCet((byte) 6)
//                .setGraduation(new Date(2017, 7, 1));
//        List<Skill> skillList = new ArrayList<>();
//        skillList.add(new Skill("Java", "熟练", "熟练掌握Java，具有扎实的基础，对网络、多线程以及JVM等有一定的掌握。"));
//        skillList.add(new Skill("Spring", "熟练", "熟练使用Spring (Boot)和Spring MVC框架。"));
//        skillList.add(new Skill("数据库", "熟悉", "熟悉MyBatis，熟悉MySQL数据库。熟悉常用Linux命令。"));
//        skillList.add(new Skill("其他框架", "使用", "Spring Session、Shiro、Dubbo、Redis"));
//        me.setSkillList(skillList);
////        me.setLoadTest(new Load(new byte[1024 * 1024 * 100]));
//    }
//
//    @Test
//    public void mainTest() {
//        System.out.println("main test...");
//
//        long t1 = System.nanoTime();
//        byte[] data = airy.serialize(me);
//        long t2 = System.nanoTime();
//        System.out.println("serialize: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms, size: " + data.length);
//
//        t1 = System.nanoTime();
//        Object object = airy.deserialize(data);
//        t2 = System.nanoTime();
//        System.out.println("deserialize: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms");
//
//        System.out.println(object);
//        System.out.println();
//    }
//
//    @Test
//    public void threadTest() throws InterruptedException {
//        System.out.println("thread test...");
//        for (int i = 0; i < 10; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    assertEquals(airy.serialize(me).length, 549);
//                }
//            }).start();
//        }
//        Thread.sleep(1000);
//    }
//
//    @Test
//    public void javaSerializableTest() throws IOException, ClassNotFoundException {
//        System.out.println("java serializable test...");
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
//        long t1 = System.nanoTime();
//        outputStream.writeObject(me);
//        outputStream.close();
//        long t2 = System.nanoTime();
//        System.out.println("serialize: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms, size: " + byteArrayOutputStream.size());
//
//        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
//
//        t1 = System.nanoTime();
//        inputStream.readObject();
//        t2 = System.nanoTime();
//        System.out.println("deserialize: " + (t2 - t1) / NANOTIMES_PER_MILLISEC + "ms");
//
//    }
//
//    @Test
//    public void mapTest() {
//        System.out.println("map test...");
//
//        byte[] data = airy.serialize(me);
//        ObjectMap objectMap = new ObjectMap(data, (StructuredSerializer) serializer);
//        assertEquals(objectMap.get("name"), "张荣帆");
//        assertEquals(objectMap.get("gender"), Gender.MALE);
//        assertEquals(objectMap.get("mobile"), 18667022962L);
//        assertEquals(objectMap.get("email"), "610384825@qq.com");
//        assertEquals(objectMap.get("major"), "软件工程");
//        assertEquals(objectMap.get("cet"), (byte) 6);
//        assertEquals(objectMap.get("graduation"), new Date(2017, 7, 1));
//        assertEquals(((List<Skill>) objectMap.get("skillList")).size(), 4);
//    }
//
//    @Test
//    public void concurrentMapTest() throws InterruptedException {
//        System.out.println("concurrent map test...");
//
//        byte[] data = airy.serialize(me);
//        final ObjectMap objectMap = new ConcurrentObjectMap(data, (StructuredSerializer) serializer);
//        for (int i = 0; i < 10; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    assertEquals(objectMap.get("name"), "张荣帆");
//                    assertEquals(objectMap.get("gender"), Gender.MALE);
//                    assertEquals(objectMap.get("mobile"), 18667022962L);
//                    assertEquals(objectMap.get("email"), "610384825@qq.com");
//                    assertEquals(objectMap.get("major"), "软件工程");
//                    assertEquals(objectMap.get("cet"), (byte) 6);
//                    assertEquals(objectMap.get("graduation"), new Date(2017, 7, 1));
//                    assertEquals(((List<Skill>) objectMap.get("skillList")).size(), 4);
//                }
//            }).start();
//        }
//        Thread.sleep(1000);
//    }
}
