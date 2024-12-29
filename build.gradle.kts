plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version "1.5.30"  // 直接声明插件和版本
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"  // 确保 Kotlin 编译为 JDK 1.8 字节码
    }
}

buildscript {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/jcenter")
        }
        mavenCentral() // 使用 Maven 中央仓库
        google() // 使用 Google 的 Maven 仓库
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")  // 如果是 Android 项目，配置 Android Gradle Plugin
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(files("libs/SDKLib.jar"))
    implementation(files("libs/dvlib-27.1.3.jar"))
    implementation(files("libs/asm-tree-7.0.jar"))
    implementation(files("libs/lint-model-27.1.3.jar"))
    implementation(files("libs/sdk-common-27.1.3.jar"))
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // 替换为最新版本
}

tasks {
    shadowJar {
        archiveBaseName.set("example-fat-jar")
        archiveVersion.set("1.0")
        archiveClassifier.set("") // 确保没有附加分类后缀
    }
}