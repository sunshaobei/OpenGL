package com.test.opengl.shape

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Oval : Shape() {

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor=aColor;" +
            "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"


    private var mProgram: Int = 0
    private lateinit var vertexBuffer:FloatBuffer

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)

    //顶点之间的偏移量
    private val vertexStride = 0 // 每个顶点四个字节

    private var mMatrixHandler: Int = 0

    private lateinit var vertexs:FloatArray
    //设置颜色，依次为红绿蓝和透明通道
    private var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)

        //获取元着色器的vColor成员句柄
        val mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor")
        //绘制颜色
        GLES20.glEnableVertexAttribArray(mColorHandler)
        GLES20.glVertexAttribPointer(mColorHandler, 4, GLES20.GL_FLOAT, false, 0, colorBuf)

        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexs.size / 3)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
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

    private lateinit var createColor:FloatArray
    private lateinit var colorBuf:FloatBuffer

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        vertexs = createVertex()
        val allocateDirect = ByteBuffer.allocateDirect(vertexs.size * 4)
        allocateDirect.order(ByteOrder.nativeOrder())
        vertexBuffer = allocateDirect.asFloatBuffer()
        vertexBuffer.put(vertexs)
        vertexBuffer.position(0)

        createColor = createColor()
        val colord = ByteBuffer.allocateDirect(createColor.size * 4)
        colord.order(ByteOrder.nativeOrder())
        colorBuf = colord.asFloatBuffer()
        colorBuf.put(createColor)
        colorBuf.position(0)

        mProgram = GLES20.glCreateProgram()
        //获取顶点着色器id；
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }




    private fun createVertex(): FloatArray {
        val arrayList = ArrayList<Float>()
        val colorList = ArrayList<Float>()
        //添加原点，
        arrayList.add(0.0f)
        arrayList.add(0.0f)
        arrayList.add(0.0f)
        colorList.add(0.0f)
        colorList.add(1.0f)
        colorList.add(0.0f)
        colorList.add(1.0f)
        var i = 0f
        while (i <=360) {
            arrayList.add((1 * Math.sin(i * Math.PI / 180f)).toFloat())
            arrayList.add((1 * Math.cos(i * Math.PI / 180f)).toFloat())
            arrayList.add(0.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)
            i += 0.1f
        }
        val f = FloatArray(arrayList.size)
        for (t in f.indices) {
            f[t] = arrayList[t]
        }
        return f
    }



    private fun createColor(): FloatArray {
        val colorList = ArrayList<Float>()
        var i = 0f
        while (i <=360) {
            //添加原点，
            colorList.add(0.0f)
            colorList.add(1.0f)
            colorList.add(0.0f)
            colorList.add(1.0f)

            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)

            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)
            colorList.add(1.0f)

            i += 0.1f
        }
        val f = FloatArray(colorList.size)
        for (t in f.indices) {
            f[t] = colorList[t]
        }
        return f
    }
}