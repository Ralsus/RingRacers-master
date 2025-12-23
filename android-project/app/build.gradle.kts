import java.net.URL
import java.io.FileOutputStream

plugins {
    id("com.android.application")
}

// SDL2 download
val sdl2Version = "2.28.5"
val sdl2Dir = file("src/main/cpp/SDL2")

tasks.register("downloadSDL2") {
    doLast {
        if (!file("${sdl2Dir}/CMakeLists.txt").exists()) {
            println("Downloading SDL2 ${sdl2Version}...")
            
            val zipFile = file("${layout.buildDirectory.get()}/SDL2.zip")
            layout.buildDirectory.get().asFile.mkdirs()
            
            URL("https://github.com/libsdl-org/SDL/releases/download/release-${sdl2Version}/SDL2-${sdl2Version}.zip")
                .openStream().use { input ->
                    FileOutputStream(zipFile).use { out -> input.copyTo(out) }
                }
            
            copy {
                from(zipTree(zipFile))
                into(file("src/main/cpp"))
            }
            
            file("src/main/cpp/SDL2-${sdl2Version}").renameTo(sdl2Dir)
            println("SDL2 ready!")
        }
    }
}

tasks.matching { it.name.contains("CMake") || it.name.contains("externalNative") }.configureEach {
    dependsOn("downloadSDL2")
}

android {
    namespace = "org.kartkrew.ringracers"
    compileSdk = 34
    ndkVersion = "25.2.9519653"

    defaultConfig {
        applicationId = "org.kartkrew.ringracers"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "2.3"
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-frtti", "-fexceptions")
                cFlags += listOf("-std=c11")
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-24"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            isDebuggable = true
            isJniDebuggable = true
        }
    }
    
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    // Include game assets (if present)
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        // Don't compress pk3 files
        resources {
            excludes += listOf("**/*.pk3")
        }
    }
    
    // Large APK support
    aaptOptions {
        noCompress += listOf("pk3", "wad", "deh", "bex", "lua")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}
