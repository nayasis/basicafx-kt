import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`maven`
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.noarg") version "1.5.21"
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
version = "0.1.5-SNAPSHOT"
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

	implementation("com.github.nayasis:basica-kt:0.1.6")
	implementation("commons-cli:commons-cli:1.4")
	implementation("no.tornado:tornadofx:1.7.20")
	implementation("org.jclarion:image4j:0.7")
	implementation("org.apache.httpcomponents:httpclient:4.5.8")
	implementation("org.controlsfx:controlsfx:8.40.10")

	// javafx
	implementation("org.openjfx:javafx-base:13")
	"supportImplementation"("org.openjfx:javafx-base:13:win")
	"supportImplementation"("org.openjfx:javafx-base:13:mac")
	"supportImplementation"("org.openjfx:javafx-base:13:linux")
	implementation("org.openjfx:javafx-graphics:13")
	"supportImplementation"("org.openjfx:javafx-graphics:13:win")
	"supportImplementation"("org.openjfx:javafx-graphics:13:mac")
	"supportImplementation"("org.openjfx:javafx-graphics:13:linux")
	implementation("org.openjfx:javafx-controls:13")
	"supportImplementation"("org.openjfx:javafx-controls:13:win")
	"supportImplementation"("org.openjfx:javafx-controls:13:mac")
	"supportImplementation"("org.openjfx:javafx-controls:13:linux")
	implementation("org.openjfx:javafx-fxml:13")
	"supportImplementation"("org.openjfx:javafx-fxml:13:win")
	"supportImplementation"("org.openjfx:javafx-fxml:13:mac")
	"supportImplementation"("org.openjfx:javafx-fxml:13:linux")
	implementation("org.openjfx:javafx-web:13")
	"supportImplementation"("org.openjfx:javafx-web:13:win")
	"supportImplementation"("org.openjfx:javafx-web:13:mac")
	"supportImplementation"("org.openjfx:javafx-web:13:linux")
	implementation("org.openjfx:javafx-swing:13")
	"supportImplementation"("org.openjfx:javafx-swing:13:win")
	"supportImplementation"("org.openjfx:javafx-swing:13:mac")
	"supportImplementation"("org.openjfx:javafx-swing:13:linux")

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

	testImplementation("org.openjfx:javafx-base:13:win")
	testImplementation("org.openjfx:javafx-base:13:mac")
	testImplementation("org.openjfx:javafx-base:13:linux")
	testImplementation("org.openjfx:javafx-graphics:13:win")
	testImplementation("org.openjfx:javafx-graphics:13:mac")
	testImplementation("org.openjfx:javafx-graphics:13:linux")
	testImplementation("org.openjfx:javafx-controls:13:win")
	testImplementation("org.openjfx:javafx-controls:13:mac")
	testImplementation("org.openjfx:javafx-controls:13:linux")
	testImplementation("org.openjfx:javafx-fxml:13:win")
	testImplementation("org.openjfx:javafx-fxml:13:mac")
	testImplementation("org.openjfx:javafx-fxml:13:linux")
	testImplementation("org.openjfx:javafx-web:13:win")
	testImplementation("org.openjfx:javafx-web:13:mac")
	testImplementation("org.openjfx:javafx-web:13:linux")
	testImplementation("org.openjfx:javafx-swing:13:win")
	testImplementation("org.openjfx:javafx-swing:13:mac")
	testImplementation("org.openjfx:javafx-swing:13:linux")

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