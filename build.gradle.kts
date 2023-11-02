import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
}

group = "flocean.module.peeper.fjnuoj"
version = "3.3.1b1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.40")
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks.jar {
    archiveFileName.set("module_peeper_fjnuoj.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = project.group.toString() + ".Main"
    }
    from(
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    finalizedBy("jar")
}

val generateBuildInfo by tasks.registering {
    doLast {
        val props = Properties()
        val propsFile = file("$buildDir/buildInfo.properties")
        props.setProperty("fullName", project.group.toString())
        props.setProperty("moduleVersion", version.toString())
        props.setProperty("buildTime", SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(Date()))
        props.setProperty("buildBy", gradle.gradleVersion)
        propsFile.writer().use { props.store(it, null) }
    }
}

tasks.withType<ProcessResources> {
    dependsOn(generateBuildInfo)
    from("$buildDir/buildInfo.properties")
}


