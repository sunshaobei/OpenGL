package com.test.opengl.constant

import com.test.opengl.gesture.*
import com.test.opengl.scrawl.ScrawlTest
import com.test.opengl.shape.*

enum  class GestureData constructor(private val dataName: String, private val c: Class<out Shape>) {
    TRANSLATE("translate", Translate::class.java),
    SCALE("scale", Scale::class.java),
    TRANSLATE_AND_SCALE("translate&scale", TranslateAndScale::class.java),
    SCRAWL("scrawl", Scrawl::class.java),
    TESTSCRAWL("TestScrawl", TestScrawl::class.java),
    VBO("vbo", VBOTest::class.java),
    SCRAWLTEST("scrawltest", ScrawlTest::class.java);

    fun getDataName(): String{
        return dataName;
    }
    fun getC(): Class<out Shape>{
        return c;
    }
}
