# basicafx

basic JavaFx 13 library bases on Kotlin.


## Dependency

### maven

1. add repository in **pom.xml**.

```xml
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

2. add dependency in **pom.xml**.

```xml
<dependency>
  <groupId>com.github.nayasis</groupId>
  <artifactId>basicafx-kt</artifactId>
  <version>0.1.0</version>
</dependency>
```

### gradle

1. add repository in **build.gradle.kts**.

```kotlin
repositories {
  maven { url = uri("https://jitpack.io") }
}
```

2. add dependency in **build.gradle.kts**.

```kotlin
dependencies {
  implementation( "com.github.nayasis:basicafx-kt:0.1.0" )
}
```