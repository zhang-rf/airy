## Quickstart

```java
    Serializer serializer = new OrderedSerializer();
    // Add some resolvers that you need...
    serializer.getResolverChain().addResolver(new StringResolver());
    serializer.getResolverChain().addResolver(new EnumResolver());
    // ...
    Airy airy = new Airy(serializer);
    // serialize...
    byte[] data = airy.serialize(someObject);
    // deserialize...
    Object object = airy.deserialize(data);
```
