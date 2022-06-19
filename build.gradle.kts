import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`maven`
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.noarg") version "1.6.10"
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

group = "com.github.nayasis"
version = "0.1.12-SNAPSHOT"
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

val JAVA_FX_VERSION = "17"
dependencies {

//	implementation("com.github.nayasis:basica-kt:0.2.4")
	implementation("com.github.nayasis:basica-kt:develop-SNAPSHOT")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.8")
	implementation("org.controlsfx:controlsfx:11.1.1")

	// javafx
	implementation("org.openjfx:javafx-base:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-base:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-base:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-base:$JAVA_FX_VERSION:linux")
	implementation("org.openjfx:javafx-graphics:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:linux")
	implementation("org.openjfx:javafx-controls:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-controls:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-controls:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-controls:$JAVA_FX_VERSION:linux")
	implementation("org.openjfx:javafx-fxml:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:linux")
	implementation("org.openjfx:javafx-web:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-web:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-web:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-web:$JAVA_FX_VERSION:linux")
	implementation("org.openjfx:javafx-swing:$JAVA_FX_VERSION")
	"supportImplementation"("org.openjfx:javafx-swing:$JAVA_FX_VERSION:win")
	"supportImplementation"("org.openjfx:javafx-swing:$JAVA_FX_VERSION:mac")
	"supportImplementation"("org.openjfx:javafx-swing:$JAVA_FX_VERSION:linux")

	// spring
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

	testImplementation("org.openjfx:javafx-base:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-base:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-base:$JAVA_FX_VERSION:linux")
	testImplementation("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-graphics:$JAVA_FX_VERSION:linux")
	testImplementation("org.openjfx:javafx-controls:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-controls:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-controls:$JAVA_FX_VERSION:linux")
	testImplementation("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-fxml:$JAVA_FX_VERSION:linux")
	testImplementation("org.openjfx:javafx-web:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-web:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-web:$JAVA_FX_VERSION:linux")
	testImplementation("org.openjfx:javafx-swing:$JAVA_FX_VERSION:win")
	testImplementation("org.openjfx:javafx-swing:$JAVA_FX_VERSION:mac")
	testImplementation("org.openjfx:javafx-swing:$JAVA_FX_VERSION:linux")

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

tasks.withType<Wrapper> {
	gradleVersion = "6.7"
}