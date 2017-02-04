A easy, fast, efficient and zero-dependence serialization framework.

一个简单，快速，高效，零依赖的序列化框架。

## Installation 安装
Airy is available on the [releases page](https://github.com/zhang-rf/airy/releases).

Airy 可从 [releases page](https://github.com/zhang-rf/airy/releases) 获取。

## Quickstart 快速上手
```java
    Airy airy = new Airy();
    byte[] data = airy.serialize(someBean);
    SomeBean object = (SomeBean) airy.deserialize(data); // or airy.deserialize(data, SomeBean.class);
```
Wasn't this process a piece of cake? The rest will detail the advanced usage of the framework.

是不是很简单？接下来将详细介绍框架的一些细节和高级用法。

##Core Components 核心组成

###Serializer
Serializer is the core component that provides serialization function. in theory, the framework can work without other components but inefficient. We have HashSerializer and OrderSerializer available for now.

HashSerializer: default serializer, use the hashcode of each field as an identity

OrderSerializer: use the order of each field as an identity

|

Serializer 是 Airy 框架中提供序列化功能的核心组件，理论上只有 Serializer 框架也能工作，但那样将变得十分低效。可用的 Serializer 有 HashSerializer 和 OrderSerializer两种。

HashSerializer：默认的 Serializer ，以对象中字段的Hashcode作为序列化标识，字段的数量以及顺序可以变更，保持向前向后兼容

OrderSerializer：以对象中字段的顺序作为序列化标识，序列化的数据更小，但是字段的数量以及顺序不能变更

###Handler
Each Handler focuses on one or few types of object serialization processes, it can improve efficiency and reduce the size of serialized data.

每一个 Handler 都专注于一种或几种类型的对象的序列化过程，以提高效率，减小序列化数据的大小。

|

You can write and append your own handler to handle your classes.

你也可以编写自己的 Handler 并把它附加到 Serializer 来提高性能。

####Default Handlers
<table>
  <tr><td>BooleanHandler</td><td>CharacterHandler</td><td>ByteHandler</td><td>ShortHandler</td></tr>
  <tr><td>IntegerHandler</td><td>LongHandler</td><td>FloatHandler</td><td>DoubleHandler</td></tr>
  <tr><td>StringHandler</td><td>EnumHandler</td><td>BytesHandler</td><td>ArrayHandler</td></tr>
  <tr><td>CollectionHandler</td><td>MapHandler</td><td>BigIntegerHandler</td><td>BigDecimalHandler</td></tr>
</table>

###Class Registry
ClassRegistry can reduce the size of the serialized data by assigning a ID to the registered class.

By default, most of the commonly used Java classes have been registered, and you can manually register your classes to improve efficiency.

ClassRegistry 通过向被注册的类指派一个 ID 来减小序列化数据的字节大小。

默认情况下，大部分常用的 Java 类已经被注册过了，你可以手动注册哪些还未被注册的待序列化的类来提高效率。

##Advanced Usage 高级用法
```java
    Airy airy = new Airy(new OrderSerializer()); // use OrderSerializer instead of HashSerializer
    airy.registerClass(SomeBean.class); // register class to reduce the size of the serialized data
    airy.appendHandler(someHander); // append your owner handler
    byte[] data = airy.serialize(someBean);
    SomeBean object = (SomeBean) airy.deserialize(data); // or airy.deserialize(data, SomeBean.class);
```
##Benchmark 跑分
There is comparison with Java Serializable and Kryo in unit test, you can gitclone the code and run it to see the result.(Quietly tell you, Airy is the fastest in time and the smallest in size)

单元测试中有与 Java Serializable 和 Kryo 的跑分对比，大家可以把代码git clone下来亲自测试。（悄悄告诉你，Airy 是最快并且数据最小的，嘿嘿。）

##Issues 问题
Welcome to ask questions and doubts in the [Issues](https://github.com/zhang-rf/airy/issues) page!

欢迎在 [Issues](https://github.com/zhang-rf/airy/issues) 页面中提出遇到的问题和疑惑！