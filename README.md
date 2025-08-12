# basicafx

basic JavaFx library bases on Kotlin.
- minimum version
  - Java : 11 or above
  - JavaFx : 17 or above

[![](https://jitpack.io/v/nayasis/basicafx-kt.svg)](https://jitpack.io/#nayasis/basicafx-kt)

## Features

### Image Download with Playwright
이 라이브러리는 Playwright를 사용하여 웹에서 이미지를 다운로드할 수 있습니다. Chromium 브라우저를 사용하며 SSL 오류를 무시합니다.

#### Playwright 설치
```bash
# Gradle을 사용하는 경우
./gradlew installPlaywright

# 또는 수동으로 설치
npx playwright install chromium
```

#### 사용 예시
```kotlin
import com.github.nayasis.kotlin.javafx.misc.toBufferedImage

// URL에서 이미지 다운로드
val imageUrl = "https://example.com/image.jpg"
val bufferedImage = imageUrl.toUrl().toBufferedImage()

// JavaFX Image로 변환
val javafxImage = bufferedImage.toImage()
```

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