package com.joshua.demo.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.Adapter
import android.widget.ListView
import android.widget.SectionIndexer

class IndexAlphabeticalScroller(context: Context, lv: ListView?) {
    private val indexBarWidth: Float
    private val indexBarMargin: Float
    private val previewPadding: Float
    private val density: Float = context.resources.displayMetrics.density
    private val scaledDensity: Float = context.resources.displayMetrics.scaledDensity
    private var alphaRate = 0f
    private var state = STATE_HIDDEN
    private var listViewWidth = 0
    private var listViewHeight = 0
    private var currentSection = -1
    private var isIndexing = false
    private var listView: ListView? = null
    private var indexer: SectionIndexer? = null
    private var sections: Array<String>? = null
    private var indexBarRect: RectF? = null
    private val halve = 2
    fun draw(canvas: Canvas) {
        if (state == STATE_HIDDEN) return
        val indexbarPaint = Paint()
        indexbarPaint.color = Color.BLACK
        indexbarPaint.alpha = (64 * alphaRate).toInt()
        indexbarPaint.isAntiAlias = true
        canvas.drawRoundRect(indexBarRect!!, 5 * density, 5 * density, indexbarPaint)
        if (sections != null && sections!!.size > 0) {
            if (currentSection >= 0) {
                val previewPaint = Paint()
                previewPaint.color = Color.BLACK
                previewPaint.alpha = 96
                previewPaint.isAntiAlias = true
                previewPaint.setShadowLayer(3f, 0f, 0f, Color.argb(64, 0, 0, 0))
                val previewTextPaint = Paint()
                previewTextPaint.color = Color.WHITE
                previewTextPaint.isAntiAlias = true
                previewTextPaint.textSize = 50 * scaledDensity
                val previewTextWidth = previewTextPaint.measureText(sections!![currentSection])
                val previewSize = 2 * previewPadding + previewTextPaint.descent() - previewTextPaint.ascent()
                val previewRect = RectF((listViewWidth - previewSize) / halve, (listViewHeight - previewSize) / halve, (listViewWidth - previewSize) / halve + previewSize, (listViewHeight - previewSize) / halve + previewSize)
                canvas.drawRoundRect(previewRect, 5 * density, 5 * density, previewPaint)
                canvas.drawText(sections!![currentSection], previewRect.left + (previewSize - previewTextWidth) / halve - 1, previewRect.top + previewPadding - previewTextPaint.ascent() + 1, previewTextPaint)
            }
            val indexPaint = Paint()
            indexPaint.color = Color.WHITE
            indexPaint.alpha = (255 * alphaRate).toInt()
            indexPaint.isAntiAlias = true
            indexPaint.textSize = 12 * scaledDensity
            val sectionHeight = (indexBarRect!!.height() - 2 * indexBarMargin) / sections!!.size
            val paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / halve
            for (i in sections!!.indices) {
                val paddingLeft = (indexBarWidth - indexPaint.measureText(sections!![i])) / halve
                canvas.drawText(sections!![i], indexBarRect!!.left + paddingLeft, indexBarRect!!.top + indexBarMargin + sectionHeight * i + paddingTop - indexPaint.ascent(), indexPaint)
            }
        }
    }

    fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> if (state != STATE_HIDDEN && contains(ev.x, ev.y)) {
                setState(STATE_SHOWN)
                isIndexing = true
                currentSection = getSectionByPoint(ev.y)
                listView!!.setSelection(indexer!!.getPositionForSection(currentSection))
                return true
            }
            MotionEvent.ACTION_MOVE -> if (isIndexing) {
                if (contains(ev.x, ev.y)) {
                    currentSection = getSectionByPoint(ev.y)
                    listView!!.setSelection(indexer!!.getPositionForSection(currentSection))
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isIndexing) {
                    isIndexing = false
                    currentSection = -1
                }
                if (state == STATE_SHOWN) setState(STATE_HIDING)
            }
        }
        return false
    }

    fun onSizeChanged(width: Int, high: Int, oldWidth: Int, oldHigh: Int) {
        listViewWidth = width
        listViewHeight = high
        indexBarRect = RectF(width - indexBarMargin - indexBarWidth, indexBarMargin, width - indexBarMargin, high - indexBarMargin)
    }

    fun show() {
        if (state == STATE_HIDDEN) setState(STATE_SHOWING) else if (state == STATE_HIDING) setState(STATE_HIDING)
    }

    fun hide() {
        if (state == STATE_SHOWN) setState(STATE_HIDING)
    }

    fun setAdapter(adapter: Adapter?) {
        if (adapter is SectionIndexer) {
            indexer = adapter
            sections = indexer!!.sections as Array<String>
        }
    }

    private fun setState(state: Int) {
        if (state < STATE_HIDDEN || state > STATE_HIDING) return
        this.state = state
        when (this.state) {
            STATE_HIDDEN -> mHandler.removeMessages(0)
            STATE_SHOWING -> {
                alphaRate = 0f
                fade(0)
            }
            STATE_SHOWN -> mHandler.removeMessages(0)
            STATE_HIDING -> {
                alphaRate = 1f
                fade(2000)
            }
        }
    }

    fun contains(x: Float, y: Float): Boolean {
        return x >= indexBarRect!!.left && y >= indexBarRect!!.top && y <= indexBarRect!!.top + indexBarRect!!.height()
    }

    private fun getSectionByPoint(y: Float): Int {
        if (sections == null || sections!!.isEmpty()) return 0
        if (y < indexBarRect!!.top + indexBarMargin) return 0
        return if (y >= indexBarRect!!.top + indexBarRect!!.height() - indexBarMargin) sections!!.size - 1 else ((y - indexBarRect!!.top - indexBarMargin) / ((indexBarRect!!.height() - 2 * indexBarMargin) / sections!!.size)).toInt()
    }

    private fun fade(delay: Long) {
        mHandler.removeMessages(0)
        mHandler.sendEmptyMessageAtTime(0, SystemClock.uptimeMillis() + delay)
    }

    private val mHandler: Handler = object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (state) {
                STATE_SHOWING -> {
                    alphaRate += (1 - alphaRate) * 0.2.toFloat()
                    if (alphaRate > 0.9) {
                        alphaRate = 1f
                        setState(STATE_SHOWN)
                    }
                    listView!!.invalidate()
                    fade(10)
                }
                STATE_SHOWN -> setState(STATE_HIDING)
                STATE_HIDING -> {
                    alphaRate -= alphaRate * 0.2.toFloat()
                    if (alphaRate < 0.1) {
                        alphaRate = 0f
                        setState(STATE_HIDDEN)
                    }
                    listView!!.invalidate()
                    fade(10)
                }
            }
        }
    }

    companion object {
        private const val STATE_HIDDEN = 0
        private const val STATE_SHOWING = 1
        private const val STATE_SHOWN = 2
        private const val STATE_HIDING = 3
    }

    init {
        listView = lv
        setAdapter(listView!!.adapter)
        indexBarWidth = 20 * density
        indexBarMargin = 10 * density
        previewPadding = 5 * density
    }
}