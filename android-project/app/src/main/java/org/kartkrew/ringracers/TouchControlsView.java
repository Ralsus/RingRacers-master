package org.kartkrew.ringracers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Touch controls overlay for Ring Racers.
 */
public class TouchControlsView extends View {
    
    public static final int BTN_ACCELERATE = 1;
    public static final int BTN_BRAKE = 2;
    public static final int BTN_DRIFT = 3;
    public static final int BTN_ITEM = 4;
    
    public static final int DPAD_NONE = 0;
    public static final int DPAD_LEFT = 1;
    public static final int DPAD_RIGHT = 2;
    public static final int DPAD_UP = 4;
    public static final int DPAD_DOWN = 8;
    
    private Paint paintDPad, paintButton, paintPressed, paintText;
    
    private float dpadCenterX, dpadCenterY, dpadRadius = 100f;
    private int currentDPadState = DPAD_NONE;
    private int dpadPointerId = -1;
    
    private Map<Integer, RectF> buttonRects = new HashMap<>();
    private Map<Integer, Boolean> buttonStates = new HashMap<>();
    private Map<Integer, Integer> buttonPointerIds = new HashMap<>();
    private Map<Integer, String> buttonLabels = new HashMap<>();
    
    public interface TouchControlsListener {
        void onDPadChanged(int direction);
        void onButtonPressed(int buttonId, boolean pressed);
    }
    
    private TouchControlsListener listener;
    private boolean isVisible = true;
    
    public TouchControlsView(Context context) { super(context); init(); }
    public TouchControlsView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    
    private void init() {
        paintDPad = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDPad.setColor(Color.argb(80, 100, 100, 255));
        paintDPad.setStyle(Paint.Style.FILL);
        
        paintButton = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintButton.setColor(Color.argb(80, 100, 100, 100));
        paintButton.setStyle(Paint.Style.FILL);
        
        paintPressed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPressed.setColor(Color.argb(180, 50, 200, 50));
        paintPressed.setStyle(Paint.Style.FILL);
        
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(28f);
        paintText.setTextAlign(Paint.Align.CENTER);
        
        buttonLabels.put(BTN_ACCELERATE, "GAS");
        buttonLabels.put(BTN_BRAKE, "BRAKE");
        buttonLabels.put(BTN_DRIFT, "DRIFT");
        buttonLabels.put(BTN_ITEM, "ITEM");
        
        for (int id : buttonLabels.keySet()) {
            buttonStates.put(id, false);
            buttonPointerIds.put(id, -1);
        }
    }
    
    public void setTouchControlsListener(TouchControlsListener l) { listener = l; }
    public void setControlsVisible(boolean v) { isVisible = v; invalidate(); }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float margin = 50f;
        dpadCenterX = margin + dpadRadius;
        dpadCenterY = h - margin - dpadRadius;
        
        float btnSize = 90f;
        float rightX = w - margin - btnSize * 2;
        float bottomY = h - margin;
        
        buttonRects.put(BTN_ACCELERATE, new RectF(rightX + btnSize, bottomY - btnSize * 2, rightX + btnSize * 2, bottomY - btnSize));
        buttonRects.put(BTN_BRAKE, new RectF(rightX, bottomY - btnSize, rightX + btnSize, bottomY));
        buttonRects.put(BTN_DRIFT, new RectF(rightX - btnSize, bottomY - btnSize * 2, rightX, bottomY - btnSize));
        buttonRects.put(BTN_ITEM, new RectF(rightX, bottomY - btnSize * 3, rightX + btnSize, bottomY - btnSize * 2));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (!isVisible) return;
        
