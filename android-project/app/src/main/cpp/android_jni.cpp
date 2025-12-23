/**
 * Ring Racers Android - JNI Bridge
 * 
 * This file serves as the main entry point and JNI bridge between
 * Java/Android and the Ring Racers native game engine.
 */

#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <string>
#include <cstdlib>
#include <cstring>

#define LOG_TAG "RingRacers"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

// SDL headers
#ifdef HAVE_SDL
#include <SDL.h>
#endif

// Forward declaration for Ring Racers main function
// Note: d_main.cpp defines D_SRB2Main
extern "C" {
    // These are defined in Ring Racers source
    extern int D_SRB2Main(int argc, char **argv);
    extern void I_ShutdownSystem(void);
    extern void I_Quit(void);
}

// Global state
static JavaVM* g_javaVM = nullptr;
static jobject g_activityRef = nullptr;
static bool g_gameRunning = false;
static char* g_homePath = nullptr;

// JNI OnLoad
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad: Ring Racers native library loaded");
    g_javaVM = vm;
    
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        LOGE("Failed to get JNI environment");
        return JNI_ERR;
    }
    
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnUnload: Cleaning up");
    
    if (g_homePath) {
        free(g_homePath);
        g_homePath = nullptr;
    }
}

extern "C" {

// Set environment variable from Java
JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_RingRacersActivity_nativeSetEnv(
    JNIEnv* env, 
    jclass clazz, 
    jstring name, 
    jstring value) 
{
    const char* nameStr = env->GetStringUTFChars(name, nullptr);
    const char* valueStr = env->GetStringUTFChars(value, nullptr);
    
    if (nameStr && valueStr) {
        setenv(nameStr, valueStr, 1);
        LOGD("setenv: %s = %s", nameStr, valueStr);
        
        // Store home path for later use
        if (strcmp(nameStr, "RINGRACERSHOME") == 0 || strcmp(nameStr, "SRB2HOME") == 0) {
            if (g_homePath) free(g_homePath);
            g_homePath = strdup(valueStr);
        }
    }
    
    if (nameStr) env->ReleaseStringUTFChars(name, nameStr);
    if (valueStr) env->ReleaseStringUTFChars(value, valueStr);
}

// Get game data path
JNIEXPORT jstring JNICALL
Java_org_kartkrew_ringracers_RingRacersActivity_nativeGetGamePath(
    JNIEnv* env,
    jclass clazz)
{
    const char* path = getenv("RINGRACERSHOME");
    if (!path) path = getenv("SRB2HOME");
    if (!path) path = "/sdcard/ringracers";
    
    return env->NewStringUTF(path);
}

// Initialize and start the game
JNIEXPORT jint JNICALL
Java_org_kartkrew_ringracers_RingRacersActivity_nativeInit(
    JNIEnv* env,
    jclass clazz,
    jobjectArray argsArray)
{
    LOGI("nativeInit called");
    
    if (g_gameRunning) {
        LOGW("Game already running!");
        return -1;
    }
    
    // Convert Java string array to C array
    int argc = env->GetArrayLength(argsArray);
    char** argv = new char*[argc + 1];
    
    for (int i = 0; i < argc; i++) {
        jstring jstr = (jstring)env->GetObjectArrayElement(argsArray, i);
        const char* str = env->GetStringUTFChars(jstr, nullptr);
        argv[i] = strdup(str);
        env->ReleaseStringUTFChars(jstr, str);
        LOGD("argv[%d] = %s", i, argv[i]);
    }
    argv[argc] = nullptr;
    
    g_gameRunning = true;
    
    LOGI("Starting Ring Racers with %d arguments...", argc);
    
    int result = 0;
    
#ifdef HAVE_SDL
    // SDL will call SDL_main
    result = SDL_main(argc, argv);
#else
    // Direct call
    result = D_SRB2Main(argc, argv);
#endif
    
    // Cleanup
    for (int i = 0; i < argc; i++) {
        free(argv[i]);
    }
    delete[] argv;
    
    g_gameRunning = false;
    
    LOGI("Ring Racers exited with code %d", result);
    return result;
}

// Shutdown the game
JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_RingRacersActivity_nativeQuit(
    JNIEnv* env,
    jclass clazz)
{
    LOGI("nativeQuit called");
    
    if (g_gameRunning) {
        I_Quit();
        g_gameRunning = false;
    }
}

// Touch Controls - Send key event
JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_TouchControlsView_nativeSendKey(
    JNIEnv* env,
    jclass clazz,
    jint keyCode,
    jboolean pressed)
{
    LOGD("Key: code=%d pressed=%d", keyCode, pressed);
    
#ifdef HAVE_SDL
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = pressed ? SDL_KEYDOWN : SDL_KEYUP;
    event.key.keysym.scancode = (SDL_Scancode)keyCode;
    event.key.keysym.sym = SDL_GetKeyFromScancode((SDL_Scancode)keyCode);
    event.key.state = pressed ? SDL_PRESSED : SDL_RELEASED;
    event.key.repeat = 0;
    SDL_PushEvent(&event);
#endif
}

// Touch Controls - Send axis event (for D-Pad as analog stick)
JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_TouchControlsView_nativeSendAxis(
    JNIEnv* env,
    jclass clazz,
    jint axis,
    jfloat value)
{
    LOGD("Axis: axis=%d value=%.2f", axis, value);
    
#ifdef HAVE_SDL
    // Convert -1.0 to 1.0 range to SDL axis range (-32768 to 32767)
    Sint16 axisValue = (Sint16)(value * 32767.0f);
    
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = SDL_JOYAXISMOTION;
    event.jaxis.which = 0;  // Virtual joystick ID
    event.jaxis.axis = (Uint8)axis;
    event.jaxis.value = axisValue;
    SDL_PushEvent(&event);
#endif
}

// Check if game is running
JNIEXPORT jboolean JNICALL
Java_org_kartkrew_ringracers_RingRacersActivity_nativeIsRunning(
    JNIEnv* env,
    jclass clazz)
{
    return g_gameRunning ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"

// SDL main entry point (called by SDL Android activity)
#ifdef HAVE_SDL
extern "C" int SDL_main(int argc, char *argv[]) {
    LOGI("SDL_main started with %d arguments", argc);
    
    for (int i = 0; i < argc; i++) {
        LOGD("  argv[%d] = %s", i, argv[i]);
    }
    
    int result = 0;
    
    try {
        LOGI("Calling D_SRB2Main...");
        result = D_SRB2Main(argc, argv);
    } catch (const std::exception& e) {
        LOGE("Exception in D_SRB2Main: %s", e.what());
        result = -1;
    } catch (...) {
        LOGE("Unknown exception in D_SRB2Main");
        result = -1;
    }
    
    LOGI("D_SRB2Main returned %d", result);
    return result;
}
#endif
