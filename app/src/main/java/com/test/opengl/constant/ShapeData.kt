package com.test.opengl.constant

import com.test.opengl.shape.*

enum  class ShapeData constructor(private val dataName: String, private val c: Class<out Shape>) {
    TRIANGLE("三角形", Triangle::class.java),
    SQUARE("正方形", Square::class.java),
    TRIANGLE2("等腰直角三角形", Triangle2::class.java),
    OVAL("圆形",Oval::class.java),
    OVAL2("圆形2",Oval2::class.java),
    CUBE("正方体",Cube::class.java),
    IMAGE("图片",Image::class.java);

    fun getDataName(): String{
        return dataName;
    }
    fun getC(): Class<out Shape>{
        return c;
    }
}
