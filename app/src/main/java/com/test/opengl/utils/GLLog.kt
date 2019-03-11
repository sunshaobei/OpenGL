package com.test.opengl.utils

import android.opengl.GLES20
import android.util.Log

object GLLog{
    private val TAG = GLLog::class.java.simpleName
    fun compile(shader:Int,shaderType:Int):Boolean{
        //存放编译成功shader数量的数组
        var compiled = IntArray(1)
        //获取Shader的编译情况
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            //若编译失败则显示错误日志并删除此shader
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return false;
        }
        Log.e(TAG, "编译成功$shader")
        return true
    }
}
