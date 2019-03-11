package com.test.opengl.fragment

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.opengl.R
import com.test.opengl.shape.Shape

class ShapeFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var glView: GLSurfaceView
    private  var rederSet:Boolean =false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_shape, null, false)
        glView = rootView.findViewById(R.id.glView)
        glView.setEGLContextClientVersion(2)
        val get = arguments!!.get("c") as Class<*>
        glView.setRenderer(get.newInstance() as GLSurfaceView.Renderer?)
        // 按需渲染
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        rederSet = true
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (rederSet) {
            glView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (rederSet) {
            glView.onPause()
        }
    }
}
