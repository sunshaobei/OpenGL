package com.test.opengl.gesture

import android.opengl.GLES20
import android.util.Log

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.opengles.GL10

class GLLine {
    /**
     * 顶点字节数组
     */
    private var pointByteBuffer: ByteBuffer? = null
    /**
     * 顶点RGBA字节数组
     */
    private var colorByteBuffer: ByteBuffer? = null
    /**
     * 顶点坐标数组
     */
    private var pointBuffer: FloatBuffer? = null
    /**
     * 顶点RGBA数组
     */
    private var colorBuffer: FloatBuffer? = null
    /**
     * 正在写入第几个顶点float
     */
    private var pointBufferPos = 0
    /**
     * 正在写入第几个颜色float
     */
    private var colorBufferPos = 0
    /**
     * 初始化时的顶点数目
     */
    private var initVertexCount = 1 * 1024

    val vertexCount: Int
        get() = pointBufferPos / 3

    fun drawLine(x: Float, y: Float) {
        //按初始化大小初始化顶点字节数组和顶点数组
        if (pointBuffer == null) {
            pointByteBuffer = ByteBuffer.allocateDirect(initVertexCount * 4)    //顶点数 * sizeof(float)
            pointByteBuffer!!.order(ByteOrder.nativeOrder())
            pointBuffer = pointByteBuffer!!.asFloatBuffer()
            pointBuffer!!.position(0)
            pointBufferPos = 0
        }
        //按初始化大小初始化RGBA字节数组和RGBA数组
        if (colorBuffer == null) {
            colorByteBuffer = ByteBuffer.allocateDirect(initVertexCount * 4)
            colorByteBuffer!!.order(ByteOrder.nativeOrder())
            colorBuffer = colorByteBuffer!!.asFloatBuffer()
            colorBuffer!!.position(0)
            colorBufferPos = 0
        }
        //写入坐标值x,y,z
        pointBuffer!!.put(pointBufferPos++, x)
        pointBuffer!!.put(pointBufferPos++, y)
        pointBuffer!!.put(pointBufferPos++, 0f)
        //写入颜色值r,g,b,a
        colorBuffer!!.put(colorBufferPos++, 1f)
        colorBuffer!!.put(colorBufferPos++, Math.random().toFloat())
        colorBuffer!!.put(colorBufferPos++, 1f)
        colorBuffer!!.put(colorBufferPos++, 1f)
        //如果写入的颜色数超过初始值，将顶点数和颜色数组容量翻倍
        if (colorBufferPos * 4 >= initVertexCount) {
            Log.i("GLLines", "扩容点数到:$initVertexCount")
            initVertexCount *= 2

            val qbb = ByteBuffer.allocateDirect(initVertexCount * 4)    //顶点数 * sizeof(float) ;
            qbb.order(ByteOrder.nativeOrder())
            System.arraycopy(pointByteBuffer!!.array(), 0, qbb.array(), 0, pointBufferPos * 4)   //顶点数 * sizeof(float)
            pointByteBuffer = qbb
            pointBuffer = pointByteBuffer!!.asFloatBuffer()

            val qbb2 = ByteBuffer.allocateDirect(initVertexCount * 4)    //顶点数 * sizeof(float) ;
            qbb2.order(ByteOrder.nativeOrder())
            System.arraycopy(colorByteBuffer!!.array(), 0, qbb2.array(), 0, colorBufferPos * 4)  //sizeof(R,G,B,Alpha) * sizeof(float)
            colorByteBuffer = qbb2
            colorBuffer = colorByteBuffer!!.asFloatBuffer()

        }
    }

    fun drawTo(gl: GL10) {
        if (pointBuffer != null && colorBuffer != null) {
            pointBuffer!!.position(0)
            colorBuffer!!.position(0)
            gl.glVertexPointer(3, GLES20.GL_FLOAT, 0, pointBuffer)
            gl.glColorPointer(4, GLES20.GL_FLOAT, 0, colorBuffer)
            gl.glLineWidth(3f)
            gl.glDrawArrays(GLES20.GL_LINE_STRIP, 0, pointBufferPos / 3) //添加的point浮点数/3才是坐标数（因为一个坐标由x,y,z3个float构成，不能直接用）, 第三个参数count如果超过实际点数就会不断有指向0的点在最后
            //            gl.glDrawElements(GL10.GL_LINE_STRIP,0, pointBufferPos / 3, null);  //第一个参数是点的类型，第二个参数是点的个数，第三个是第四个参数的类型，第四个参数是点的存储绘制顺序。
        }
    }
}