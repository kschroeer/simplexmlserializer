# SimpleXMLSerializer
@author Kay Schröer (acsf.dev@gmail.com)

This is a very basic XML serializer. It stores Java objcts in a XML file or loads a XML file and map its content back to a Java object.

The idea behind is not to have to rely on extensive frameworks such as Apache Xerces to manage e.g. configuration files, but to manage with only a few classes.

## Features

- Serialization of objects to files and streams
- Deserialization of objects from files and streams
- Support of basic types, collections, dates, enums, files, maps and custom classes
- Excluding attributes from serialization (e.g. for internal use)
- Validation of deserialized objects

## Usage

**Serialization:**

```java
MyConfig config = new MyConfig();
try {
    XMLSerializer.serialize(
        new File("AppConfig.xml"),
        config
    );
} catch (Exception e) {
    e.printStackTrace();
}
```

**Deserialization:**

```java
try {
    MyConfig config = (MyConfig) XMLSerializer.deserialize(
        new File("AppConfig.xml"),
        MyConfig.class
    );
} catch (Exception e) {
    e.printStackTrace();
}
```

## Type handling

In order to keep the process as simple as possible, the generated XML document consists exclusively of elements with subelements, no attributes. The class names correspond to the nodes in the DOM, the object contents are stored as text nodes. DTDs and XSDs are waived.

Please note that if certain attributes are to be excluded from serialization, they must be marked with the keyword "transient".

**Example:**

```java
public class Demo {
    private boolean isActive = true;
    private ConnectionDetails connectionDetails = new ConnectionDetails();
    private transient int internalValue = 2018;
}

public class ConnectionDetails {
    private String host = "github.com";
    private int port = 80;
}
```

```xml
<Demo>
    <IsActive>true</IsActive>
    <ConnectionDetails>
        <Host>github.com</Host>
        <Port>80</Port>
    </ConnectionDetails>
</Demo>
```

### Primitives and Strings

For the primitive types boolean, byte, char, double, float, int, long, short (or their wrapper classes) and strings, the toString() method is called to serialize them.

**Example:**

```java
public class Demo {
    private boolean boolAttr = false;
    private byte byteAttr = (byte) 127;
    private char charAttr = 'x';
    private double doubleAttr = 99.99;
    private float floatAttr = 0.1F;
    private int intAttr = -5;
    private long longAttr = 1000000000;
    private short shortAttr = 256;
    private String stringAttr = "dummy";
}
```

```xml
<Demo>
    <BoolAttr>false</BoolAttr>
    <ByteAttr>127</ByteAttr>
    <CharAttr>x</CharAttr>
    <DoubleAttr>99.99</DoubleAttr>
    <FloatAttr>0.1</FloatAttr>
    <IntAttr>-5</IntAttr>
    <LongAttr>1000000000</LongAttr>
    <ShortAttr>256</ShortAttr>
    <StringAttr>dummy</StringAttr>
</Demo>
```

### Dates, Enums and Files

Dates are serialized as timestamps (number of milliseconds since January 1, 1970, 00:00:00 GMT), for Enums the string representation is stored and on the File objects the toString() method is called to get the full path and file name.

**Example:**

```java
public class Demo {
    private Date dateAttr = new Date();
    private EnumType enumAttr = EnumType.ONE;
    private File fileAttr = new File("/home/myfile.java");
}

public enum EnumType {
    ONE, TWO, THREE;
}
```

```xml
<Demo>
    <DateAttr>1534302128061</DateAttr>
    <EnumAttr>ONE</EnumAttr>
    <FileAttr>/home/myfile.java</FileAttr>
</Demo>
```

### Arrays and Collections

Arrays and objects that implement the Collection interface are serialized as a list of peer elements. The array type or the generic list type serves as the node name. For collections, it should also be noted that they are only supported by inheritance.

**Example:**

```java
public class Demo {
    private int[] arrAttr = { 1, 2, 3 };
    private FileList listAttr = new FileList();
}

public class FileList extends ArrayList<File> {
    public FileList() {
        super();
        add(new File("myfile1.java"));
        add(new File("myfile2.java"));
        add(new File("myfile3.java"));
    }
}
```

```xml
<Demo>
    <ArrAttr>
        <Integer>1</Integer>
        <Integer>2</Integer>
        <Integer>3</Integer>
    </ArrAttr>
    <ListAttr>
        <File>myfile1.java</File>
        <File>myfile2.java</File>
        <File>myfile3.java</File>
    </ListAttr>
</Demo>
```

### Maps

Objects that implement the Map interface are serialized as a list of pair elements. The generic types serve as node names. It should also be noted that the maps are only supported by inheritance.

**Example:**

```java
public class Demo {
    private FileMap mapAttr = new FileMap();
}

public class FileMap extends HashMap<String, String> {
    public FileMap() {
        super();
        put("path", "/home");
        put("name", "myfile.java");
    }
}
```

```xml
<Demo>
    <MapAttr>
        <String>path</String>
        <String>/home</String>
        <String>name</String>
        <String>myfile.java</String>
    </MapAttr>
</Demo>
```

## Validation

As already described, no DTDs and XSDs are used. In order to be able to validate the objects anyway, the deserialization checks whether the target class implements the ObjectInputValidation interface. If so, the deserializer executes the validateObject() method, and the result can then be handled by catching the InvalidObjectException.

**Example:**

```java
public class Demo implements ObjectInputValidation {
    private boolean isActive = true;

    @Override
    public void validateObject() throws InvalidObjectException {
        if (isActive) {
            throw new InvalidObjectException("error message");
        }
    }
}

try {
    Demo demo = (Demo) XMLSerializer.deserialize(
        new File("Demo.xml"),
        Demo.class
    );
} catch (InvalidObjectException e) {
    System.err.println("Invalid value found.");
} catch (Exception e) {
    e.printStackTrace();
}
```
