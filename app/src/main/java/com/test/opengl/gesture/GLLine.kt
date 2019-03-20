package com.test.opengl.gesture

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLLine constructor(var render: Any, var pointSize: Float, var width: Int) {
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


    var lastX = 0f
    var lastY = 0f

    fun down(x: Float, y: Float) {
        this.lastX = x
        this.lastY = y
        drawPoint(lastX, lastY)
    }

    fun drawPoint(x: Float, y: Float) {
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
        write(x, y)
        val computeDis = computeDis(x, lastX, y, lastY)
        val fl = pointSize / (2 * width).toFloat()
        if (computeDis > fl) {
            val d = (computeDis / fl).toInt() + 1
            var i = 1
            while (i <= d) {
                write(lastX + i * (x - lastX) / d, lastY + i * (y - lastY) / d)
                i++
            }
        }
        lastX = x
        lastY = y
    }

    private fun write(x: Float, y: Float) {
        //写入坐标值x,y,z
        pointBuffer!!.put(pointBufferPos++, x)
        pointBuffer!!.put(pointBufferPos++, y)
        pointBuffer!!.put(pointBufferPos++, 0f)
        //写入颜色值r,g,b,a
        colorBuffer!!.put(colorBufferPos++, 0f)
        colorBuffer!!.put(colorBufferPos++, 1f)
        colorBuffer!!.put(colorBufferPos++, 0f)
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

//
//    fun drawTo(gl: GL10?, mProgram: Int) {
//        if (pointBuffer != null && colorBuffer != null) {
//            pointBuffer!!.position(0)
//            colorBuffer!!.position(0)
//            //获取顶点着色器的vPosiiton成员句柄
//            //准备坐标数据
//            if (render is ScrawlRender)
//                GLES20.glVertexAttribPointer((render as ScrawlRender).mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 12, pointBuffer)
//            else
//                GLES20.glVertexAttribPointer((render as EraserRender).mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 12, pointBuffer)
//            //绘制颜色
//            if (render is ScrawlRender)
//                GLES20.glVertexAttribPointer((render as ScrawlRender).mColorHandler, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
//            else
//                GLES20.glVertexAttribPointer((render as EraserRender).mColorHandler, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
//            //绘制三角形  绘制模式 从哪里开始 顶底总数
//            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pointBufferPos / 3)
//        }
//    }

    /**
     * 计算两个点之间的距离
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    fun computeDis(x1: Float, x2: Float, y1: Float, y2: Float): Double {
        return Math.sqrt(Math.pow(((x2 - x1).toDouble()), 2.0) + Math.pow(((y2 - y1).toDouble()), 2.0))
    }

}