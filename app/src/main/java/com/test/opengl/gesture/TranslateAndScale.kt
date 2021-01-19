package com.test.opengl.gesture

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import com.test.opengl.AppContext
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.shape.Shape
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 未知问题，图片显示不了
 */
class TranslateAndScale constructor(var glView: GLView) : Shape(), GLView.DispatchTouchListener {


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
    internal var outputMatrix = FloatArray(16)
    internal var scleMatrix = FloatArray(16)
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


    var top = 0f;
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
                top = -1 / sWidthHeight * sWH
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3f, 7f)
            } else {
                top = -sWH / sWidthHeight
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -sWH / sWidthHeight, sWH / sWidthHeight, 3f, 7f)
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
        Matrix.setIdentityM(scleMatrix, 0);//建立单位矩阵
        Matrix.setIdentityM(mTranslateMatrix, 0);//建立单位矩阵

    }

    override fun onDrawFrame(gl: GL10) {
        var mat = FloatArray(16)
        Matrix.multiplyMM(mat, 0, mMVPMatrix, 0, mTranslateMatrix, 0)
        Matrix.multiplyMM(outputMatrix, 0, mat, 0, scleMatrix, 0)
        draw()
    }

    var textture: IntArray? = null

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        onDrawSet()
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, outputMatrix, 0)
        GLES20.glEnableVertexAttribArray(glHPosition)
        GLES20.glEnableVertexAttribArray(glHCoordinate)
        GLES20.glUniform1i(glHTexture, 0)
        if (textture != null) {
            GLES20.glDeleteTextures(1, textture, 0)
        }
        textture = createTexture()
        textureId = textture!![0]
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos)
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(glHPosition)
        GLES20.glDisableVertexAttribArray(glHCoordinate)
    }

    private fun createTexture(): IntArray {
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
            return texture
        }
        return texture
    }

    fun onDrawSet() {
//        GLES20.glUniform1i(hChangeType, 0)
//        GLES20.glUniform3fv(hChangeColor, 1, floatArrayOf(0.0f, 0.0f, 0.0f), 0)
    }

    fun onDrawCreatedSet(mProgram: Int) {
//        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType")
//        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor")
    }

    private fun createBp(): Bitmap {
        return BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
    }


    private var dis_start = 0.0
    var pointer1 = FloatArray(2)
    var pointer2 = FloatArray(2)
    var pointMap = mutableMapOf<Int, FloatArray>()
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        if (event != null) {

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pointer1[0] = toOpenGLCoord(event.x, true)
                    pointer1[1] = toOpenGLCoord(event.y, false)
                    pointer2[0] = 0f
                    pointer2[1] = 0f
                    pointMap.clear()
                    pointMap[event.getPointerId(0)] = pointer1
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    pointer1[0] = toOpenGLCoord(event.x, true)
                    pointer1[1] = toOpenGLCoord(event.y, false)
                    pointer2[0] = toOpenGLCoord(event.getX(1), true)
                    pointer2[1] = toOpenGLCoord(event.getY(1), false)
                    pointMap[event.getPointerId(0)] = pointer1
                    pointMap[event.getPointerId(1)] = pointer2
                    dis_start = computeDis(pointer1[0], pointer2[0], pointer1[1], pointer2[1])
                }

                MotionEvent.ACTION_POINTER_UP -> {

                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 2) {
                        pointer1[0] = toOpenGLCoord(event.x, true)
                        pointer1[1] = toOpenGLCoord(event.y, false)
                        pointer2[0] = toOpenGLCoord(event.getX(1), true)
                        pointer2[1] = toOpenGLCoord(event.getY(1), false)
                        pointMap[event.getPointerId(0)] = pointer1
                        pointMap[event.getPointerId(1)] = pointer2
                        var dis_start2 = computeDis(pointer1[0], pointer2[0], pointer1[1], pointer2[1])
                        val d = dis_start2 / dis_start
                        var scale = d.toFloat()
                        Matrix.scaleM(scleMatrix, 0, scale, scale, 0.0f)
                        glView.requestRender()
                        dis_start = dis_start2
                        Log.e("scale", "$scale")
                    } else if (event.pointerCount == 1) {
                        val pointer = pointMap[event.getPointerId(0)]
                        var transX = toOpenGLCoord(event.x, true) - pointer!![0]
                        var transY = toOpenGLCoord(event.y, false) - pointer[1]
                        pointer[0] = toOpenGLCoord(event.x, true)
                        pointer[1] = toOpenGLCoord(event.y, false)
                        Log.e("transX", "$transX")
                        Log.e("transY", "$transY")
                        Matrix.translateM(mTranslateMatrix, 0, transX, transY, 0f)
                        glView.requestRender()
                    }
                    x = mTranslateMatrix[mTranslateMatrix.size - 4]
                    y = mTranslateMatrix[mTranslateMatrix.size - 3]
                    Log.e("x", "$x")
                    Log.e("y", "$y")
                    Log.e("scaleM", "${scleMatrix[0]}")

                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val scale = scleMatrix[0]
                    var disX = 0f
                    var disY = 0f
                    if (Math.abs(mTranslateMatrix[mTranslateMatrix.size - 4]) > getMinDisX(scale)) {
                        disX = Math.abs(mTranslateMatrix[mTranslateMatrix.size - 4]) - getMinDisX(scale)
                    }


                    if (Math.abs(mTranslateMatrix[mTranslateMatrix.size - 3]) > getMinDisY(scale)) {
                        disY = Math.abs(mTranslateMatrix[mTranslateMatrix.size - 3]) - getMinDisY(scale)
                    }

                    if (disY > 0 || disX > 0 || scale < 1) {
                        val ofFloat = ValueAnimator.ofFloat(1f, 0f)
                        ofFloat.duration = 200
                        ofFloat.interpolator = DecelerateInterpolator()
                        ofFloat.addUpdateListener {
                            val animatedValue = it.animatedValue as Float
                            var tx = mTranslateMatrix[mTranslateMatrix.size - 4]
                            var ty = mTranslateMatrix[mTranslateMatrix.size - 3]
                            if (disX > 0) {
                                if (tx > 0) {
                                    tx = getMinDisX(scale) + disX * animatedValue
                                } else if (tx < 0) {
                                    tx = -getMinDisX(scale) - disX * animatedValue
                                }
                            }
                            if (disY > 0) {
                                if (ty > 0) {
                                    ty = getMinDisY(scale) + disY * animatedValue
                                } else if (ty < 0) {
                                    ty = -(getMinDisY(scale)) - disY * animatedValue
                                }
                            }
                            if (scale < 1) {
                                val fl = 1 - ((1 - scale) * animatedValue)
                                scleMatrix[0] = fl
                                scleMatrix[5] = fl
                            }

                            mTranslateMatrix[mTranslateMatrix.size - 4] = tx
                            mTranslateMatrix[mTranslateMatrix.size - 3] = ty
                            glView.requestRender()
                        }
                        ofFloat.start()
                    }
                }

            }
            return true
        }

        return false
    }

    private fun getMinDisX(s: Float): Float {
        return if (s < 1) {
            0f
        } else {
            s - 1
        }
    }

    private fun getMinDisY(s: Float): Float {
        return if (s <= 1) {
            0f
        } else {
            val fl = s - 1
            val fl1 = (1 - 1 / Math.abs(top)) / (1 / Math.abs(top))
            if ((fl > fl1)) {
                fl - fl1
            } else {
                0f
            }
        }
    }


    var x = 0f
    var y = 0f

    /**
     * 计算两个点之间的距离
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    private fun computeDis(x1: Float, x2: Float, y1: Float, y2: Float): Double {
        return Math.sqrt(Math.pow(((x2 - x1).toDouble()), 2.0) + Math.pow(((y2 - y1).toDouble()), 2.0))
    }


    private fun toOpenGLCoord(value: Float, isWidth: Boolean): Float {
        return if (isWidth) {
            (value / glView.width) * 2 - 1
        } else {
            -((value / glView.height) * 2 - 1)
        }
    }


}
