package com.test.opengl.constant

import com.test.opengl.gesture.*
import com.test.opengl.scrawl.ScrawlRender
import com.test.opengl.shape.*

enum  class GestureData constructor(private val dataName: String, private val c: Class<out Shape>) {
    TRANSLATE("translate", Translate::class.java),
    SCALE("scale", Scale::class.java),
    TRANSLATE_AND_SCALE("translate&scale", TranslateAndScale::class.java),
    VBO("vbo", VBOTest::class.java),
    SCRAWLRENDER("ScrawlRender", ScrawlRender::class.java);

    fun getDataName(): String{
        return dataName;
    }
    fun getC(): Class<out Shape>{
        return c;
    }
}
