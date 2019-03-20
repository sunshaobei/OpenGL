package com.test.opengl.fragment


import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.gesture.Translate
import com.test.opengl.shape.Shape


/**
 * A simple [Fragment] subclass.
 *
 */
class GestureFragment : Fragment() {

    private lateinit var rootView: FrameLayout
    private lateinit var glView: GLView
    private  var rederSet:Boolean =false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_shape, null, false) as FrameLayout
        glView = rootView.findViewById(R.id.glView)
        glView.setEGLContextClientVersion(2)
        val get = arguments!!.get("c") as Class<*>
        var renderer:GLSurfaceView.Renderer
        try {
            val constructor = get.getDeclaredConstructor(GLView::class.java)
            constructor.isAccessible = true
            renderer = constructor.newInstance(glView) as Shape
        } catch (e: Exception) {
            e.printStackTrace()
            renderer = Translate(glView)
        }

        glView.setRenderer(renderer)
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
