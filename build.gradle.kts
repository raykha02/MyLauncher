plugins {
    id("application")
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("fr.flowarg:openlauncherlib:3.2.11")
    implementation("fr.flowarg:flowupdater:1.9.4")
    implementation("fr.flowarg:flowupdater-curseforgeplugin:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.launcher.Main")
}