plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pulxo.steps.service"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":data")) // Service needs data layer to repository sync if needed, though usually App wires them. Better to keep it clean. Let's just depend on domain and core for now. Wait, service might need to write to DB. We can inject the repository. So domain is enough if we use interfaces.

    implementation("androidx.core:core-ktx:1.12.0")
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
