/*
 *
 * NoFilter.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.test.opengl.filter

import android.content.res.Resources

/**
 * Description:
 */
class BlendFilter(res: Resources) : AFilter(res) {

    override fun onClear() {

    }

    override fun onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
                "shader/base_fragment.sh")
    }

    override fun onSizeChanged(width: Int, height: Int) {

    }

}
