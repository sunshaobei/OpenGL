package com.test.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FGLActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var glSurfaceView = GLSurfaceView(this)
        setContentView(glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
            override fun onDrawFrame(gl: GL10?) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
            }
        })
    }
}
