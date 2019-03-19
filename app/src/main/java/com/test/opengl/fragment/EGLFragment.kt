package com.test.opengl.fragment


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.test.opengl.AppContext
import com.test.opengl.GLView
import com.test.opengl.R
import com.test.opengl.egl.GLES20BackEnv
import com.test.opengl.filter.NoFilter
import com.test.opengl.utils.MatrixUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * A simple [Fragment] subclass.
 *
 */
class EGLFragment : Fragment() ,GLSurfaceView.Renderer{


    lateinit var src :NoFilter
    var textureId = 0

    override fun onDrawFrame(gl: GL10?) {
        //清除缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        src.textureId = textureId
        src.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0,width,height)
        glView
        src.setSize(width,height)
        MatrixUtils.getMatrix(src.matrix, MatrixUtils.TYPE_CENTERINSIDE, mBitmap.width, mBitmap.height,
                width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        src.create()
    }

    private lateinit var rootView: FrameLayout
    private lateinit var glView: GLView
    private  var rederSet:Boolean =false
    lateinit var mBitmap :Bitmap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_gesture, null, false) as FrameLayout
        glView = rootView.findViewById(R.id.glView)
        glView.setEGLContextClientVersion(2)
        src = NoFilter(context!!.resources)
         mBitmap = createBp()
        val gleS20BackEnv = GLES20BackEnv(mBitmap.width, mBitmap.height)
        gleS20BackEnv.setThreadOwner(Looper.getMainLooper().thread.name)
        gleS20BackEnv.setFilter(NoFilter(context!!.resources))
        gleS20BackEnv.setInput(mBitmap)
        gleS20BackEnv.bitmap
        this.textureId = gleS20BackEnv.textureId!!
        val bitmap1 = gleS20BackEnv.bitmap
        glView.setRenderer(this)
        // 按需渲染
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        rederSet = true
        return rootView
    }

    private fun createBp(): Bitmap {
        var icon = BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
        return icon
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
