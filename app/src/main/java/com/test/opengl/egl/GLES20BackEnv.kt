package com.test.opengl.egl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log

import com.test.opengl.filter.AFilter

import java.nio.IntBuffer


/**
 * Description:
 */
class GLES20BackEnv(private val mWidth: Int, private val mHeight: Int) {
    private val mEGLHelper: EGLHelper

    private var mFilter: AFilter? = null
    internal lateinit var mBitmap: Bitmap
    internal lateinit var mThreadOwner: String

    val bitmap: Bitmap?
        get() {
            if (mFilter == null) {
                Log.e(TAG, "getBitmap: Renderer was not set.")
                return null
            }
            if (Thread.currentThread().name != mThreadOwner) {
                Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.")
                return null
            }
            mFilter!!.textureId = createTexture(mBitmap)
            mFilter!!.draw()
            return convertToBitmap()
        }
    val textureId: Int?
        get() {
            if (mFilter == null) {
                Log.e(TAG, "getBitmap: Renderer was not set.")
                return null
            }
            if (Thread.currentThread().name != mThreadOwner) {
                Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.")
                return null
            }
            mFilter!!.textureId = createTexture(mBitmap)
            mFilter!!.draw()
            return mFilter!!.textureId
        }

    init {
        mEGLHelper = EGLHelper()
        mEGLHelper.eglInit(mWidth, mHeight)
    }

    fun setThreadOwner(threadOwner: String) {
        this.mThreadOwner = threadOwner
    }

    fun setFilter(filter: AFilter) {
        mFilter = filter

        // Does this thread own the OpenGL context?
        if (Thread.currentThread().name != mThreadOwner) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.")
            return
        }
        // Call the renderer initialization routines
        mFilter!!.create()
        mFilter!!.setSize(mWidth, mHeight)
    }

    fun destroy() {
        mEGLHelper.destroy()
    }


    private fun convertToBitmap(): Bitmap {
        val iat = IntArray(mWidth * mHeight)
        val ib = IntBuffer.allocate(mWidth * mHeight)
        mEGLHelper.mGL.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                ib)
        val ia = ib.array()

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (i in 0 until mHeight) {
            System.arraycopy(ia, i * mWidth, iat, (mHeight - i - 1) * mWidth, mWidth)
        }
        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat))
        return bitmap
    }

    fun setInput(bitmap: Bitmap) {
        this.mBitmap = bitmap
    }

    private fun createTexture(bmp: Bitmap?): Int {
        val texture = IntArray(1)
        if (bmp != null && !bmp.isRecycled) {
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
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0)
            return texture[0]
        }
        return 0
    }

    companion object {

        internal val TAG = "GLES20BackEnv"
        internal val LIST_CONFIGS = false
    }


}
