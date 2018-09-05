package com.sidbola.cryptrack.features.shared.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller

class SwipeCancelViewPager : ViewPager {

    private var canSwipe = true

    constructor(context: Context) : super(context) {
        setMyScroller()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setMyScroller()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (canSwipe) {
            return super.onInterceptTouchEvent(event)
        }

        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (canSwipe) {
            return super.onTouchEvent(event)
        }

        return false
    }

    fun swipeEnabled(enabled: Boolean) {
        canSwipe = enabled
    }

    // down one is added for smooth scrolling

    private fun setMyScroller() {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            scroller.set(this, MyScroller(context))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class MyScroller(context: Context) : Scroller(context, DecelerateInterpolator()) {

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/)
        }
    }
}