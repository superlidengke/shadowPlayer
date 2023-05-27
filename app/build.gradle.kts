plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileSdk = 33
    defaultConfig {
        applicationId = "com.example.simplemusic"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
    namespace = "com.example.simplemusic"
    sourceSets {
        getByName("debug") {
            assets {
                srcDirs("src\\debug\\assets", "src\\androidTest\\assets")
            }
        }
    }
}

dependencies {
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar")
            )
        )
    )
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("de.hdodenhof:circleimageview:3.0.0")
    implementation("com.jaeger.statusbarutil:library:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation("com.github.rosuH:MPG123-Android:0.1.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.9.0")
    implementation("org.litepal.android:core:2.0.0")
    implementation("me.rosuh:AndroidFilePicker:0.8.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

repositories {
    maven { url = uri("https://jitpack.io") }
    jcenter()
}