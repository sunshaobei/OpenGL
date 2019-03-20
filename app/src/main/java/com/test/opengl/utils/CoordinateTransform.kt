package com.test.opengl.utils

import android.graphics.Bitmap
import android.view.View

/**
 *  坐标转换工具
 */
object CoordinateTransform{

    fun createVertex(glView: View, mBitmap: Bitmap, mask: Bitmap, x: Float, y: Float) :FloatArray{
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


    fun createTexture(glView: View, mBitmap: Bitmap, mask: Bitmap, x: Float, y: Float) :FloatArray?{
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
