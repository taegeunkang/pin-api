import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.jpa") version "1.4.32"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "com.pin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

springBoot {
    val jar: Jar by tasks
    jar.enabled = false
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("commons-fileupload:commons-fileupload:1.4")
    implementation("com.google.firebase:firebase-admin:9.1.1")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    runtimeOnly("mysql:mysql-connector-java")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("io.springfox:springfox-swagger2:2.9.2")
    implementation("io.springfox:springfox-swagger-ui:2.9.2")
//    implementation group: "javax.xml.bind", name: "jaxb-api", version: "2.3.1"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

allOpen { // 추가적으로 열어줄 allOpen
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
