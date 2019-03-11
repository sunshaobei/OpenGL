package com.test.opengl

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContext{
    private lateinit var mContext:Context
    fun init(context: Context){
        AppContext.mContext = context
    }
    fun getContext():Context{
        return mContext
    }
}