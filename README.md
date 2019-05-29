Differentia
===========

A little framework for building meaningful messages on what has changed, for an audit log.

Installation
------------

In your Java application add a dependency on:

```xml
<dependency>
    <groupId>com.infobelt</groupId>
    <artifactId>differentia</artifactId>
    <version>${project.version}</version>
</dependency>
```

Getting Started
---------------

First up you will need to annotate the object that you want to manage audit messages on, for
example:

```java
@Data
@AuditMetadata(name = "example", descriptiveProperty = "name")
public class SimpleExampleObject {

    @AuditMetadata(name="First name")
    private String name;

    @AuditMetadata(name="Description")
    private String description;

    private int amount;

}
```

This basically tells us how we want to present the names of the properties in the audit message, also what we want 
to name the object.  Note we also can say which property is the descriptive one.  This is used when we 
want to construct a message like "Added address home" (where address is the name of the class and home was the value of
the property flagged the descriptiveProperty).

You can then simply create an AuditBuilder and use it:

```java
AuditBuilder builder = new AuditBuilder()
SimpleExampleObject obj1 = new SimpleExampleObject();
obj1.setName("Cheese");
log.info(builder.buildMessage(null, obj1));
```

Based on the object defined above you should end up with:

```
New example Cheese
```

License
=======

[See license](LICENSE.md)
