import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib.*"))
        exclude(dependency("org.slf4j:slf4j-api"))
        exclude(dependency("org.yaml:snakeyaml"))
        exclude(dependency("com.mysql:mysql-connector-j"))
        exclude(dependency("com.zaxxer:HikariCP"))
    }
    relocate("io.github.theramu.dependencyloader", "io.github.theramu.servershout.dependencyloader")
    relocate("org.bstats", "io.github.theramu.servershout.metrics")
}
