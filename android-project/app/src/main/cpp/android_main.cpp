/**
 * Ring Racers Android - Main JNI Bridge
 */

#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "RingRacers"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// SDL headers (if available)
#ifdef HAVE_SDL
#include <SDL.h>
#endif

extern "C" {

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad - Ring Racers native library loaded");
    return JNI_VERSION_1_6;
}

// Touch Controls JNI
JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_TouchControlsView_nativeSendKey(
    JNIEnv* env, jclass clazz, jint keyCode, jboolean pressed) 
{
    LOGI("Key event: code=%d pressed=%d", keyCode, pressed);
    
#ifdef HAVE_SDL
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = pressed ? SDL_KEYDOWN : SDL_KEYUP;
    event.key.keysym.scancode = (SDL_Scancode)keyCode;
    event.key.keysym.sym = SDL_GetKeyFromScancode((SDL_Scancode)keyCode);
    event.key.state = pressed ? SDL_PRESSED : SDL_RELEASED;
    SDL_PushEvent(&event);
#endif
}

JNIEXPORT void JNICALL
Java_org_kartkrew_ringracers_TouchControlsView_nativeSendAxis(
    JNIEnv* env, jclass clazz, jint axis, jfloat value) 
{
    LOGI("Axis event: axis=%d value=%.2f", axis, value);
    
#ifdef HAVE_SDL
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = SDL_JOYAXISMOTION;
    event.jaxis.which = 0;
    event.jaxis.axis = (Uint8)axis;
    event.jaxis.value = (Sint16)(value * 32767.0f);
    SDL_PushEvent(&event);
#endif
}

} // extern "C"

// SDL main entry point
#ifdef HAVE_SDL
extern "C" int SDL_main(int argc, char *argv[]) {
    LOGI("SDL_main started");
    
    // TODO: Call Ring Racers main function
    // return D_SRB2Main(argc, argv);
    
    LOGI("Ring Racers would start here");
    return 0;
}
#endif
