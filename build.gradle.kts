import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.vanniktech.maven.publish.SonatypeHost

plugins {
	java
	signing
	kotlin("jvm") version "2.2.0"
	id("com.vanniktech.maven.publish") version "0.31.0"
	id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "io.github.nayasis"
version = "0.2.3-SNAPSHOT"

repositories {
	mavenLocal()
	mavenCentral()
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(22))
	}
}

javafx {
	version = "24.0.2"
	modules = listOf("javafx.controls","javafx.web","javafx.fxml","javafx.swing")
}

dependencies {

	implementation("io.github.nayasis:basica-kt:0.3.7-SNAPSHOT")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.controlsfx:controlsfx:11.2.2")
	implementation("org.sejda.imageio:webp-imageio:0.1.2")
	implementation("org.yaml:snakeyaml:2.2")
	implementation("ch.qos.logback:logback-classic:1.5.13")
	implementation("com.microsoft.playwright:playwright:1.54.0")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.10.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

	// test
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
	testImplementation("org.testfx:testfx-junit5:4.0.18")

//	testImplementation("com.techsenger.jeditermfx:jeditermfx:1.1.0")
	testImplementation("com.techsenger.jeditermfx:jeditermfx-core:1.1.0")
	testImplementation("com.techsenger.jeditermfx:jeditermfx-ui:1.1.0")
	testImplementation("com.techsenger.jeditermfx:jeditermfx-app:1.0.0")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
	options.release.set(22)
}

tasks.register<Exec>("installPlaywright") {
	group = "playwright"
	description = "Install Playwright browsers"
	commandLine("npx", "playwright", "install", "chromium")
}

mavenPublishing {
	// Skip signing for local Maven repository deployment
	if (!gradle.startParameter.taskNames.any { 
		it.contains("publishToMavenLocal") || it.contains("publishMavenPublicationToMavenLocal") 
	}) {
		signAllPublications()
	}
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	pom {
		name.set(rootProject.name)
		description.set("Basic JavaFx library based on Kotlin")
		url.set("https://github.com/nayasis/basicafx-kt")
		licenses {
			license {
				name.set("Apache License, Version 2.0")
				url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
			}
		}
		developers {
			developer {
				id.set("nayasis")
				name.set("nayasis")
				email.set("nayasis@gmail.com")
			}
		}
		scm {
			connection.set("scm:git:github.com/nayasis/basicafx-kt.git")
			developerConnection.set("scm:git:ssh://github.com/nayasis/basicafx-kt.git")
			url.set("https://github.com/nayasis/basicafx-kt/tree/master")
		}
	}
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
	freeCompilerArgs.set(listOf("-XXLanguage:+BreakContinueInInlineLambdas"))
}