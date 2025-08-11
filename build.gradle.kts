import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.vanniktech.maven.publish.SonatypeHost

plugins {
	java
	signing
	kotlin("jvm") version "2.2.0"
	kotlin("plugin.noarg") version "1.9.20"
	id("com.vanniktech.maven.publish") version "0.31.0"
	id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.github.nayasis"
version = "0.2.2"

noArg {
	invokeInitializers = true
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

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {

	implementation("io.github.nayasis:basica-kt:0.3.5")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.14")
	implementation("org.controlsfx:controlsfx:11.1.1")
	implementation("org.sejda.imageio:webp-imageio:0.1.2")
	implementation("org.yaml:snakeyaml:2.2")
	implementation("ch.qos.logback:logback-classic:1.5.13")
	implementation("com.microsoft.playwright:playwright:1.54.0")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:3.0.5")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	// test
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")

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

// Playwright 설치 task 추가
tasks.register("installPlaywright") {
	group = "playwright"
	description = "Install Playwright browsers"
	doLast {
		exec {
			commandLine("npx", "playwright", "install", "chromium")
		}
	}
}

mavenPublishing {
	signAllPublications()
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	pom {
		name.set(rootProject.name)
		description.set("Basic Kotlin utility library providing common functionality for Kotlin applications.")
		url.set("https://github.com/nayasis/basica-kt")
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
			connection.set("scm:git:github.com/nayasis/basica-kt.git")
			developerConnection.set("scm:git:ssh://github.com/nayasis/basica-kt.git")
			url.set("https://github.com/nayasis/basica-kt/tree/master")
		}
	}
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
	freeCompilerArgs.set(listOf("-XXLanguage:+BreakContinueInInlineLambdas"))
}