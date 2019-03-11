package com.test.opengl.gesture

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glEnable
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.test.opengl.AppContext
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.shape.Shape

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Translate constructor(var glView: GLView) : Shape(), GLView.DispatchTouchListener {


    private val vertexShadeCode = "attribute vec4 vPosition;" +
            "attribute vec2 vCoordinate;" +
            "uniform mat4 vMatrix;" +
            "varying vec2 aCoordinate;" +
            "void main(){" +
            "    gl_Position=vMatrix*vPosition;" +
            "    aCoordinate=vCoordinate;" +
            "}"

    private val fragmentShadeCode = "precision mediump float;" +
            "uniform sampler2D vTexture;" +
            "varying vec2 aCoordinate;" +
            "void main(){" +
            "    gl_FragColor=texture2D(vTexture,aCoordinate);}"


    private val sPos = floatArrayOf(-1.0f, 1.0f, //左上角
            -1.0f, -1.0f, //左下角
            1.0f, 1.0f, //右上角
            1.0f, -1.0f     //右下角
    )

    private val sCoord = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)


    private var mBitmap: Bitmap? = null
    internal var mProgram: Int = 0
    internal var glHPosition: Int = 0
    internal var glHCoordinate: Int = 0
    internal var glHTexture: Int = 0
    internal var hIsHalf: Int = 0
    internal var glHUxy: Int = 0
    private lateinit var bPos: FloatBuffer
    private lateinit var bCoord: FloatBuffer

    internal var glHMatrix: Int = 0
    internal var mProjectMatrix = FloatArray(16)
    internal var mViewMatrix = FloatArray(16)
    internal var mMVPMatrix = FloatArray(16)
    internal var mTranslateMatrix = FloatArray(16)

    internal var textureId: Int = 0


    internal var hChangeType: Int = 0
    internal var hChangeColor: Int = 0


    init {
        glView.setDispatchTouchListener(this)
    }


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        val bb = ByteBuffer.allocateDirect(sPos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        bPos = bb.asFloatBuffer()
        bPos.put(sPos)
        bPos.position(0)
        val cc = ByteBuffer.allocateDirect(sCoord.size * 4)
        cc.order(ByteOrder.nativeOrder())
        bCoord = cc.asFloatBuffer()
        bCoord.put(sCoord)
        bCoord.position(0)
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        //        mProgram=ShaderUtils.createProgram(mContext.getResources(),vertex,fragment);
        mProgram = GLES20.glCreateProgram()
        val i = loadShader(GLES20.GL_VERTEX_SHADER, vertexShadeCode)
        val i1 = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShadeCode)
        GLES20.glAttachShader(mProgram, i)
        GLES20.glAttachShader(mProgram, i1)
        GLES20.glLinkProgram(mProgram)

        GLES20.glEnable(GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        onDrawCreatedSet(mProgram)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {

        GLES20.glViewport(0, 0, width, height)
        if (mBitmap == null) mBitmap = createBp()
        val w = mBitmap!!.width
        val h = mBitmap!!.height
        val sWH = w / h.toFloat()
        val sWidthHeight = width / height.toFloat()
        if (width > height) {
            if (sWH > sWidthHeight) {
                //正交投影
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1f, 1f, 3f, 7f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1f, 1f, 3f, 7f)
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3f, 7f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -sWH / sWidthHeight, sWH / sWidthHeight, 3f, 7f)
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10) {

        draw()

    }

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        onDrawSet()
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0)
        GLES20.glEnableVertexAttribArray(glHPosition)
        GLES20.glEnableVertexAttribArray(glHCoordinate)
        GLES20.glUniform1i(glHTexture, 0)
        textureId = createTexture()
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos)
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(glHPosition)
        GLES20.glDisableVertexAttribArray(glHCoordinate)
    }

    private fun createTexture(): Int {
        val texture = IntArray(1)
        if (mBitmap != null && !mBitmap!!.isRecycled) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0)
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
            return texture[0]
        }
        return 0
    }

    fun onDrawSet() {
        GLES20.glUniform1i(hChangeType, 0)
        GLES20.glUniform3fv(hChangeColor, 1, floatArrayOf(0.0f, 0.0f, 0.0f), 0)
    }

    fun onDrawCreatedSet(mProgram: Int) {
        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType")
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor")
    }

    private fun createBp(): Bitmap {
        var icon = BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
        return icon
    }



    var x0 = 0f
    var y0 = 0f
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {
            val x1 = toOpenGLCoord(event.x, true)
            val y1 = toOpenGLCoord(event.y, false)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x0 = x1
                    y0 = y1
                }

                MotionEvent.ACTION_MOVE -> {

                    var transX = x1 - x0;
                    var transY = y1 - y0;
                    x0 = x1
                    y0 = y1
                    Log.e("transX", "$transX")
                    Log.e("transY", "$transY")
                    Matrix.translateM(mMVPMatrix, 0, transX, transY, 0f)
                    glView.requestRender()
                }
                MotionEvent.ACTION_UP -> {

                }

            }
        }

        return true
    }


    fun toOpenGLCoord(value: Float, isWidth: Boolean): Float {
        if (isWidth) {
            return (value / glView.width) * 2 - 1
        } else {
            return -((value / glView.height) * 2 - 1)
        }
    }


}
