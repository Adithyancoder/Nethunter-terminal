package com.offsec.nhterm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal non-deprecated replacement for Android's deprecated KeyboardView/Keyboard.
 * - Renders a simple QWERTY-like keyboard layout.
 * - Provides a listener interface similar to the old KeyboardView.OnKeyboardActionListener.
 *
 * This is intentionally small and self-contained so it can be used as a drop-in replacement
 * for the small in-app keyboard used by the project. It doesn't try to be a full-featured
 * IME; it's an in-app keyboard view.
 */
public class CustomKeyboardView extends View {
  public static class Key {
    public Rect rect = new Rect();
    public int code; // primary code
    public String label;
    public boolean sticky = false;
    public boolean on = false;
  }

  private final Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final List<Key> keys = new ArrayList<>();
  private KeyboardActionListener listener = null;
  private int pressedKey = -1;

  public interface KeyboardActionListener {
    void onPress(int primaryCode);
    void onRelease(int primaryCode);
    void onKey(int primaryCode, int[] keyCodes);
    void onText(CharSequence text);
    void swipeLeft();
    void swipeRight();
    void swipeDown();
    void swipeUp();
  }

  public CustomKeyboardView(Context context) {
    super(context);
    init();
  }

  public CustomKeyboardView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    keyPaint.setStyle(Paint.Style.FILL);
    keyPaint.setColor(Color.LTGRAY);
    textPaint.setColor(Color.BLACK);
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setTextSize(36f);
    setFocusable(true);
    setClickable(true);
  }

  /**
   * Loads a simple keyboard layout. The provided resource id is ignored in this minimal
   * implementation; we only provide a single QWERTY-like layout. If you want per-resource
   * layouts, extend this method to parse resources.
   */
  public void loadKeyboardLayout(int unusedResId) {
    keys.clear();
    // Basic 3-row qwerty lower-case; add space row
    String[] rows = new String[]{"qwertyuiop", "asdfghjkl", "zxcvbnm"};
    for (String r : rows) {
      for (char c : r.toCharArray()) {
        Key k = new Key();
        k.code = (int) c;
        k.label = String.valueOf(c);
        keys.add(k);
      }
    }
    // add space key as a wide key
    Key space = new Key();
    space.code = ' ';
    space.label = " ";
    keys.add(space);
    requestLayout();
    invalidate();
  }

  public List<Key> getKeys() {
    return keys;
  }

  public void setKeyboardActionListener(KeyboardActionListener l) {
    this.listener = l;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (keys.isEmpty()) return;
    // Layout keys into rows: first 10, next 9, next 7, then space at bottom center
    int padding = 8;
    int rowCount = 4; // including space row
    int[] rowCounts = new int[]{10, 9, 7, 1};
    int y = padding;
    int rowHeight = (h - padding * (rowCount + 1)) / rowCount;
    int idx = 0;
    for (int r = 0; r < rowCounts.length; r++) {
      int count = rowCounts[r];
      int keyWidth = (w - padding * (count + 1)) / Math.max(1, count);
      int x = padding;
      for (int c = 0; c < count && idx < keys.size(); c++) {
        Key k = keys.get(idx);
        k.rect.left = x;
        k.rect.top = y;
        k.rect.right = x + keyWidth;
        k.rect.bottom = y + rowHeight;
        x += keyWidth + padding;
        idx++;
      }
      y += rowHeight + padding;
    }
    // If there are leftover keys (shouldn't happen), map them sequentially
    while (idx < keys.size()) {
      Key k = keys.get(idx);
      k.rect.left = padding;
      k.rect.top = y;
      k.rect.right = w - padding;
      k.rect.bottom = y + rowHeight;
      y += rowHeight + padding;
      idx++;
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (Key k : keys) {
      keyPaint.setColor(k.on ? Color.DKGRAY : Color.LTGRAY);
      canvas.drawRect(k.rect, keyPaint);
      float cx = (k.rect.left + k.rect.right) / 2f;
      float cy = (k.rect.top + k.rect.bottom) / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
      canvas.drawText(k.label, cx, cy, textPaint);
    }
  }

  private int findKeyIndex(int x, int y) {
    for (int i = 0; i < keys.size(); i++) {
      Key k = keys.get(i);
      if (k.rect.contains(x, y)) return i;
    }
    return -1;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    int x = (int) event.getX();
    int y = (int) event.getY();
    int idx = findKeyIndex(x, y);
    if (action == MotionEvent.ACTION_DOWN) {
      pressedKey = idx;
      if (pressedKey >= 0) {
        Key k = keys.get(pressedKey);
        if (listener != null) listener.onPress(k.code);
        invalidate(k.rect);
      }
      return true;
    } else if (action == MotionEvent.ACTION_UP) {
      if (pressedKey >= 0 && pressedKey < keys.size()) {
        Key k = keys.get(pressedKey);
        if (listener != null) {
          listener.onRelease(k.code);
          listener.onKey(k.code, new int[]{k.code});
        }
        invalidate(k.rect);
      }
      pressedKey = -1;
      performClick();
      return true;
    } else if (action == MotionEvent.ACTION_MOVE) {
      // ignore for now
      return true;
    }
    return super.onTouchEvent(event);
  }

  @Override
  public boolean performClick() {
    super.performClick();
    return true;
  }
}

