plugins {
    kotlin("jvm") version "1.5.30"
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

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.5.1")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

task<Jar>("buildJar") {
    archiveBaseName.set("my-library")
    archiveVersion.set("1.0")
    from(sourceSets.main.get().output)
}
