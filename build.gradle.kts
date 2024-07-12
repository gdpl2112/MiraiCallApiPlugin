plugins {
    id("java")

    val kotlinVersion = "1.7.22"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "io.github.gdpl2112"
version = "3.4"

repositories {
    maven("https://repo1.maven.org/maven2/")
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
    mavenCentral()
}

mirai {
    noTestCore = true
    setupConsoleTestRuntime {
        // 移除 mirai-core 依赖
        classpath = classpath.filter {
            !it.nameWithoutExtension.startsWith("mirai-core-jvm")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))

//    compileOnly("net.mamoe:mirai-core:2.15.0")
	testConsoleRuntime("top.mrxiaom:overflow-core:2.16.0-db61867-SNAPSHOT")
//    compileOnly("net.mamoe:mirai-console-compiler-common:2.15.0")
//    implementation(platform("net.mamoe:mirai-bom:2.15.0"))
//    testImplementation("net.mamoe:mirai-core-mock:2.15.0")
    testImplementation("net.mamoe:mirai-logging-slf4j:2.15.0")

    implementation(platform("org.slf4j:slf4j-parent:2.0.6"))
    testImplementation("org.slf4j:slf4j-simple")

//    compileOnly("io.github.Kloping:SpringTool:0.5.8")
//    compileOnly("io.github.Kloping:spt-web:0.2.0")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")

    implementation("io.github.Kloping:SpringTool:0.5.8")
    implementation("io.github.Kloping:spt-web:0.2.0")
}

