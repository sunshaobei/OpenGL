package com.test.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class GLView : GLSurfaceView {
    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val dispatchTouchEvent = this.dispatchTouchListener?.dispatchTouchEvent(event)
        if (dispatchTouchEvent != null) {
            return dispatchTouchEvent
        }
        return super.dispatchTouchEvent(event)
    }


    fun setDispatchTouchListener(dispatchTouchListener: DispatchTouchListener) {
        this.dispatchTouchListener = dispatchTouchListener
    }

    private var dispatchTouchListener: DispatchTouchListener? = null

    interface DispatchTouchListener {
        fun dispatchTouchEvent(event: MotionEvent?): Boolean
    }
}
