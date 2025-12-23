package org.libsdl.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

/**
 * SDLActivity - Base class for SDL2 on Android.
 * Based on SDL2 Android project template.
 */
public class SDLActivity extends Activity implements SurfaceHolder.Callback, 
        View.OnKeyListener, View.OnTouchListener {
    
    private static final String TAG = "SDL";
    
    // Main components
    protected static SDLActivity mSingleton;
    protected static SDLSurface mSurface;
    public static FrameLayout mLayout;
    protected static Thread mSDLThread;
    
    // Native functions (implemented in SDL library)
    public static native int nativeInit(Object arguments);
    public static native void nativeLowMemory();
    public static native void nativeQuit();
    public static native void nativePause();
    public static native void nativeResume();
    public static native void onNativeResize(int x, int y, int format, float rate);
    public static native void onNativeKeyDown(int keycode);
    public static native void onNativeKeyUp(int keycode);
    public static native void onNativeTouch(int touchDevId, int pointerFingerId,
                                            int action, float x, float y, float p);
    public static native void onNativeSurfaceCreated();
    public static native void onNativeSurfaceChanged();
    public static native void onNativeSurfaceDestroyed();
    
    // State
    protected static boolean mIsPaused = false;
    protected static boolean mHasFocus = true;
    
    /**
     * Get libraries to load. Override in subclass.
     */
    protected String[] getLibraries() {
        return new String[] { "SDL2", "main" };
    }
    
    /**
     * Get command line arguments. Override in subclass.
     */
    protected String[] getArguments() {
        return new String[0];
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSingleton = this;
        
        // Load native libraries
        loadLibraries();
        
        // Create layout
        mLayout = new FrameLayout(this);
        setContentView(mLayout);
        
        // Create SDL surface
        mSurface = new SDLSurface(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        mLayout.addView(mSurface, params);
        
        mSurface.setOnKeyListener(this);
        mSurface.setOnTouchListener(this);
    }
    
    protected void loadLibraries() {
        for (String lib : getLibraries()) {
            try {
                System.loadLibrary(lib);
                Log.v(TAG, "Loaded library: " + lib);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Failed to load " + lib + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        if (mSurface != null) {
            mSurface.handleResume();
        }
    }
    
    @Override
    protected void onPause() {
        mIsPaused = true;
        if (mSurface != null) {
            mSurface.handlePause();
        }
        super.onPause();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mHasFocus = hasFocus;
    }
    
    @Override
    protected void onDestroy() {
        if (mSDLThread != null) {
            try {
                nativeQuit();
                mSDLThread.join(1000);
            } catch (Exception e) {
                Log.e(TAG, "Exception on destroy: " + e.getMessage());
            }
        }
        super.onDestroy();
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try { nativeLowMemory(); } catch (Exception e) {}
    }
    
    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surfaceCreated");
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "surfaceChanged: " + width + "x" + height);
        
        try {
            onNativeResize(width, height, format, 60.0f);
        } catch (Exception e) {}
        
        // Start SDL thread if not running
        if (mSDLThread == null) {
            mSDLThread = new Thread(new SDLMain(), "SDLThread");
            mSDLThread.start();
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed");
        try { onNativeSurfaceDestroyed(); } catch (Exception e) {}
    }
    
    // Input handlers
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            try { onNativeKeyDown(keyCode); } catch (Exception e) {}
            return true;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            try { onNativeKeyUp(keyCode); } catch (Exception e) {}
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();
        
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = event.getPointerId(i);
            float x = event.getX(i);
            float y = event.getY(i);
            float p = event.getPressure(i);
            
            try {
                onNativeTouch(0, pointerId, action, x, y, p);
            } catch (Exception e) {}
        }
        
        return true;
    }
    
    // Static helper
    public static Surface getNativeSurface() {
        if (mSurface != null) {
            return mSurface.getHolder().getSurface();
        }
        return null;
    }
    
    /**
     * SDL Main Thread
     */
    class SDLMain implements Runnable {
        @Override
        public void run() {
            try {
                onNativeSurfaceCreated();
                onNativeSurfaceChanged();
                
                String[] args = getArguments();
                nativeInit(args);
            } catch (Exception e) {
                Log.e(TAG, "SDL thread exception: " + e.getMessage());
            }
            
            Log.v(TAG, "SDL thread finished");
        }
    }
    
    /**
     * SDL Surface View
     */
    static class SDLSurface extends SurfaceView implements SurfaceHolder.Callback {
        
        public SDLSurface(Context context) {
            super(context);
            getHolder().addCallback(this);
            getHolder().setFormat(PixelFormat.RGBA_8888);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }
        
        public void handleResume() {
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            try { SDLActivity.nativeResume(); } catch (Exception e) {}
        }
        
        public void handlePause() {
            try { SDLActivity.nativePause(); } catch (Exception e) {}
        }
        
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mSingleton != null) {
                mSingleton.surfaceCreated(holder);
            }
        }
        
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mSingleton != null) {
                mSingleton.surfaceChanged(holder, format, width, height);
            }
        }
        
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mSingleton != null) {
                mSingleton.surfaceDestroyed(holder);
            }
        }
    }
}
