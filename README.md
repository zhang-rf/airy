A easy, fast, efficient and zero-dependence serialization framework.

## Installation
Airy is available on the [releases page](https://github.com/zhang-rf/airy/releases) and at Maven Central.

### Integration with Maven
```xml
    <dependency>
        <groupId>me.rfprojects</groupId>
        <artifactId>airy</artifactId>
        <version>1.0.3</version>
    </dependency>
```

### Integration with Gradle
```groovy
    compile "me.rfprojects:airy:1.0.3"
```

## Quickstart
```java
    Airy airy = new Airy();
    byte[] data = airy.serialize(someBean);
    SomeBean object = (SomeBean) airy.deserialize(data); // or airy.deserialize(data, SomeBean.class);
```
Wasn't this process a piece of cake? The rest will detail the advanced usage of the framework.

##Core Components

###Serializer
Serializer is main component that provides the complete serializing function, it can work without the other components but inefficient.

We have HashSerializer and OrderSerializer available for now.

HashSerializer: use the hashcode of each field as an identity (default serializer)

OrderSerializer: use the order of each field as an identity

###Handler
Each handler concentrates on the serializing process of one or few classes, it can optimize the efficiency of serialization.

You can write and append your own handler to handle your classes.

####Default Handlers
<table>
  <tr><td>BooleanHandler</td><td>CharacterHandler</td><td>ByteHandler</td><td>ShortHandler</td></tr>
  <tr><td>IntegerHandler</td><td>LongHandler</td><td>FloatHandler</td><td>DoubleHandler</td></tr>
  <tr><td>StringHandler</td><td>EnumHandler</td><td>BytesHandler</td><td>ArrayHandler</td></tr>
  <tr><td>CollectionHandler</td><td>MapHandler</td><td>BigIntegerHandler</td><td>BigDecimalHandler</td></tr>
</table>

###Class Registry
ClassRegistry can reduce the size of the serialized data by replacing classname with an identity number.

We have already registered frequently used java classes by default, you can register your own classes to improve serialization efficiency.

##Advanced Usage
```java
    Airy airy = new Airy(new OrderSerializer()); // use OrderSerializer instead of HashSerializer
    airy.registerClass(SomeBean.class); // register class to reduce the size of the serialized data
    airy.appendHandler(someHander); // append your owner handler
    byte[] data = airy.serialize(someBean);
    SomeBean object = (SomeBean) airy.deserialize(data); // or airy.deserialize(data, SomeBean.class);
```
