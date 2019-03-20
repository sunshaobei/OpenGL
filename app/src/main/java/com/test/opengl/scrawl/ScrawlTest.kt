package com.test.opengl.scrawl

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.test.opengl.AppContext
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.shape.Shape
import com.test.opengl.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ScrawlTest constructor(var glView: GLView) : Shape() {


    private var mProgram = 0

    private var mHPosition = 0
    private var mHCoord = 0
    private var mHCoord2 = 0
    private var mHMatrix = 0
    private var mHTexture = 0
    private var mTextureSrc = 0
    lateinit var mBitmap: Bitmap
    lateinit var circle: Bitmap

    internal var mProjectMatrix = FloatArray(16)
    internal var mViewMatrix = FloatArray(16)
    internal var mMVPMatrix = FloatArray(16)
    var texture = IntArray(2)


    private val mask = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)

    val originalMatrix: FloatArray
        get() = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
        GLES20.glUseProgram(mProgram)
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glEnableVertexAttribArray(mHCoord)
        GLES20.glEnableVertexAttribArray(mHCoord2)

        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, mMVPMatrix, 0)

        val sPos = VBOHelper.createVertex(glView, mBitmap, circle, glView.width.toFloat() / 2, glView.height.toFloat() / 2)
        val sCoord = VBOHelper.createTexture(glView, mBitmap, circle, glView.width.toFloat() / 2, glView.height.toFloat() / 2)

        val bb = ByteBuffer.allocateDirect(sPos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        var bPos = bb.asFloatBuffer()
        bPos.put(sPos)
        bPos.position(0)
        val cc = ByteBuffer.allocateDirect(sCoord!!.size * 4)
        cc.order(ByteOrder.nativeOrder())
        var bCoord = cc.asFloatBuffer()
        bCoord.put(sCoord)
        bCoord.position(0)



        val aa = ByteBuffer.allocateDirect(mask.size * 4)
        aa.order(ByteOrder.nativeOrder())
        var aaaa = cc.asFloatBuffer()
        aaaa.put(mask)
        aaaa.position(0)


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
        GLES20.glUniform1i(mHTexture, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[1])
        GLES20.glUniform1i(mTextureSrc, 1)


        //传入顶点坐标
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos)
        //传入纹理坐标
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, bCoord)
        GLES20.glVertexAttribPointer(mHCoord2, 2, GLES20.GL_FLOAT, false, 0, aaaa)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHCoord)


    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
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


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        mProgram = ShaderUtils.createProgram(glView.context.resources, "shader/base_vertex.sh",
                "shader/scrawl.frag")
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord")
        mHCoord2 = GLES20.glGetAttribLocation(mProgram, "vCoord2")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        mTextureSrc = GLES20.glGetUniformLocation(mProgram, "vTextureSrc")
        mBitmap = createBp()
        circle = createBp(100, 100)

        //生成纹理
        GLES20.glGenTextures(2, texture, 0)
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

        //生成纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[1])
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, circle, 0)
    }


    private fun createBp(): Bitmap {
        var icon = BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
        return icon
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
