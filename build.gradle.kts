import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	`maven-publish`
	kotlin("jvm") version "1.8.10"
	kotlin("plugin.noarg") version "1.8.10"
	id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "com.github.nayasis"
version = "0.1.13-SNAPSHOT"

noArg {
	annotation("com.github.nayasis.kotlin.spring.kotlin.annotation.NoArg")
	invokeInitializers = true
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
	registerFeature("support") {
		usingSourceSet(sourceSets["main"])
	}
	withJavadocJar()
	withSourcesJar()
}

javafx {
	version = "21.0.1"
	modules = listOf("javafx.controls","javafx.web","javafx.fxml","javafx.swing")
}

configurations.all {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
	resolutionStrategy.cacheDynamicVersionsFor(5, "minutes")
}

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
	maven { url = uri("https://jitpack.io") }
}

dependencies {

	implementation("com.github.nayasis:basica-kt:0.3.1")
//	implementation("com.github.nayasis:basica-kt:develop-SNAPSHOT")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.14")
	implementation("org.controlsfx:controlsfx:11.1.1")
	implementation("org.sejda.imageio:webp-imageio:0.1.2")

	// spring
	"supportImplementation"("org.springframework.boot:spring-boot-starter-web:2.5.6")
	"supportImplementation"("ch.qos.logback:logback-classic:1.3.5")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:3.0.5")
	implementation("au.com.console:kassava:2.1.0")

	// test
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
	testImplementation("ch.qos.logback:logback-classic:1.3.5")

}

tasks.withType<Test> {
	useJUnitPlatform()
	exclude("**/*")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf(
			"-Xjsr305=strict"
		)
		jvmTarget = "11"
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}