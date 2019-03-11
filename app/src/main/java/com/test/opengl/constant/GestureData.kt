package com.test.opengl.constant

import com.test.opengl.gesture.Scale
import com.test.opengl.gesture.Translate
import com.test.opengl.gesture.TranslateAndScale
import com.test.opengl.shape.*

enum  class GestureData constructor(private val dataName: String, private val c: Class<out Shape>) {
    TRANSLATE("translate", Translate::class.java),
    SCALE("scale", Scale::class.java),
    TRANSLATE_AND_SCALE("translate&scale", TranslateAndScale::class.java);

    fun getDataName(): String{
        return dataName;
    }
    fun getC(): Class<out Shape>{
        return c;
    }
}