        // D-Pad
        canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, paintDPad);
        
        // Direction indicators
        Paint dirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dirPaint.setColor(Color.WHITE);
        dirPaint.setTextSize(40f);
        dirPaint.setTextAlign(Paint.Align.CENTER);
        
        int alpha = (currentDPadState & DPAD_LEFT) != 0 ? 255 : 100;
        dirPaint.setAlpha(alpha);
        canvas.drawText("◀", dpadCenterX - 50, dpadCenterY + 15, dirPaint);
        
        alpha = (currentDPadState & DPAD_RIGHT) != 0 ? 255 : 100;
        dirPaint.setAlpha(alpha);
        canvas.drawText("▶", dpadCenterX + 50, dpadCenterY + 15, dirPaint);
        
        alpha = (currentDPadState & DPAD_UP) != 0 ? 255 : 100;
        dirPaint.setAlpha(alpha);
        canvas.drawText("▲", dpadCenterX, dpadCenterY - 40, dirPaint);
        
        alpha = (currentDPadState & DPAD_DOWN) != 0 ? 255 : 100;
        dirPaint.setAlpha(alpha);
        canvas.drawText("▼", dpadCenterX, dpadCenterY + 60, dirPaint);
        
        // Buttons
        for (Map.Entry<Integer, RectF> entry : buttonRects.entrySet()) {
            int id = entry.getKey();
            RectF rect = entry.getValue();
            boolean pressed = buttonStates.getOrDefault(id, false);
            
            Paint p = pressed ? paintPressed : paintButton;
            if (!pressed) {
                switch (id) {
                    case BTN_ACCELERATE: p.setColor(Color.argb(100, 50, 200, 50)); break;
                    case BTN_BRAKE: p.setColor(Color.argb(100, 200, 50, 50)); break;
                    case BTN_DRIFT: p.setColor(Color.argb(100, 50, 50, 200)); break;
                    case BTN_ITEM: p.setColor(Color.argb(100, 200, 200, 50)); break;
                }
            }
            
            canvas.drawRoundRect(rect, 10f, 10f, p);
            canvas.drawText(buttonLabels.get(id), rect.centerX(), rect.centerY() + 10, paintText);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isVisible) return false;
        
        int action = event.getActionMasked();
        int idx = event.getActionIndex();
        int pid = event.getPointerId(idx);
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                handleDown(event.getX(idx), event.getY(idx), pid);
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (event.getPointerId(i) == dpadPointerId) {
                        updateDPad(event.getX(i), event.getY(i));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                handleUp(pid);
                break;
            case MotionEvent.ACTION_CANCEL:
                resetAll();
                break;
        }
        invalidate();
        return true;
    }
    
    private void handleDown(float x, float y, int pid) {
        float dx = x - dpadCenterX, dy = y - dpadCenterY;
        if (Math.sqrt(dx*dx + dy*dy) <= dpadRadius * 1.2f) {
            dpadPointerId = pid;
            updateDPad(x, y);
            return;
        }
        
        for (Map.Entry<Integer, RectF> e : buttonRects.entrySet()) {
            if (e.getValue().contains(x, y)) {
                int id = e.getKey();
                buttonStates.put(id, true);
                buttonPointerIds.put(id, pid);
                if (listener != null) listener.onButtonPressed(id, true);
                return;
            }
        }
    }
    
    private void handleUp(int pid) {
        if (pid == dpadPointerId) {
            dpadPointerId = -1;
            currentDPadState = DPAD_NONE;
            if (listener != null) listener.onDPadChanged(DPAD_NONE);
        }
        
        for (Map.Entry<Integer, Integer> e : buttonPointerIds.entrySet()) {
            if (e.getValue() == pid) {
                int id = e.getKey();
                buttonStates.put(id, false);
                buttonPointerIds.put(id, -1);
                if (listener != null) listener.onButtonPressed(id, false);
            }
        }
    }
    
    private void updateDPad(float x, float y) {
        float dx = x - dpadCenterX, dy = y - dpadCenterY;
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        
        int state = DPAD_NONE;
        if (dist > 25f) {
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            if (angle >= -60 && angle < 60) state |= DPAD_RIGHT;
            if (angle >= 120 || angle < -120) state |= DPAD_LEFT;
            if (angle >= 30 && angle < 150) state |= DPAD_DOWN;
            if (angle >= -150 && angle < -30) state |= DPAD_UP;
        }
        
        if (state != currentDPadState) {
            currentDPadState = state;
            if (listener != null) listener.onDPadChanged(state);
        }
    }
    
    private void resetAll() {
        dpadPointerId = -1;
        currentDPadState = DPAD_NONE;
        if (listener != null) listener.onDPadChanged(DPAD_NONE);
        
        for (int id : buttonStates.keySet()) {
            if (buttonStates.get(id)) {
                buttonStates.put(id, false);
                buttonPointerIds.put(id, -1);
                if (listener != null) listener.onButtonPressed(id, false);
            }
        }
    }
    
    // Native methods
    public static native void nativeSendKey(int key, boolean pressed);
    public static native void nativeSendAxis(int axis, float value);
}
