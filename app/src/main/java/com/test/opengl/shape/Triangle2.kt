package com.test.opengl.shape

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Triangle2: Shape() {

    private var TAG: String = Triangle2::class.java.simpleName

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor=aColor;" +
            "}"

    private val fragmentShaderCode = (
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}")


    private val COORDS_PER_VERTEX = 3
    private val triangleCoords = floatArrayOf(0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    )
    //顶点之间的偏移量
    private val vertexStride = COORDS_PER_VERTEX * 4 // 每个顶点四个字节


    private var mProjectMatrix: FloatArray = FloatArray(16)
    private var mViewMatrix: FloatArray = FloatArray(16)
    private var mMVPMatrix: FloatArray = FloatArray(16)
    private var mProgram: Int = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var colorBuffer: FloatBuffer
    //设置颜色，依次为红绿蓝和透明通道
    //设置颜色
    private var color = floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f)


    override fun onDrawFrame(gl: GL10?) {
        Log.e(TAG, "onDrawFrame")
        //清除颜色 深层 缓冲
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        //获取变换矩阵vMatrix成员句柄
        val mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        //获取顶点着色器的vPosiiton成员句柄
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        //获取元着色器的vColor成员句柄
        val mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor")
        //绘制颜色
        GLES20.glEnableVertexAttribArray(mColorHandler)
        GLES20.glVertexAttribPointer(mColorHandler, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        //绘制三角形  绘制模式 从哪里开始 顶底总数
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, triangleCoords.size / COORDS_PER_VERTEX)
        // 禁用顶点坐标id
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged")
        GLES20.glViewport(0, 0, width, height)
        //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e(TAG, "onSurfaceCreated")
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)

        val colorBf = ByteBuffer.allocateDirect(color.size * 4)
        colorBf.order(ByteOrder.nativeOrder())
        colorBuffer = colorBf.asFloatBuffer()
        colorBuffer.put(color)
        colorBuffer.position(0)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        //将定点着色器添加到es程序
        GLES20.glAttachShader(mProgram, vertexShader)
        //将片元着色器添加到es程序
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }
}