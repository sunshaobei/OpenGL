package com.test.opengl.scrawl

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.utils.CoordinateTransform
import com.test.opengl.utils.ShaderUtils
import com.test.opengl.vbo.VBOHelper

class ScrawlVBO constructor(var glView: GLView,var scrawlSize:Int) {

    private var mProgram = 0
    private var mVPosition = 0
    private var mFPosition = 0
    private var mMaskPosition = 0
    private var mTexture = 0
    private var mTextureMask = 0
    private var mMatrix = 0


    private lateinit var mBitmap: Bitmap
    private lateinit var mMaskBitmap: Bitmap
    private var mTextureIds = IntArray(3)
    private var mFrame = IntArray(1)
    private var mVboIds = ArrayList<Int>()

    private val mMaskTextureData = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    fun onCreate() {
        mProgram = ShaderUtils.createProgram(glView.context.resources, "shader/scrawl_vertex.vert",
                "shader/scrawl.frag")

        GLES20.glGenFramebuffers(1, mFrame, 0)
        if (mProgram > 0) {
            //获取顶点坐标字段
            mVPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
            //获取纹理坐标字段
            mFPosition = GLES20.glGetAttribLocation(mProgram, "vCoord")
            mMaskPosition = GLES20.glGetAttribLocation(mProgram, "vCoord2")

            mTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
            mMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")

            mTextureMask = GLES20.glGetUniformLocation(mProgram, "vTextureSrc")

            mBitmap = BitmapFactory.decodeResource(glView.context.resources, R.mipmap.attack)
                    ?: return

            mMaskBitmap = createBp(scrawlSize, scrawlSize)
            //创建vbo
            createTexture()
        }

    }

    fun createTexture() {
        //创建纹理
        GLES20.glGenTextures(3, mTextureIds, 0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0])
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置纹理为2d图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1])
        //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

        //设置纹理为2d图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mMaskBitmap, 0)
    }


    private var mProjectMatrix = FloatArray(16)
    private var mViewMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)
    fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val w = mBitmap.width
        val h = mBitmap.height
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


    fun createVBO(x: Float, y: Float) {
        synchronized(mVboIds){
            val createVertex = CoordinateTransform.createVertex(glView, mBitmap, mMaskBitmap, x, y)
            val createTexture = CoordinateTransform.createTexture(glView, mBitmap, mMaskBitmap, x, y)
            mVboIds.add(VBOHelper.createVBO(createVertex, createTexture!!, mMaskTextureData))
        }
    }

    fun useProgram() {
        GLES20.glUseProgram(mProgram)
    }


    fun onDraw() {
        synchronized(mVboIds){
            for (mVboId in mVboIds) {
                onDraw(mVboId)
            }
        }
    }

    private fun onDraw(vboId: Int) {

        GLES20.glEnableVertexAttribArray(mVPosition)
        GLES20.glEnableVertexAttribArray(mFPosition)
        GLES20.glEnableVertexAttribArray(mMaskPosition)

        GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0])
        GLES20.glUniform1i(mTexture, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1])
        GLES20.glUniform1i(mTextureMask, 1)
        VBOHelper.useVboDraw(vboId, mVPosition, mFPosition, mMaskPosition)
        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mVPosition)
        GLES20.glDisableVertexAttribArray(mFPosition)
        GLES20.glDisableVertexAttribArray(mMaskPosition)
    }

    fun onDrawFBO(): Int {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrame[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mTextureIds[2], 0)

        onDraw()

        //解绑
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return mTextureIds[2]
    }


    private fun createBp(w: Int, h: Int): Bitmap {
        val min = Math.min(w, h)
        val icon = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(icon)
        val photoPaint = Paint()
        photoPaint.color = Color.RED
        photoPaint.isDither = true
        photoPaint.isFilterBitmap = true
        photoPaint.isAntiAlias = true
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawCircle((min / 2).toFloat(), (min / 2).toFloat(), (min / 2).toFloat(), photoPaint)
        return icon
    }

}
