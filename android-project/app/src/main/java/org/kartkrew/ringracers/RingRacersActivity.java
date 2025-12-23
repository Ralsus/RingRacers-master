package org.kartkrew.ringracers;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

import java.io.File;

/**
 * Main activity for Ring Racers Android port.
 */
public class RingRacersActivity extends SDLActivity implements TouchControlsView.TouchControlsListener {
    
    private static final String TAG = "RingRacers";
    
    private TouchControlsView touchControls;
    private Vibrator vibrator;
    private AssetCopier assetCopier;
    
    // Native methods
    private static native void nativeSetEnv(String name, String value);
    private static native String nativeGetGamePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onCreate(savedInstanceState);
        
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        assetCopier = new AssetCopier(this);
        
        // Setup game path
        setupGamePath();
        
        // Copy assets if needed (first run)
        assetCopier.copyAssetsIfNeeded();
        
        // Add touch controls
        setupTouchControls();
        
        // Fullscreen
        enableFullscreen();
    }
    
    private void setupGamePath() {
        String gamePath = assetCopier.getGamePath();
        
        // Create directories
        new File(gamePath).mkdirs();
        new File(gamePath + "/addons").mkdirs();
        new File(gamePath + "/demos").mkdirs();
        new File(gamePath + "/luafiles").mkdirs();
        
        // Set environment for native code
        try {
            nativeSetEnv("SRB2HOME", gamePath);
            nativeSetEnv("RINGRACERSHOME", gamePath);
        } catch (UnsatisfiedLinkError e) {
            // Native lib not loaded yet, that's fine
        }
    }
    
    private void setupTouchControls() {
        touchControls = new TouchControlsView(this);
        touchControls.setTouchControlsListener(this);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        
        if (mLayout != null) {
            mLayout.addView(touchControls, params);
        }
    }
    
    private void enableFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enableFullscreen();
    }
    
    // Touch controls callbacks
    @Override
    public void onDPadChanged(int direction) {
        float x = 0, y = 0;
        if ((direction & TouchControlsView.DPAD_LEFT) != 0) x = -1f;
        if ((direction & TouchControlsView.DPAD_RIGHT) != 0) x = 1f;
        if ((direction & TouchControlsView.DPAD_UP) != 0) y = -1f;
        if ((direction & TouchControlsView.DPAD_DOWN) != 0) y = 1f;
        
        TouchControlsView.nativeSendAxis(0, x);
        TouchControlsView.nativeSendAxis(1, y);
    }
    
    @Override
    public void onButtonPressed(int buttonId, boolean pressed) {
        int key = 0;
        switch (buttonId) {
            case TouchControlsView.BTN_ACCELERATE: key = 57; break; // Space
            case TouchControlsView.BTN_BRAKE: key = 29; break;      // Ctrl
            case TouchControlsView.BTN_DRIFT: key = 42; break;      // Shift
            case TouchControlsView.BTN_ITEM: key = 28; break;       // Enter
        }
        
        if (key != 0) {
            TouchControlsView.nativeSendKey(key, pressed);
            if (pressed) vibrate();
        }
    }
    
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(15);
            }
        }
    }
    
    @Override
    protected String[] getLibraries() {
        return new String[] { "SDL2", "ringracers" };
    }
    
    @Override
    protected String[] getArguments() {
        String gamePath = assetCopier.getGamePath();
        return new String[] { "-home", gamePath };
    }
}
