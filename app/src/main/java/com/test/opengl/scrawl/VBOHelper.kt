package com.test.opengl.scrawl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.view.View
import java.nio.ByteBuffer
import java.nio.ByteOrder

object VBOHelper {
    fun createVBO(vertexData: FloatArray, textureData: FloatArray): Int {
        var vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        vertexBuffer.position(0)

        var textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData)
        textureBuffer.position(0)
        val vbos = IntArray(1)
        GLES20.glGenBuffers(vbos.size, vbos, 0)
        var vboId = vbos[0]
        //2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW)
        //4. 为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
        //5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        return vboId
    }

    fun useVboDraw(vboId:Int,vPosition:Int,fPosition:Int) {
        //1. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //2. 设置顶点数据
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, 0)
        // 默认四个顶点每个顶点x y  占4字节
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 0, 2*4* 4)
        //3. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }


    fun createVertex(glView:View,mBitmap:Bitmap,mask:Bitmap,x: Float, y: Float) :FloatArray{
        val i = mBitmap.width / mBitmap.height.toFloat()
        val realH = glView.width / i


        var centerX = (x / glView.width) * 2 - 1
        var centerY = -(2 * y / realH - glView.height / realH)
        val offset = (mask.width.toFloat() /glView.width.toFloat())
        val offset2 = offset*i
        var vertexData = floatArrayOf(
                centerX-offset,centerY+offset2,
                centerX-offset,centerY-offset2,
                centerX+offset,centerY+offset2,
                centerX+offset,centerY-offset2
        )
        return vertexData
    }



    fun createTexture(glView:View,mBitmap:Bitmap,mask:Bitmap,x: Float, y: Float) :FloatArray?{
        val i = mBitmap.width / mBitmap.height.toFloat()
        val realH = glView.width / i
        if (y<(glView.height-realH)/2|| y>glView.height-realH/2)
            return null
        var centerX = (x / glView.width)
        var centerY = (y - (glView.height-realH)/2)/realH
        val offset = (mask.width.toFloat() / glView.width.toFloat())/2
        val offset2 = offset*i
        var textureData = floatArrayOf(
                centerX-offset,centerY-offset2,
                centerX-offset,centerY+offset2,
                centerX+offset,centerY-offset2,
                centerX+offset,centerY+offset2
        )
        return textureData
    }
}
