package com.baseuilibrary

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.LinearInterpolator

class CustomRuler(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_PRECISION_WIDTH = 2.0F
        private const val DEFAULT_CURRENT_PRECISION_WIDTH = 4.0F
        private const val DEFAULT_PRECISION_HORIZONTAL_PADDING = 10.0F
        private const val DEFAULT_PRECISION_VERTICAL_PADDING = 20.0F
        private const val DEFAULT_HIGHLIGHT_COLOR_STRING = "#5BB779"
        private const val DEFAULT_RULER_BACKGROUND_COLOR_STRING = "#F6F9F6"
        private const val DEFAULT_CURRENT_PRECISION_COLOR_STRING = "#4ABB73"
        private const val DEFAULT_CURRENT_TEXT_SIZE = 66.0f
        private const val DEFAULT_LONG_PRESSED_ANIMATION_DURATION = 100L
        private const val MIN_FLING_VELOCITY_X = 500.0f
        private const val UNIT_KG = "cm"
        private const val TOUCH_EVENT_LONG_PRESS = 1
    }

    private val mRulerBackgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor(DEFAULT_RULER_BACKGROUND_COLOR_STRING)
    }
    private val mPrecisionPaint = Paint().apply {
        strokeWidth = DEFAULT_PRECISION_WIDTH
        color = Color.parseColor("#0000FF")
    }
    private val mCurrentTextPaint = Paint().apply {
        textSize = DEFAULT_CURRENT_TEXT_SIZE
        color = Color.parseColor(DEFAULT_HIGHLIGHT_COLOR_STRING)
    }
    private val mCurrentTextUnitPaint = Paint().apply {
        textSize = DEFAULT_CURRENT_TEXT_SIZE / 2
        color = Color.parseColor(DEFAULT_HIGHLIGHT_COLOR_STRING)
    }
    private val mCurrentTextPrecisionPaint = Paint().apply {
        strokeWidth = DEFAULT_CURRENT_PRECISION_WIDTH
        color = Color.parseColor(DEFAULT_CURRENT_PRECISION_COLOR_STRING)
        strokeCap = Paint.Cap.ROUND
    }
    private val mPrecisionTextPaint = Paint().apply {
        textSize = DEFAULT_CURRENT_TEXT_SIZE / 2
    }

    private var mLastScrollTime: Long = 0
    private var mIsFlinging = false

    /**
     * unit : mm
     */
    private var mStart = 0
    /**
     * unit : mm
     */
    private var mEnd = 0
    /**
     * unit : mm
     */
    private var mOffsetX = 0
    /**
     * unit : mm
     */
    private var mInitMiddle = 0
    /**
     * record current selected num
     * unit : mm
     */
    private var mCurrentNum = 0

    /**
     * record current num when longPressed, we need this to perform animation
     */
    private var mLastNum = mCurrentNum
    private var mIsLongPressed = false
    private val mEventHandler = EventHandler()
    private var mLastX = 0.0f
    private var mLongPressedStartNum = 0
    private var mLongPressedEndNum = 0

    private var mFirstDraw = true
    private val mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            Log.d("customRuler", "velocityX = $velocityX")
            if (Math.abs(velocityX).compareTo(MIN_FLING_VELOCITY_X) > 0 && !mIsFlinging) {
                val distance = ((e1.x - e2.x)).toInt() / 3
                val duration = Math.abs(distance.toFloat() / velocityX * 10000).toLong()
                val compareResult = mCurrentNum.compareTo(mInitMiddle)

                if (compareResult == 0) {
                    return if (distance > 0) {//move left
                        Log.d("customRuler", "onFling: middle, move left $distance")
                        animatePrecision(distance, duration)
                    } else {//move right
                        Log.d("customRuler", "onFling: middle, move right $distance")
                        animateOffset(distance, duration)
                    }
                }

                if (compareResult < 0) {
                    //Log.d("customRuler", "distance = $distance, offsetDistance = ${mInitMiddle - mCurrentNum}, precisionDistance = ${distance - mInitMiddle + mCurrentNum}")
                    //move left涉及状态改变
                    return if (distance > mInitMiddle - mCurrentNum) {//move left
                        Log.d("customRuler", "onFling: left, move left ${mInitMiddle - mCurrentNum}-> right, move left ${distance - mInitMiddle + mCurrentNum}")
                        animateOffset(mInitMiddle - mCurrentNum, duration
                        ) {
                            animatePrecision(distance - mInitMiddle + mCurrentNum, duration)
                        }
                    } else {//move right or left
                        Log.d("customRuler", "onFling: left, move $distance")
                        animateOffset(distance, duration)
                    }
                }

                if (compareResult > 0) {
                    //Log.d("customRuler", "distance = $distance, precisionDistance = ${mInitMiddle - mCurrentNum}, offsetDistance = ${distance - mInitMiddle + mCurrentNum}")
                    //move right涉及状态改变
                    return if (distance < mInitMiddle - mCurrentNum) {//move right
                        Log.d("customRuler", "onFling: right, move right ${mInitMiddle - mCurrentNum} -> left, move right ${distance - mInitMiddle + mCurrentNum}")
                        animatePrecision(mInitMiddle - mCurrentNum, duration) {
                            animateOffset(distance - mInitMiddle + mCurrentNum, duration)
                        }
                    } else {//move left or right
                        Log.d("customRuler", "onFling: right, move $distance")
                        animatePrecision(distance, duration)
                    }
                }

            }
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val current = System.currentTimeMillis()
            Log.d("customRuler", "onScroll last time = ${current - mLastScrollTime}")
            mLastScrollTime = current
            if (!mIsFlinging) {
                val revisedDistanceX: Int = if (Math.abs(distanceX).compareTo(10.0f) > 0) (distanceX / 10).toInt() else distanceX.toInt()
                val compareResult = mCurrentNum.compareTo(mInitMiddle)

                if (compareResult == 0) {
                    if (revisedDistanceX > 0) {
                        Log.d("customRuler", "onScroll: middle, move left $revisedDistanceX")
                        mStart += revisedDistanceX
                    } else {
                        Log.d("customRuler", "onScroll: middle, move right $revisedDistanceX")
                        mOffsetX += if (mCurrentNum + revisedDistanceX < 0) mCurrentNum else -revisedDistanceX
                    }
                    updateViews()
                } else if (compareResult < 0) {
                    //move left涉及状态转换
                    if (revisedDistanceX > mInitMiddle - mCurrentNum) {
                        Log.d("customRuler", "onScroll: left, move left ${mInitMiddle - mCurrentNum} -> right, move left ${revisedDistanceX - mInitMiddle + mCurrentNum}")
                        mOffsetX = 0
                        updateViews()
                        mStart += revisedDistanceX - mInitMiddle + mCurrentNum
                        updateViews()
                    } else {
                        //move left
                        if (revisedDistanceX > 0) {
                            Log.d("customRuler", "onScroll: left, move left $revisedDistanceX")
                            mOffsetX -= revisedDistanceX
                            updateViews()
                        } else if (mOffsetX != mInitMiddle) {//move right
                            Log.d("customRuler", "onScroll: left, move right ${if (mCurrentNum + revisedDistanceX < 0) mCurrentNum else -revisedDistanceX}")
                            mOffsetX += if (mCurrentNum + revisedDistanceX < 0) mCurrentNum else -revisedDistanceX
                            updateViews()
                        }
                    }
                } else {
                    //move right涉及状态转换
                    if (revisedDistanceX < mInitMiddle - mCurrentNum) {
                        Log.d("customRuler", "onScroll: right, move right ${mCurrentNum - mInitMiddle} -> left, move right ${mInitMiddle - mCurrentNum - revisedDistanceX}")
                        mStart -= mCurrentNum - mInitMiddle
                        updateViews()
                        mOffsetX += mInitMiddle - mCurrentNum - revisedDistanceX
                        updateViews()
                    } else {//move left or move right
                        Log.d("customRuler", "onScroll: right, move $revisedDistanceX")
                        mStart += revisedDistanceX
                        updateViews()
                    }
                }
                return true
            }
            return false
        }
    })

    @SuppressLint("HandlerLeak")
    private inner class EventHandler : android.os.Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                TOUCH_EVENT_LONG_PRESS -> initLongPressedState()

            }
        }
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastX = event.x
                mIsLongPressed = false
                mEventHandler.sendEmptyMessageAtTime(TOUCH_EVENT_LONG_PRESS,
                    event.downTime + ViewConfiguration.getLongPressTimeout())
                mGestureDetector.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = mLastX - event.x
                val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
                if (Math.abs(deltaX) > touchSlop) {
                    //Log.d("longPressed", "distance = $deltaX")
                    if (mIsLongPressed) {
                        if (deltaX > 0) {
                            if (mCurrentNum < mLongPressedEndNum) {
                                mCurrentNum++
                                invalidate()
                            }
                        } else {
                            if (mCurrentNum > mLongPressedStartNum) {
                                mCurrentNum--
                                invalidate()
                            }
                        }
                    } else {
                        mEventHandler.removeMessages(TOUCH_EVENT_LONG_PRESS)
                        mGestureDetector.onTouchEvent(event)
                    }
                    mLastX = event.x
                }
            }
            MotionEvent.ACTION_UP -> if (mIsLongPressed) {
                mIsLongPressed = false
                flingToCurrentNum()
            } else {
                mEventHandler.removeMessages(TOUCH_EVENT_LONG_PRESS)
                mGestureDetector.onTouchEvent(event)
            }
            else -> mGestureDetector.onTouchEvent(event)

        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        //Log.d("customRuler", "onDraw start")
        if (mFirstDraw) {
            mEnd = (mStart + (width - paddingLeft - paddingRight) / (DEFAULT_PRECISION_HORIZONTAL_PADDING + DEFAULT_PRECISION_WIDTH)).toInt()
            mInitMiddle = (mEnd + mStart) / 2
            mCurrentNum = mInitMiddle
            mFirstDraw = false
        }
        drawRuler(canvas)
        //Log.d("customRuler", "onDraw end")
    }

    private fun flingToCurrentNum() {
        if (mLastNum != mCurrentNum) {
            val diff = mLastNum - mInitMiddle
            if (diff == 0) {
                if (mCurrentNum < mInitMiddle) {//move right, update offset
                    animateOffset(mCurrentNum - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                } else {//move left, update precision
                    animatePrecision(mCurrentNum - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                }
            } else if (diff > 0){
                if (mCurrentNum >= mInitMiddle) {
                    //move left or move right, update precision
                    animatePrecision(mCurrentNum - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                } else {
                    //move right, update precision -> update offset
                    val offset = mCurrentNum - mInitMiddle
                    animatePrecision(mInitMiddle - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION) {
                        animateOffset(offset, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                    }
                }
            } else {
                if (mCurrentNum <= mInitMiddle) {
                    //move right or move left, update offset
                    animateOffset(mCurrentNum - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                } else {
                    //move left, update offset -> update precision
                    val offset = mCurrentNum - mInitMiddle
                    animateOffset(mInitMiddle - mLastNum, DEFAULT_LONG_PRESSED_ANIMATION_DURATION){
                        animatePrecision(offset, DEFAULT_LONG_PRESSED_ANIMATION_DURATION)
                    }
                }
            }
        } else {
            invalidate()
        }
    }

    private fun initLongPressedState() {
        mLastNum = mCurrentNum
        mLongPressedStartNum = if (mCurrentNum - 5 < 0) 0 else mCurrentNum - 5
        mLongPressedEndNum = mLongPressedStartNum + 10
        mIsLongPressed = true
        invalidate()
    }

    private fun updateViews() {
        mEnd = (mStart + (width - paddingLeft - paddingRight - mOffsetX) / (DEFAULT_PRECISION_HORIZONTAL_PADDING + DEFAULT_PRECISION_WIDTH)).toInt()
        mCurrentNum = if (mOffsetX > 0) mInitMiddle - mOffsetX else (mEnd + mStart) / 2
        invalidate()
    }

    /**
     * 需要考虑左边越界
     */
    private fun animateOffset(
        distance: Int,
        duration: Long,
        nextAnimator: (() -> Boolean)? = null
    ): Boolean {
        val endOffsetX: Int = if (distance > 0) {//move left
            mOffsetX - distance
        } else {//move right
            if (mCurrentNum + distance >= 0) mOffsetX - distance else mInitMiddle
        }

        if (mOffsetX != endOffsetX) {
            ObjectAnimator.ofInt(this@CustomRuler, "offsetX", mOffsetX, endOffsetX)
                .setDuration(duration)
                .apply {
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            mIsFlinging = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            mIsFlinging = false
                            nextAnimator?.apply {
                                invoke()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            mIsFlinging = false
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            mIsFlinging = true
                        }
                    })
                    interpolator = LinearInterpolator()
                }
                .start()
            return true
        }
        return false
    }

    private fun animatePrecision(
        distance: Int,
        duration: Long,
        nextAnimator: (() -> Boolean)? = null
    ): Boolean {
        val endValue: Int = mStart + distance
        if (endValue != mStart) {
            ObjectAnimator.ofInt(this@CustomRuler, "start", mStart, endValue)
                .setDuration(duration)
                .apply {
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                            mIsFlinging = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            mIsFlinging = false
                            nextAnimator?.apply {
                                invoke()
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            mIsFlinging = false
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            mIsFlinging = true
                        }
                    })
                    interpolator = LinearInterpolator()
                }
                .start()
            return true
        }
        return false
    }

    private fun setStart(value: Int) {
        if (value >= 0) {
            Log.d("customRuler", "oldStart = $mStart, new start = $value")
            mStart = value
            updateViews()
        }
    }

    private fun setOffsetX(value: Int) {
        if (value in 0..mInitMiddle) {
            Log.d("customRuler", "oldOffset = $mOffsetX, new offset = $value")
            mOffsetX = value
            updateViews()
        }
    }

    private fun drawRuler(canvas: Canvas) {

        //background
        canvas.drawRect(
            paddingLeft.toFloat(),
            (height / 2).toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat(),
            mRulerBackgroundPaint
        )

        //precision
        Log.d("customRuler", "mCurrentNum = $mCurrentNum")
        Log.d("customRuler", "mStart = $mStart")
        Log.d("customRuler", "mOffset = $mOffsetX")
        for (i in mStart until mEnd) {
            val startX = paddingLeft + (i - mStart + 1 + mOffsetX) * (DEFAULT_PRECISION_HORIZONTAL_PADDING + DEFAULT_PRECISION_WIDTH)
            val startY = (height / 2).toFloat()
            val endY = if (i % 10 == 0) {
                val tempY = (height - paddingTop - paddingBottom) / 2 + ((height - paddingTop - paddingBottom) / 2 - DEFAULT_PRECISION_VERTICAL_PADDING) / 2
                drawPrecisionText((i / 10).toString(), startX, tempY, canvas)
                tempY
            } else {
                (height - paddingTop - paddingBottom) / 2 + ((height - paddingTop - paddingBottom) / 2 - DEFAULT_PRECISION_VERTICAL_PADDING) / 4
            }
            if (mCurrentNum == i) {
                val tempY = (height - paddingTop - paddingBottom) / 2 + (height - paddingTop - paddingBottom) / 4.0f
                canvas.drawLine(startX, startY, startX, tempY , mCurrentTextPrecisionPaint)
                drawCurrentText(canvas, (mCurrentNum / 10.0).toString())
            } else {
                canvas.drawLine(startX, startY, startX, endY, mPrecisionPaint)
            }
        }

    }

    private fun drawPrecisionText(
        text: String,
        startX: Float,
        startY: Float,
        canvas: Canvas
    ) {
        val textWidth = mPrecisionTextPaint.measureText(text)
        val textStartX = startX - textWidth / 2
        val textRect = Rect()
        mPrecisionTextPaint.getTextBounds(text, 0, text.length, textRect)
        val textHeight = textRect.height()
        val textStartY = startY + DEFAULT_PRECISION_VERTICAL_PADDING + textHeight
        canvas.drawText(text, textStartX, textStartY, mPrecisionTextPaint)
    }

    private fun drawCurrentText(canvas: Canvas, content: String) {
        val rect = Rect()
        if (mIsLongPressed) {
            mCurrentTextPaint.getTextBounds(content, 0, content.length, rect)
            val textWidth = rect.width()
            val textHeight = rect.height()
            val textX = ((width - paddingLeft - paddingRight) / 2 - textWidth / 2).toFloat()
            val textY = ((height - paddingTop - paddingBottom) / 8 * 3 + textHeight / 2).toFloat()

            val rectWidth = (width - paddingLeft - paddingRight) / 2
            val interval = (rectWidth - DEFAULT_PRECISION_WIDTH) / 12
            val rectX = (width - paddingLeft - paddingRight) / 4 +
                    (mLongPressedStartNum - mCurrentNum + 5) * (interval + DEFAULT_PRECISION_WIDTH)
            canvas.drawRect(
                rectX,
                paddingTop.toFloat(),
                rectX + rectWidth,
                paddingTop + (height - paddingTop - paddingBottom) / 2.0f,
                mRulerBackgroundPaint
            )
            for (i in mLongPressedStartNum..mLongPressedEndNum){
                val precisionX = rectX + interval * (i - mLongPressedStartNum + 1) + DEFAULT_PRECISION_WIDTH * (i - mLongPressedStartNum)
                var precisionY = (height - paddingTop - paddingBottom) / 4 - DEFAULT_PRECISION_VERTICAL_PADDING / 2
                if (i % 10 != 0) {
                    precisionY /= 2
                }
                if (mCurrentNum == i) {
                    canvas.drawLine(precisionX, paddingTop.toFloat(), precisionX, precisionY, mCurrentTextPrecisionPaint)
                } else {
                    canvas.drawLine(precisionX, paddingTop.toFloat(), precisionX, precisionY, mPrecisionPaint)
                }
                canvas.drawText(content, textX, textY, mCurrentTextPaint)
                canvas.drawText(UNIT_KG, textX + mCurrentTextPaint.measureText(content) + 10, textY - textHeight + 10, mCurrentTextUnitPaint)
            }
        } else {
            mCurrentTextPaint.getTextBounds(content, 0, content.length, rect)
            val textWidth = rect.width()
            val textHeight = rect.height()
            val textX = (width / 2 - textWidth / 2).toFloat()
            val textY = ((height - paddingTop - paddingBottom) / 4 + textHeight / 2).toFloat()
            canvas.drawText(content, textX, textY, mCurrentTextPaint)
            canvas.drawText(UNIT_KG, textX + mCurrentTextPaint.measureText(content) + 10, textY - textHeight + 10, mCurrentTextUnitPaint)
        }
    }

}