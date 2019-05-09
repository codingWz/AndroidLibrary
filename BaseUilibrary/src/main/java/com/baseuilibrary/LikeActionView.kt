package com.baseuilibrary

import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class LikeActionView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_ROTATION_X = 3.0F
        private const val MAX_TRANSLATION_Y = 65.0F

        private const val ACTION_ADD = 0
        private const val ACTION_MINUS = 1

        private const val DEFAULT_TEXT_SIZE = 42.0f
        private const val DEFAULT_ANIMATION_DURATION = 350L
        private const val DEFAULT_LETTER_SPACING = 9.0f
    }

    private val mDefaultPaint = Paint().apply {
        textSize = DEFAULT_TEXT_SIZE
    }
    private val mAnimateOldPaint = Paint().apply {
        textSize = DEFAULT_TEXT_SIZE
    }
    private val mAnimateNewPaint = Paint().apply {
        textSize = DEFAULT_TEXT_SIZE
    }
    private val mTextHeight: Float
    private val mOldStatus: LikeActionStatus = LikeActionStatus()
    private val mNewStatus: LikeActionStatus = LikeActionStatus()

    private var mLikeNum: Int
    private var mOldStr: String
    private var mNewStr: String = ""
    private var mAnimatedOldStr: String = ""
    private var mCommonOldStr: String
    private var mAnimatedNewStr: String = ""
    private var mCurrentAction = ACTION_ADD
    private var mViewWidth: Float = 0.0f
    private var mViewHeight: Float = 0.0f
    private var mOldStrWidth: Float = 0.0f
    private var mNewStrWidth: Float = 0.0f
    private var mCommonOldStrWidth: Float = 0.0f
    private var mLetterSpacing: Float = DEFAULT_LETTER_SPACING
    /**
     * 记录数字在加减过程中位数是否发生变化，这对于绘制文字的位置有影响
     */
    private var mLengthChanged: Boolean = false

    var animationFinishedListener: (() -> Unit)? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.LikeActionView, defStyleAttr, 0)
        mLikeNum = typedArray.getInt(R.styleable.LikeActionView_likeNum, 0)
        if (mLikeNum < 0) {
            mLikeNum = 0
        }
        typedArray.recycle()
        mOldStr = mLikeNum.toString()
        mCommonOldStr = mOldStr
        mOldStrWidth = mDefaultPaint.measureText(mOldStr)
        val rect = Rect()
        mDefaultPaint.getTextBounds(mOldStr, 0, mOldStr.length, rect)
        mTextHeight = (rect.bottom - rect.top).toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w.toFloat()
        mViewHeight = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = mViewWidth / 2 - mOldStrWidth / 2
        val y = mViewHeight / 2 + mTextHeight / 2

        if (mCommonOldStr.isNotEmpty()) {
            canvas.drawText(mCommonOldStr, x, y, mDefaultPaint)
        }
        var xOffset = x
        if (mAnimatedOldStr.isNotEmpty()) {
            mAnimateOldPaint.alpha = mOldStatus.mAlpha
            xOffset = if (mCommonOldStr.isEmpty()) x else  x + mCommonOldStrWidth + mLetterSpacing
            canvas.drawText(mAnimatedOldStr, xOffset, y + mOldStatus.mTranslationY, mAnimateOldPaint)
        }
        if (mAnimatedNewStr.isNotEmpty()) {
            mAnimateNewPaint.alpha = mNewStatus.mAlpha
            if (mLengthChanged) {
                canvas.drawText(mAnimatedNewStr, mViewWidth / 2 - mNewStrWidth / 2, y + mNewStatus.mTranslationY, mAnimateNewPaint)
            } else {
                canvas.drawText(mAnimatedNewStr, xOffset, y + mNewStatus.mTranslationY, mAnimateNewPaint)
            }
        }
    }

    private fun performAnimation() {
        val startStatus = LikeActionStatus(0.0f, 255, 0.0f)
        val endStatus = LikeActionStatus(-MAX_TRANSLATION_Y, 0, MAX_ROTATION_X)
        ValueAnimator.ofObject(TypeEvaluator<LikeActionStatus> { fraction, startValue, endValue ->
            if (mCurrentAction == ACTION_ADD) {
                mOldStatus.apply {
                    mAlpha = (startValue.mAlpha + fraction * (endValue.mAlpha - startValue.mAlpha)).toInt()
                    mRotationX = startValue.mRotationX + fraction * (endValue.mRotationX - startValue.mRotationX)
                    mTranslationY = startValue.mTranslationY + fraction * (endValue.mTranslationY - startValue.mTranslationY)
                }
                mNewStatus.apply {
                    mAlpha = (endValue.mAlpha - fraction * (endValue.mAlpha - startValue.mAlpha)).toInt()
                    mRotationX = endValue.mRotationX - fraction * (endValue.mRotationX - startValue.mRotationX)
                    mTranslationY = -endValue.mTranslationY + fraction * (endValue.mTranslationY - startValue.mTranslationY)
                }
            } else {
                mOldStatus.apply {
                    mAlpha = (startValue.mAlpha + fraction * (endValue.mAlpha - startValue.mAlpha)).toInt()
                    mRotationX = startValue.mRotationX + fraction * (endValue.mRotationX - startValue.mRotationX)
                    mTranslationY = startValue.mTranslationY - fraction * (endValue.mTranslationY - startValue.mTranslationY)
                }
                mNewStatus.apply {
                    mAlpha = (endValue.mAlpha - fraction * (endValue.mAlpha - startValue.mAlpha)).toInt()
                    mRotationX = endValue.mRotationX - fraction * (endValue.mRotationX - startValue.mRotationX)
                    mTranslationY = endValue.mTranslationY - fraction * (endValue.mTranslationY - startValue.mTranslationY)
                }
            }
            invalidate()
            mOldStatus
        }, startStatus, endStatus)
            .apply {
                duration = DEFAULT_ANIMATION_DURATION
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        animationFinishedListener?.apply {
                            invoke()
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationStart(animation: Animator) {
                    }
                })
            }
            .start()
    }

    private fun compareStr(oldStr: String, newStr: String): Int {
        var index = 0
        if (oldStr.length != newStr.length) {
            index = -1
        } else {
            for ((i, c) in oldStr.toCharArray().withIndex()) {
                if (c != newStr[i]) {
                    index = i
                    break
                }
            }
        }
        return index
    }

    /**
     * 动态计算字符间距
     */
    private fun getLetterSpacing(commonStr: String, animateStr: String, originStr: String): Float {
        val rect = Rect()
        mDefaultPaint.getTextBounds(commonStr, 0, commonStr.length, rect)
        val commonWidth = rect.right - rect.left
        mDefaultPaint.getTextBounds(animateStr, 0, animateStr.length, rect)
        val animateWidth = rect.right - rect.left
        mDefaultPaint.getTextBounds(originStr, 0, originStr.length, rect)
        val originWidth = rect.right - rect.left
        return (originWidth - commonWidth - animateWidth).toFloat()
    }

    private fun prepareAnimation() {
        val index = compareStr(mOldStr, mNewStr)
        if (index == -1) {
            mLengthChanged = true
            mAnimatedOldStr = mOldStr
            mAnimatedNewStr = mNewStr
            mCommonOldStr = ""
            mNewStrWidth = mDefaultPaint.measureText(mNewStr)
        } else {
            mLengthChanged = false
            mAnimatedOldStr = mOldStr.substring(index until mOldStr.length)
            mAnimatedNewStr = mNewStr.substring(index until mNewStr.length)
            mCommonOldStr = mOldStr.substring(0 until index)
            val rect = Rect()
            mDefaultPaint.getTextBounds(mCommonOldStr, 0, mCommonOldStr.length, rect)
            mCommonOldStrWidth = (rect.right - rect.left).toFloat()
            mLetterSpacing = getLetterSpacing(mCommonOldStr, mAnimatedOldStr, mOldStr)
        }
        mOldStrWidth = mDefaultPaint.measureText(mOldStr)
    }

    fun getLikeNum() = mLikeNum

    fun addNum(): String {
        mCurrentAction = ACTION_ADD
        if (mLikeNum < 0) {
            mLikeNum = 0
        }
        mOldStr = mLikeNum.toString()
        mNewStr = (++mLikeNum).toString()
        prepareAnimation()
        performAnimation()
        return mNewStr
    }

    fun minusNum(): String {
        mCurrentAction = ACTION_MINUS
        if (mLikeNum == 0) {
            return "0"
        } else {
            mOldStr = mLikeNum.toString()
            mNewStr = (--mLikeNum).toString()
            prepareAnimation()
            performAnimation()
        }
        return mNewStr
    }

    private class LikeActionStatus(
        var mTranslationY: Float = 0.0f,
        // 0-255
        var mAlpha: Int = 0,
        //未使用该属性，暂时不知道canvas在绘制text的时候如何执行rotationX
        var mRotationX: Float = 0.0f
    )
}