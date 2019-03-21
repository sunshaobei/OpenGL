package com.test.opengl.scrawl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.MotionEvent
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.filter.NoFilter
import com.test.opengl.filter.NoReverseFilter
import com.test.opengl.shape.Shape
import com.test.opengl.utils.MatrixUtils
import java.util.logging.Handler
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ScrawlRender constructor(var glView: GLView) : Shape(), GLView.DispatchTouchListener {

    var mScrawVBO = ScrawlVBO(glView, 50)

    var src = NoFilter(glView.context.resources)
    var reverse = NoReverseFilter(glView.context.resources)

    var mWidth = 0
    var mHeight = 0
    lateinit var mBitmap: Bitmap
    var mLastX = 0f
    var mLastY = 0f


    init {
        glView.setDispatchTouchListener(this)
    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var x = event!!.x
        var y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glView.queueEvent {
                    mScrawVBO.createVBO(x, y)
                }
                mLastX = x
                mLastY = y
                glView.requestRender()
            }
            MotionEvent.ACTION_MOVE -> {
                drawPoint(x, y)
                glView.requestRender()
                mLastY = y
                mLastX = x
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }


    fun drawPoint(x: Float, y: Float) {
        val absX = Math.abs(x - mLastX)
        val absY = Math.abs(y - mLastY)
        var count = if (absX > absY) {
            absX.toInt()
        } else {
            absY.toInt()
        }
        for (i in 0..count) {
            glView.queueEvent {
                mScrawVBO.createVBO(mLastX + i * (x - mLastX) / count, mLastY + i * (y - mLastY) / count)
            }
        }
    }


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mScrawVBO.onCreate()
        src.create()
        mBitmap = BitmapFactory.decodeResource(glView.context.resources, R.mipmap.attack)
                ?: return
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        GLES20.glViewport(0, 0, width, height)
        mScrawVBO.onSurfaceChanged(width, height)
        src.setSize(width, height)
        reverse.setSize(width, height)
        MatrixUtils.getMatrix(src.matrix, MatrixUtils.TYPE_CENTERINSIDE, mBitmap.width, mBitmap.height,
                width, height)
        MatrixUtils.getMatrix(reverse.matrix, MatrixUtils.TYPE_CENTERINSIDE, mBitmap.width, mBitmap.height,
                width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

//        src.textureId= createTexture()
//        src.draw()

        mScrawVBO.useProgram()

//        mScrawVBO.createTexture()


        mScrawVBO.onDraw()



//        var mBuffer = ByteBuffer.allocate(mWidth * mHeight * 4);
//        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA,
//                GLES20.GL_UNSIGNED_BYTE, mBuffer);
//        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
//        bitmap.copyPixelsFromBuffer(mBuffer)
    }


    private fun createTexture(): Int {
        val texture = IntArray(1)
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


}
