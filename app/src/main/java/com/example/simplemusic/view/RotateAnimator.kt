package com.example.simplemusic.view

import android.content.Context
import android.graphics.Canvas
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.Transformation
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

class RotateAnimator(context: Context?, imageView: ImageView?, imageView1: ImageView?) :
    AppCompatImageView(
        context!!
    ) {
    var imageView: ImageView? = null
    var Needle: ImageView? = null
    private var angle = 0f
    private var angle2 = 0f
    private var viewHeight = 0f
    private var viewWidth = 0f
    private var musicAnim: MusicAnim? = null
    fun init(imageView: ImageView?, imageView1: ImageView?) {
        this.imageView = imageView
        Needle = imageView1
        angle2 = 0f
        angle = angle2
        viewWidth = 1f
        viewHeight = viewWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewHeight = measuredHeight.toFloat()
        viewWidth = measuredWidth.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(angle2, viewWidth / 2, viewHeight / 2)
        super.onDraw(canvas)
    }

    inner class MusicAnim(fromD: Float, toD: Float, pivotX: Float, pivotY: Float) :
        RotateAnimation(fromD, toD, RELATIVE_TO_SELF, pivotX, RELATIVE_TO_SELF, pivotY) {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            angle = interpolatedTime * 360
        }
    }

    fun set_Needle() {
        val animation1 = RotateAnimation(
            0f,
            -35f,
            Animation.RELATIVE_TO_SELF,
            0.1f,
            Animation.RELATIVE_TO_SELF,
            0.15f
        )
        animation1.duration = 1
        animation1.repeatCount = 0
        animation1.fillAfter = true
        animation1.startOffset = 0
        Needle!!.startAnimation(animation1)
    }

    val animation = RotateAnimation(
        -35f,
        0f,
        Animation.RELATIVE_TO_SELF,
        0.1f,
        Animation.RELATIVE_TO_SELF,
        0.15f
    )

    init {
        init(imageView, imageView1)
    }

    private fun settime() {
        //animation.setInterpolator(new LinearInterpolator());
        animation.duration = 400
        animation.repeatCount = 0
        animation.startOffset = 0
        animation.fillAfter = true
    }

    fun playAnimator() {
        settime()
        musicAnim = MusicAnim(0f, 3600000f, viewWidth / 2, viewHeight / 2)
        musicAnim!!.duration = 360000000
        musicAnim!!.interpolator = LinearInterpolator()
        musicAnim!!.repeatCount = -1
        Needle!!.startAnimation(animation)
        imageView!!.startAnimation(musicAnim)
        invalidate()
    }

    fun pauseAnimator() {
        angle2 = (angle2 + angle) % 360
        musicAnim!!.cancel()
        animation.cancel()
        invalidate()
    }
}