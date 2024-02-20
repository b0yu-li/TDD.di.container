plugins {
    id("java")
}

group = "org.boyu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JSR 330
    implementation("jakarta.inject:jakarta.inject-api:2.0.0")
    annotationProcessor("org.glassfish:javax.annotation:10.0-b28")

    // Apache Common Collections
    implementation("org.apache.commons:commons-collections4:4.4")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // assert-j
    testImplementation("org.assertj:assertj-core:3.19.0")

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    testCompileOnly("org.projectlombok:lombok:1.18.20")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.test {
    useJUnitPlatform()
}