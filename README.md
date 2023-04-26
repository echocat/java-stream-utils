# Utils for Java Streams

Provides utils to deal with Java Streams. Especially add missing features like `takeWhile` and `batch`.

## Topics

* [Features](#features)
* [Getting Started](#getting-started)
* [FAQ](#faq)
* [Contributing](#contributing)
* [License](#license)

## Features

* [Stream `takeWhile`](#stream-takewhile)
* [Stream `generate`](#stream-generate)
* [Stream `batch`](#stream-batch)
* [ResultSet `toStream`](#resultset-tostream)
* [ResultSet `toStream` with mapper](#resultset-tostream-with-mapper)

### Stream `takeWhile`

> This is one of the missing features of Java 8 streaming API.

Continue a stream only until all elements matches a specific predicate.

Reference: [`<T> Stream<T> StreamUtils.takeWhile(Stream<T> input, Predicate<T> predicate)`](/src/main/java/org/echocat/jsu/StreamUtils.java)

Example:
```java
// Prints out random numbers to stdout until 66 was generated
// 66 will not appear in the stdout.
Random random = new Random();
Stream<Integer> stream = Stream.generate(() -> random.nextInt(100));
StreamUtils.takeWhile(stream, number -> number != 66)
    .forEach(System.out::println);
```

### Stream `generate`

> This is one of the missing features of Java 8 streaming API.

Generate a stream and decide by your own when it should be done.
The regular Streaming API could only produce endless streams.
A solution could be our [Stream `takeWhile`](#stream-takewhile) but our `generate` is sometimes a smarter solution.

Reference: [`<T> Stream<T> StreamUtils.generate(Stream<T> input, Predicate<T> predicate)`](/src/main/java/org/echocat/jsu/StreamUtils.java)

Example:
```java
// Prints out random numbers to stdout until 66 was generated
// 66 will not appear in the stdout.
Random random = new Random();
StreamUtils.generate(() -> {
    int candidate = random.nextInt(100);
    return candidate == 66 ? Value.end() : Value.valueOf(candidate);
}).forEach(System.out::println);
```

### Stream `batch`

> This is one of the missing features of Java 8 streaming API.

Slice a stream with an unknown length in batches with a fixed size.

Perfect for example to query a batch of elements from a database to do with all of their IDs another
batch query to the database to enrich them with more information.

Reference: [`<T> Stream<T> StreamUtils.batch(Stream<T> input, int batchSize)`](/src/main/java/org/echocat/jsu/StreamUtils.java)

Example:
```java
// Prints out random numbers to stdout in ten blocks until 66 was generated
// 66 will not appear in the stdout.
Random random = new Random();
Stream<Integer> stream = StreamUtils.generate(() -> {
    int candidate = random.nextInt(100);
    return candidate == 66 ? Value.end() : Value.valueOf(candidate);
});
StreamUtils.batch(stream, 10)
    .forEach(System.out::println);
```

How to integrate a batch back again into a single stream?
```java
Stream<Integer> sourceStream = ...;
Stream<List<Integer>> batchedStream = StreamUtils.batch(sourceStream, 10);
Stream<Integer> enrichedStream = batchedStream.flatMap(batch -> {
    // Do something with the batch...
    ...
    // Flat map all elements again into a single element stream...
    return batch.stream(); 
});
enrichedStream.forEach(System.out::println);
```

### ResultSet `toStream`

> This is one of the missing features of JDBC API.

Simply converts a `java.sql.ResultSet` into a `java.util.stream.Stream` of ResultSets.
For every element in this stream `ResultSet.next()` will be called
... which means every element in this stream represents a row.

Reference: [`Stream<ResultSet> JdbcUtils.toStream(ResultSet resultSet)`](/src/main/java/org/echocat/jsu/JdbcUtils.java)

Example:
```java
// Prints of every row the first column as string to stdout.
[..]
Stream<ResultSet> stream = JdbcUtils.toStream(resultSet);
stream
    .forEach(row -> System.out.println(row.getString(1)));
```

### ResultSet `toStream` with mapper

Is similar to the [`toStream`](#resultset-tostream) version of above but also includes already mapping functionality
which also handles transparent `java.sql.SQLException`s which means: You are not always required
to implement annoying `try {..} catch {..}` blocks for you mappings.

Reference: [`<T> Stream<T> JdbcUtils.toStream(ResultSet resultSet, SqlFunction<ResultSet, T> mapper)`](/src/main/java/org/echocat/jsu/JdbcUtils.java)

Example:
```java
// Prints of every row the first column as string to stdout.
[..]
Stream<String> stream = JdbcUtils.toStream(resultSet, row -> row.getString(1));
stream
    .forEach(System.out::println));
```

## Getting started

### Dependency

#### 1. Register our repository (optional)

You can directly register our repository if you want always the latest version. The central can be versions behind.

##### Maven

```xml
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
    <repository>
        <id>echocat</id>
        <url>https://packages.echocat.org/maven</url>
    </repository>
</repositories>
```

##### Gradle

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://packages.echocat.org/maven"
    }
}
```

#### 2. Pick your version

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.echocat.java-stream-utils/java-stream-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.echocat.java-stream-utils/java-stream-utils)

Find your desired version you want to install (usually the latest one) [by looking it up in our repository](https://github.com/echocat/java-stream-utils/packages/) or directly at [the Maven Central](http://search.maven.org/#search|ga|1|g:org.echocat.java-stream-utils%20AND%20a:java-stream-utils).

#### 3. Add the dependency

##### Maven

```xml 
<dependency>
    <groupId>org.echocat.java-stream-utils</groupId>
    <artifactId>java-stream-utils</artifactId>
    <version><!-- THE VERSION --></version>
</dependency>
```

##### Gradle

```groovy
compile 'org.echocat.java-stream-utils:java-stream-utils:<THE VERSION>'
```

## FAQ

### How to enable streaming result sets?

The MySQL JDBC driver by default stores at first the whole result of the database in the memory before
it will be processed. This delivers in many cases with small result sets a better performance instead
streaming it one by one. But this is very memory consuming and slow for large result sets - and also
dangerous - it may cause `OutOfMemoryError`s.

You can do the following thing to enable the streaming results (works for MySQL):
```java
Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
statement.setFetchSize(Integer.MIN_VALUE);  
```

## Contributing

java-stream-utils is an open source project of [echocat](https://echocat.org). So if you want to make this project even better, you can
contribute to this project on [Github](https://github.com/echocat/java-stream-utils) by
[fork us](https://github.com/echocat/java-stream-utils/fork).

If you commit code to this project you have to accept that this code will be released under the [license](#license) of this project.

## License

See [LICENSE](LICENSE) file.
