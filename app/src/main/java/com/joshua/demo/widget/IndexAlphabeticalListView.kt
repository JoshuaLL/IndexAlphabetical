package com.joshua.demo.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.ListAdapter
import android.widget.ListView

class IndexAlphabeticalListView : ListView {
    private var mIsFastScrollEnabled = false
    private var mScroller: IndexAlphabeticalScroller? = null
    private var mGestureDetector: GestureDetector? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun isFastScrollEnabled(): Boolean {
        return mIsFastScrollEnabled
    }

    override fun setFastScrollEnabled(enabled: Boolean) {
        mIsFastScrollEnabled = enabled
        if (mIsFastScrollEnabled) {
            if (mScroller == null) mScroller = IndexAlphabeticalScroller(context, this)
        } else {
            if (mScroller != null) {
                mScroller!!.hide()
                mScroller = null
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Overlay index bar
        if (mScroller != null) mScroller!!.draw(canvas)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Intercept ListView's touch event
        if (mScroller != null && mScroller!!.onTouchEvent(ev)) return true
        if (mGestureDetector == null) {
            mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onFling(e1: MotionEvent, e2: MotionEvent,
                                     velocityX: Float, velocityY: Float): Boolean {
                    // If fling happens, index bar shows
                    if (mScroller != null) mScroller!!.show()
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            })
        }
        mGestureDetector!!.onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (mScroller!!.contains(ev.x, ev.y)) true else super.onInterceptTouchEvent(ev)
    }

    override fun setAdapter(adapter: ListAdapter) {
        super.setAdapter(adapter)
        if (mScroller != null) mScroller!!.setAdapter(adapter)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mScroller != null) mScroller!!.onSizeChanged(w, h, oldw, oldh)
    }
}