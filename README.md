# basicafx

basic JavaFx library bases on Kotlin.
- minimum version
  - Java : 11 or above
  - JavaFx : 17 or above

[![](https://jitpack.io/v/nayasis/basicafx-kt.svg)](https://jitpack.io/#nayasis/basicafx-kt)

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
  <version>x.y.z</version>
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
  implementation( "com.github.nayasis:basicafx-kt:x.y.z" )
}
```