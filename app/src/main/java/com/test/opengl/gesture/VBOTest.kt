package com.test.opengl.gesture

import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.vbo.VBOHelper
import com.test.opengl.shape.Shape
import com.test.opengl.utils.CoordinateTransform
import com.test.opengl.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


//纹理  根据坐标系映射
class VBOTest constructor(var glView: GLView) : Shape() {

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        onSurfaceCreated()
    }

    override fun onDrawFrame(gl: GL10?) {
        draw()
    }


    internal var mProjectMatrix = FloatArray(16)
    internal var mViewMatrix = FloatArray(16)
    internal var mMVPMatrix = FloatArray(16)
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
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

    private var program: Int = 0
    private var avPosition: Int = 0
    //纹理位置
    private var afPosition: Int = 0
    private var vMaskTexture: Int = 0
    //纹理id
    //vbo id
    private var vboId: Int = 0

    private val mMaskTextureData = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f)



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

    lateinit var mBitmap: Bitmap
    val mTextureIds = IntArray(2)

    fun onSurfaceCreated() {
        program = ShaderUtils.createProgram(glView.context.resources, "shader/scrawl_vertex.vert",
                "shader/scrawl.frag")

        if (program > 0) {
            //获取顶点坐标字段
            avPosition = GLES20.glGetAttribLocation(program, "vPosition")
            //获取纹理坐标字段
            afPosition = GLES20.glGetAttribLocation(program, "vCoord")
            vMaskTexture = GLES20.glGetAttribLocation(program, "vCoord2")

            mBitmap = BitmapFactory.decodeResource(glView.context.resources, R.mipmap.attack)
                    ?: return

            var circle = createBp(100, 100)
            //创建vbo

            val createVertex = CoordinateTransform.createVertex(glView, mBitmap, circle, (glView.width / 2).toFloat(), (glView.height / 2).toFloat())
            val createTexture = CoordinateTransform.createTexture(glView, mBitmap, circle, (glView.width / 2).toFloat(), (glView.height / 2).toFloat())

            vboId = VBOHelper.createVBO(createVertex, createTexture!!)

            //创建纹理
            GLES20.glGenTextures(2, mTextureIds, 0)
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
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, circle, 0)
        }
    }

    fun draw() {
        //使用程序
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)

        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(avPosition)
        GLES20.glEnableVertexAttribArray(afPosition)


        GLES20.glEnableVertexAttribArray(vMaskTexture)


        var b = ByteBuffer.allocateDirect(mMaskTextureData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mMaskTextureData)
        b.position(0)
        GLES20.glVertexAttribPointer(vMaskTexture, 2, GLES20.GL_FLOAT, false, 0, b)

        val vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")

        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mMVPMatrix, 0)

        val vTexture = GLES20.glGetUniformLocation(program, "vTexture")
        val vTextureSrc = GLES20.glGetUniformLocation(program, "vTextureSrc")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureIds[0])
        GLES20.glUniform1i(vTexture,0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureIds[1])
        GLES20.glUniform1i(vTextureSrc,1)


        VBOHelper.useVboDraw(vboId, avPosition, afPosition)

        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(avPosition)
        GLES20.glDisableVertexAttribArray(afPosition)
        GLES20.glDisableVertexAttribArray(vMaskTexture)

    }

}
