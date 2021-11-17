import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.noarg") version "1.4.20"
	id("org.openjfx.javafxplugin") version "0.0.8"
}

noArg {
	invokeInitializers = true
}

java {
	// for 'supportImplementation'
	registerFeature("support") {
		usingSourceSet(sourceSets["main"])
	}
}

javafx {
	version = "13"
	modules = listOf("javafx.controls","javafx.fxml","javafx.web","javafx.swing")
}

group = "com.github.nayasis"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_11

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

	implementation("com.github.nayasis:basica-kt:0.1.3")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.8")
	implementation("org.controlsfx:controlsfx:8.40.10")

	"supportImplementation"("org.springframework.boot:spring-boot-starter-web:2.5.6")
	"supportImplementation"("ch.qos.logback:logback-classic:1.2.3")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:2.0.10")
	implementation("au.com.console:kassava:2.1.0")

	// test
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
	testImplementation("ch.qos.logback:logback-classic:1.2.3")

}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf(
			"-Xjsr305=strict"
		)
		jvmTarget = "11"
	}
}