import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	`maven-publish`
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.noarg") version "1.9.20"
	id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "com.github.nayasis"
version = "0.2.1-SNAPSHOT"

noArg {
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
	version = "19.0.2.1"
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
//	implementation("com.github.nayasis:basica-kt:0.3.2-SNAPSHOT")
//	implementation("com.github.nayasis:basica-kt:develop-SNAPSHOT")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.14")
	implementation("org.controlsfx:controlsfx:11.1.1")
	implementation("org.sejda.imageio:webp-imageio:0.1.2")
	implementation("org.yaml:snakeyaml:2.2")
	implementation("ch.qos.logback:logback-classic:1.4.11")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:3.0.5")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
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