dependencies {
    implementation(project(":common"))
    compileOnly("dev.folia:folia-api:1.20.6-R0.1-SNAPSHOT")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}
