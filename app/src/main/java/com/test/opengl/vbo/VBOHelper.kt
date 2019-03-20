package com.test.opengl.vbo

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * vbo 工具类 二维。不做三维拓展
 */

object VBOHelper {
//    fun createVBO(vertexData: FloatArray, textureData: FloatArray): Int {
//
//        var vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//                .put(vertexData)
//        vertexBuffer.position(0)
//
//        var textureBuffer = ByteBuffer.allocateDirect(textureData.size * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//                .put(textureData)
//        textureBuffer.position(0)
//        val vbos = IntArray(1)
//        GLES20.glGenBuffers(vbos.size, vbos, 0)
//        var vboId = vbos[0]
//        //2. 绑定VBO
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
//        //3. 分配VBO需要的缓存大小
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + textureData.size * 4, null, GLES20.GL_STATIC_DRAW)
//        //4. 为VBO设置顶点数据的值
//        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
//        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, textureData.size * 4, textureBuffer)
//        //5. 解绑VBO
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
//        return vboId
//    }

//
//    fun useVboDraw(vboId: Int, vPosition: Int, fPosition: Int) {
//        //1. 绑定VBO
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
//        //2. 设置顶点数据
//        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, 0)
//        // 默认四个顶点每个顶点x y  占4字节
//        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 0, 2 * 4 * 4)
//        //3. 解绑VBO
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
//    }

    fun createVBO(vararg arrays: FloatArray): Int {
        val vbos = IntArray(1)
        GLES20.glGenBuffers(vbos.size, vbos, 0)
        var vboId = vbos[0]
        //2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)


        var buffers = ArrayList<FloatBuffer>()
        var size = 0
        for (floatArray in arrays) {
            var buffer = ByteBuffer.allocateDirect(floatArray.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(floatArray)
            buffer.position(0)
            buffers.add(buffer)
            size += floatArray.size * 4
        }
        //3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size, null, GLES20.GL_STATIC_DRAW)
        //4. 为VBO设置顶点数据的值
        for (i in 0 until buffers.size) {
            var k = 0
            var size2 = 0
            while (k < i) {
                size2 += arrays[k].size*4
                k++
            }
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, size2, arrays[i].size*4, buffers[i])
        }
        //5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        return vboId
    }

    fun useVboDraw(vboId: Int,vararg position: Int) {
        //1. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //2. 设置顶点数据
        for (i in position) {
            GLES20.glVertexAttribPointer(i, 2, GLES20.GL_FLOAT, false, 0, 2 * 4 * 4*i)
        }
        //3. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}
