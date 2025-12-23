/**
 * Ring Racers Android - Touch Input Handler
 * Maps touch input to game controls
 */

#include <android/log.h>

#define LOG_TAG "RingRacers-Touch"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#ifdef HAVE_SDL
#include <SDL.h>
#endif

namespace TouchInput {

// Game control IDs matching Ring Racers controls
enum GameControl {
    GC_ACCELERATE = 0,  // Gas/accelerate
    GC_BRAKE,           // Brake
    GC_DRIFT,           // Drift/powerslide
    GC_ITEM,            // Use item
    GC_LOOKBACK,        // Look behind
    GC_PAUSE,           // Pause menu
    GC_TURNLEFT,        // Turn left (D-Pad)
    GC_TURNRIGHT,       // Turn right (D-Pad)
    GC_AIMUP,           // Aim up (D-Pad)
    GC_AIMDOWN,         // Aim down (D-Pad)
    GC_NUM
};

#ifdef HAVE_SDL
// SDL Scancodes for each control
// These match the default Ring Racers key bindings
static SDL_Scancode g_scancodes[GC_NUM] = {
    SDL_SCANCODE_SPACE,      // GC_ACCELERATE
    SDL_SCANCODE_LCTRL,      // GC_BRAKE
    SDL_SCANCODE_LSHIFT,     // GC_DRIFT
    SDL_SCANCODE_RETURN,     // GC_ITEM
    SDL_SCANCODE_L,          // GC_LOOKBACK
    SDL_SCANCODE_ESCAPE,     // GC_PAUSE
    SDL_SCANCODE_LEFT,       // GC_TURNLEFT
    SDL_SCANCODE_RIGHT,      // GC_TURNRIGHT
    SDL_SCANCODE_UP,         // GC_AIMUP
    SDL_SCANCODE_DOWN        // GC_AIMDOWN
};
#endif

// Track control states to avoid duplicate events
static bool g_states[GC_NUM] = {false};

void SendControlEvent(GameControl control, bool pressed) {
    if (control >= GC_NUM) return;
    
    // Avoid duplicate events
    if (g_states[control] == pressed) return;
    g_states[control] = pressed;
    
    LOGD("Control %d -> %s", control, pressed ? "PRESSED" : "RELEASED");
    
#ifdef HAVE_SDL
    SDL_Scancode scancode = g_scancodes[control];
    
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = pressed ? SDL_KEYDOWN : SDL_KEYUP;
    event.key.keysym.scancode = scancode;
    event.key.keysym.sym = SDL_GetKeyFromScancode(scancode);
    event.key.state = pressed ? SDL_PRESSED : SDL_RELEASED;
    event.key.repeat = 0;
    SDL_PushEvent(&event);
#endif
}

void ProcessDPadInput(float axisX, float axisY) {
    const float threshold = 0.3f;
    
    // X axis: left/right
    SendControlEvent(GC_TURNLEFT, axisX < -threshold);
    SendControlEvent(GC_TURNRIGHT, axisX > threshold);
    
    // Y axis: up/down (for aiming/looking)
    SendControlEvent(GC_AIMUP, axisY < -threshold);
    SendControlEvent(GC_AIMDOWN, axisY > threshold);
}

// Optional: Send as virtual gamepad instead of keyboard
void SendGamepadButton(int button, bool pressed) {
#ifdef HAVE_SDL
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = pressed ? SDL_JOYBUTTONDOWN : SDL_JOYBUTTONUP;
    event.jbutton.which = 0;
    event.jbutton.button = (Uint8)button;
    event.jbutton.state = pressed ? SDL_PRESSED : SDL_RELEASED;
    SDL_PushEvent(&event);
#endif
}

void SendGamepadAxis(int axis, float value) {
#ifdef HAVE_SDL
    Sint16 axisValue = (Sint16)(value * 32767.0f);
    
    SDL_Event event;
    SDL_memset(&event, 0, sizeof(event));
    event.type = SDL_JOYAXISMOTION;
    event.jaxis.which = 0;
    event.jaxis.axis = (Uint8)axis;
    event.jaxis.value = axisValue;
    SDL_PushEvent(&event);
#endif
}

void ResetAllControls() {
    for (int i = 0; i < GC_NUM; i++) {
        if (g_states[i]) {
            SendControlEvent((GameControl)i, false);
        }
    }
}

} // namespace TouchInput

// C API exports
extern "C" {

void TouchInput_SendControl(int control, int pressed) {
    TouchInput::SendControlEvent(
        static_cast<TouchInput::GameControl>(control),
        pressed != 0
    );
}

void TouchInput_ProcessDPad(float x, float y) {
    TouchInput::ProcessDPadInput(x, y);
}

void TouchInput_Reset() {
    TouchInput::ResetAllControls();
}

} // extern "C"
