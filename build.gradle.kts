plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.github.FlowArg:OpenLauncherLib:VERSION")
    implementation("com.github.FlowArg:FlowUpdater:VERSION")
}

tasks.test {
    useJUnitPlatform()
}