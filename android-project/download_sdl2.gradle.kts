// Gradle script to download prebuilt SDL2 for Android
// Run: ./gradlew setupSDL

import java.net.URL
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import java.io.BufferedInputStream

val cppDir = file("app/src/main/cpp")
val sdl2Dir = file("$cppDir/SDL2")

// SDL2 Android prebuilt AAR contains the .so libraries
val sdlAndroidVersion = "2.28.5"
val sdlSourceUrl = "https://github.com/libsdl-org/SDL/releases/download/release-$sdlAndroidVersion/SDL2-$sdlAndroidVersion.zip"

tasks.register("downloadSDL2Source") {
    description = "Downloads SDL2 source with Android project"
    group = "sdl"
    
    doLast {
        if (file("$sdl2Dir/include/SDL.h").exists()) {
            println("SDL2 already exists, skipping download.")
            return@doLast
        }
        
        println("Downloading SDL2 $sdlAndroidVersion source...")
        val zipFile = file("$cppDir/sdl2_temp.zip")
        
        try {
            // Download
            URL(sdlSourceUrl).openStream().use { input ->
                FileOutputStream(zipFile).use { output ->
                    input.copyTo(output)
                }
            }
            println("Download complete. Extracting...")
            
            // Extract
            ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    // Rename SDL2-version to SDL2
                    val newPath = entry.name.replace("SDL2-$sdlAndroidVersion", "SDL2")
                    val destFile = file("$cppDir/$newPath")
                    
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        destFile.outputStream().use { out ->
                            zis.copyTo(out)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            
            println("SDL2 extracted to: $sdl2Dir")
        } finally {
            zipFile.delete()
        }
    }
}

// Task to build SDL2 for Android using its own Android.mk
tasks.register("buildSDL2") {
    description = "Builds SDL2 native libraries for Android"
    group = "sdl"
    dependsOn("downloadSDL2Source")
    
    doLast {
        val androidProjectDir = file("$sdl2Dir/android-project")
        
        if (!androidProjectDir.exists()) {
            throw GradleException("SDL2 Android project not found at $androidProjectDir")
        }
        
        println("SDL2 source is ready. The CMake will build it automatically.")
        println("\nTo manually build, you can run:")
        println("  cd ${sdl2Dir}/android-project")
        println("  ./gradlew assembleRelease")
    }
}

tasks.register("setupSDL") {
    description = "Download and prepare SDL2 for Android build"
    group = "sdl"
    dependsOn("downloadSDL2Source")
    
    doLast {
        println("")
        println("========================================")
        println("SDL2 setup complete!")
        println("========================================")
        println("SDL2 location: $sdl2Dir")
        println("")
        println("CMake will compile SDL2 automatically during build.")
        println("Run: ./gradlew assembleDebug")
        println("========================================")
    }
}
