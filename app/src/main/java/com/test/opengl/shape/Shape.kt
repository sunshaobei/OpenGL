package com.test.opengl.shape

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.test.opengl.utils.GLLog

abstract class Shape: GLSurfaceView.Renderer{

    fun loadShader(shaderType: Int, shaderCode: String): Int {
        //根据type创建顶点着色器或者片元着色器
        var shader = GLES20.glCreateShader(shaderType)
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode)
        //编译着色器程序
        GLES20.glCompileShader(shader)
        // 编译日志
       if (!GLLog.compile(shader, shaderType)){
           //编译失败删除shader
           shader =0
       }
        return shader
    }

}
