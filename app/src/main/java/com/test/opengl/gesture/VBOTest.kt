package com.test.opengl.gesture

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.shape.Shape
import com.test.opengl.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
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

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0,width,height)
    }

    private val vertexCount = vertexData.size / COORDS_PER_VERTEX
    //每一次取的总的点 大小
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    //位置
    private val vertexBuffer: FloatBuffer
    //纹理
    private val textureBuffer: FloatBuffer
    private var program: Int = 0
    private var avPosition: Int = 0
    //纹理位置
    private var afPosition: Int = 0
    //纹理id
    private var textureId: Int = 0
    //vbo id
    private var vboId: Int = 0

    init {

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        vertexBuffer.position(0)


        textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData)
        textureBuffer.position(0)
    }


    fun onSurfaceCreated() {
        program = ShaderUtils.createProgram(glView.context.resources, "shader/base_vertex.sh",
                "shader/base_fragment.sh")

        if (program > 0) {
            //获取顶点坐标字段
            avPosition = GLES20.glGetAttribLocation(program, "vPosition")
            //获取纹理坐标字段
            afPosition = GLES20.glGetAttribLocation(program, "vCoord")

            //创建vbo
            createVBO()

            val textureIds = IntArray(1)
            //创建纹理
            GLES20.glGenTextures(1, textureIds, 0)
            if (textureIds[0] == 0) {
                return
            }
            textureId = textureIds[0]
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            //环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            //过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            val bitmap = BitmapFactory.decodeResource(glView.context.resources, R.mipmap.attack)
                    ?: return

//设置纹理为2d图片
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
    }

    fun draw() {
        //使用程序
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(avPosition)
        GLES20.glEnableVertexAttribArray(afPosition)

        val vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")

        var mMVPMatrix = FloatArray(16)
        Matrix.setIdentityM(mMVPMatrix,0)
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mMVPMatrix, 0)

        //直接设置
        // 设置顶点位置值
        //        GLES20.glVertexAttribPointer(avPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        //设置纹理值
        //        GLES20.glVertexAttribPointer(afPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureBuffer);

        //使用vbo设置
//        val glGetUniformLocation = GLES20.glGetUniformLocation(program, "vTexture")
//        GLES20.glUniform1i(glGetUniformLocation,0)
        useVboDraw()

        //绘制 GLES20.GL_TRIANGLE_STRIP:复用坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(avPosition)
        GLES20.glDisableVertexAttribArray(afPosition)

    }

    private fun createVBO() {
        //1. 创建VBO
        val vbos = IntArray(1)
        GLES20.glGenBuffers(vbos.size, vbos, 0)
        vboId = vbos[0]
        //2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW)
        //4. 为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        //5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    private fun useVboDraw() {
        //1. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //2. 设置顶点数据
        GLES20.glVertexAttribPointer(avPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 0)
        GLES20.glVertexAttribPointer(afPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexData.size * 4)
        //3. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    companion object {


        //顶点坐标
        internal var vertexData = floatArrayOf(// in counterclockwise order:
                -1f, -1f, 0.0f, // bottom left
                1f, -1f, 0.0f, // bottom right
                -1f, 1f, 0.0f, // top left
                1f, 1f, 0.0f)// top right

        //纹理坐标  对应顶点坐标  与之映射
        internal var textureData = floatArrayOf(// in counterclockwise order:
                0f, 1f, 0.0f, // bottom left
                1f, 1f, 0.0f, // bottom right
                0f, 0f, 0.0f, // top left
                1f, 0f, 0.0f)// top right

        //每一次取点的时候取几个点
        internal val COORDS_PER_VERTEX = 3
    }
}
