import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

group = "com.example"
version = "0.0.3-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

val springShellVersion: String by project
val picocliVersion: String by project
val sparkVersion: String by project
val sparkRedisVersion: String by project
val janinoVersion: String by project

configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("info.picocli:picocli-spring-boot-starter:${picocliVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.springframework.shell:spring-shell-starter")
    implementation("org.apache.spark:spark-core_2.12:${sparkVersion}")
    implementation("org.apache.spark:spark-sql_2.12:${sparkVersion}")
    implementation("com.redislabs:spark-redis_2.12:${sparkRedisVersion}")
    implementation("org.codehaus.janino:janino:${janinoVersion}")
    implementation("org.codehaus.janino:commons-compiler:${janinoVersion}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // for CVE only
    implementation("org.apache.hadoop:hadoop-client-api:3.3.3")
    implementation("com.google.protobuf:protobuf-java:3.21.2")
    implementation("com.google.guava:guava:31.1-jre")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
