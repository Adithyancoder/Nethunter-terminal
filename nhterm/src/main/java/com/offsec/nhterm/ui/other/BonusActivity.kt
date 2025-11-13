package com.offsec.nhterm.ui.other

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.*
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.offsec.nhterm.R

/**
 * @author kiva
 */

class BonusActivity : AppCompatActivity() {
  private lateinit var mLayout: FrameLayout
  private var mTapCount: Int = 0
  private var mKeyCount: Int = 0
  private var mInterpolator = PathInterpolator(0f, 0f, 0.5f, 1f)

  private fun makeRipple(): Drawable {
    val idx = newColorIndex()
    val lollipopBackground = ShapeDrawable(OvalShape())
    lollipopBackground.paint.color = FLAVORS[idx]
    return RippleDrawable(
      ColorStateList.valueOf(FLAVORS[idx + 1]),
      lollipopBackground, null
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mLayout = FrameLayout(this)
    setContentView(mLayout)

    // Make activity fullscreen (hide status and navigation bars) using WindowInsets API
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val controller = WindowInsetsControllerCompat(window, mLayout)
    controller.hide(
      WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
    )
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
  }

  override fun onAttachedToWindow() {
    val dm = resources.displayMetrics
    val dp = dm.density
    val size = (dm.widthPixels.coerceAtMost(dm.heightPixels).toFloat().coerceAtMost(600 * dp) - 100 * dp).toInt()
    val stick = object : View(this) {
      var mPaint = Paint()
      var mShadow = Path()
      // Preallocate gradient drawable and colors to avoid allocations in onDraw
      private val gradient = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.LEFT_RIGHT
        colors = intArrayOf(0xFFFFFFFF.toInt(), 0xFFAAAAAA.toInt())
      }

      public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setWillNotDraw(false)
        outlineProvider = object : ViewOutlineProvider() {
          override fun getOutline(view: View, outline: Outline) {
            outline.setRect(0, height / 2, width, height)
          }
        }
      }

      public override fun onDraw(c: Canvas) {
        val w = width
        val h = height / 2
        c.translate(0f, h.toFloat())
        // Update pre-allocated gradient for current size/center and draw
        gradient.setGradientCenter(w * 0.75f, 0f)
        gradient.setBounds(0, 0, w, h)
        gradient.draw(c)
        mPaint.color = 0xFFAAAAAA.toInt()
        mShadow.reset()
        mShadow.moveTo(0f, 0f)
        mShadow.lineTo(w.toFloat(), 0f)
        mShadow.lineTo(w.toFloat(), size / 2 + 1.5f * w)
        mShadow.lineTo(0f, (size / 2).toFloat())
        mShadow.close()
        c.drawPath(mShadow, mPaint)
      }
    }
    mLayout.addView(
      stick, FrameLayout.LayoutParams(
        (32 * dp).toInt(),
        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL
      )
    )
    stick.alpha = 0f

    val im = ImageView(this)
    im.translationZ = 20f
    im.scaleX = 0f
    im.scaleY = 0f
    val platlogo = androidx.appcompat.content.res.AppCompatResources.getDrawable(this, R.drawable.ic_kali_logo)
    platlogo?.alpha = 0
    im.setImageDrawable(platlogo)
    im.background = makeRipple()
    im.isClickable = true
    val highlight = ShapeDrawable(OvalShape())
    highlight.paint.color = 0x10FFFFFF
    highlight.setBounds(
      (size * .15f).toInt(), (size * .15f).toInt(),
      (size * .6f).toInt(), (size * .6f).toInt()
    )
    im.overlay.add(highlight)
    im.setOnClickListener {
      if (mTapCount == 0) {
        im.animate()
          .translationZ(40f)
          .scaleX(1f)
          .scaleY(1f)
          .setInterpolator(mInterpolator)
          .setDuration(700)
          .setStartDelay(500)
          .start()

        platlogo?.let { d ->
          val a = ObjectAnimator.ofInt(d, "alpha", 0, 255)
          a.interpolator = mInterpolator
          a.startDelay = 1000
          a.start()
        }

        stick.animate()
          .translationZ(20f)
          .alpha(1f)
          .setInterpolator(mInterpolator)
          .setDuration(700)
          .setStartDelay(750)
          .start()
      } else {
        im.background = makeRipple()
      }
      mTapCount++
    }

    // Enable hardware keyboard input for TV compatibility.
    im.isFocusable = true
    im.requestFocus()
    im.setOnKeyListener { _, keyCode, event ->
      if (keyCode != KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
        ++mKeyCount
        if (mKeyCount > 2) {
          if (mTapCount > 5) {
            im.performLongClick()
          } else {
            im.performClick()
          }
        }
        true
      } else {
        false
      }
    }

    mLayout.addView(im, FrameLayout.LayoutParams(size, size, Gravity.CENTER))

    im.animate().scaleX(0.3f).scaleY(0.3f)
      .setInterpolator(mInterpolator)
      .setDuration(500)
      .setStartDelay(800)
      .start()
  }

  companion object {
    internal val FLAVORS = intArrayOf(
      0xFF9C27B0.toInt(), 0xFFBA68C8.toInt(), // grape
      0xFFFF9800.toInt(), 0xFFFFB74D.toInt(), // orange
      0xFFF06292.toInt(), 0xFFF8BBD0.toInt(), // bubblegum
      0xFFAFB42B.toInt(), 0xFFCDDC39.toInt(), // lime
      0xFF795548.toInt(), 0xFFA1887F.toInt()
    )// mystery flavor

    internal fun newColorIndex(): Int {
      return 2 * (Math.random() * FLAVORS.size / 2).toInt()
    }
  }
}
