package com.test.opengl.gesture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.test.opengl.AppContext
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.filter.BlendFilter
import com.test.opengl.filter.NoFilter
import com.test.opengl.filter.NoReverseFilter
import com.test.opengl.shape.Shape
import com.test.opengl.utils.MatrixUtils
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Scrawl constructor(var glView: GLView) : Shape() {

    private var TAG: String = Scrawl::class.java.simpleName


    var scrawlRender = ScrawlRender(glView)
    var eraserRender = EraserRender(glView)


    var src = NoFilter(glView.context.resources)
    var reverse = NoReverseFilter(glView.context.resources)
    var blend = NoReverseFilter(glView.context.resources)

    var height = 0
    var width = 0
    var isScawl =true



    private val fFrame = IntArray(3)
    private val fTexture = IntArray(4)


    fun isScrawl(isScawl: Boolean){
        this.isScawl = isScawl
    }

    init {
        glView.postDelayed({
            isScawl =false;
        },10000)
    }

    override fun onDrawFrame(gl: GL10) {
        Log.e(TAG, "onDrawFrame")

        //清除缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
        if (isScawl){
            //清除颜色 深层 缓冲
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0])
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[0], 0)
            scrawlRender.setTouch()
            scrawlRender.onDrawFrame(gl)
        } else{
            //清除颜色 深层 缓冲
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[1])
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[1])
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, fTexture[1], 0)
            eraserRender.setTouch()
            eraserRender.onDrawFrame(gl)
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[2])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[2])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fTexture[2], 0)



//
//        var mBuffer = ByteBuffer.allocate(width * height * 4);
//        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA,
//                GLES20.GL_UNSIGNED_BYTE, mBuffer);
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        bitmap.copyPixelsFromBuffer(mBuffer)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
//
        if (fTexture[0]!=0){
            blend.textureId = fTexture[0]
            blend.draw()
        }

        if (fTexture[1]!=0){
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE_MINUS_DST_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
            blend.textureId = fTexture[1]
            blend.draw()
        }

        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)


        //生成纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[3])
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


//        src.textureId = fTexture[3]
//        src.draw()
//
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
//
//        reverse.textureId = fTexture[2]
//        reverse.draw()


    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged")
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
        src.setSize(width, height)
        reverse.setSize(width, height)
        blend.setSize(width,height)
        MatrixUtils.getMatrix(reverse.matrix, MatrixUtils.TYPE_FITXY, width, height,
                width, height)
        MatrixUtils.getMatrix(src.matrix, MatrixUtils.TYPE_CENTERINSIDE, mBitmap!!.width, mBitmap!!.height,
                width, height)
        MatrixUtils.getMatrix(blend.matrix, MatrixUtils.TYPE_FITXY, width, height,
                width, height)

        scrawlRender.onSurfaceChanged(gl,width,height)
        eraserRender.onSurfaceChanged(gl,width,height)

    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.e(TAG, "onSurfaceCreated")
        mBitmap = createBp()

        mBitmap = createBp()

        scrawlRender.onSurfaceCreated(gl,config)
        eraserRender.onSurfaceCreated(gl,config)

        src.create()
        reverse.create()
        blend.create()

        GLES20.glGenFramebuffers(3, fFrame, 0)
        GLES20.glGenTextures(4, fTexture, 0)
    }

    private fun createBp(): Bitmap {
        var icon = BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
        return icon
    }

    private lateinit var mBitmap: Bitmap

}