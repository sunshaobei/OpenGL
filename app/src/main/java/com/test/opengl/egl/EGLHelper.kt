package com.test.opengl.egl

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

/**
 * EGL执行流程
 * a, 选择Display
 * b, 选择Config
 * c, 创建Surface
 * d, 创建Context
 * e, 指定当前的环境为绘制环境
 */

class EGLHelper {

    lateinit var mEgl: EGL10
    lateinit var mEglDisplay: EGLDisplay
    lateinit var mEglConfig: EGLConfig
    lateinit var mEglSurface: EGLSurface
    lateinit var mEglContext: EGLContext
    lateinit var mGL: GL10

    private var surfaceType = SURFACE_PBUFFER
    private var surface_native_obj: Any? = null

    private var red = 8
    private var green = 8
    private var blue = 8
    private var alpha = 8
    private var depth = 16
    private var renderType = 4
    private val bufferType = EGL10.EGL_SINGLE_BUFFER
    private val shareContext = EGL10.EGL_NO_CONTEXT


    fun config(red: Int, green: Int, blue: Int, alpha: Int, depth: Int, renderType: Int) {
        this.red = red
        this.green = green
        this.blue = blue
        this.alpha = alpha
        this.depth = depth
        this.renderType = renderType
    }

    fun setSurfaceType(type: Int, vararg obj: Any) {
        this.surfaceType = type
        if (obj != null) {
            this.surface_native_obj = obj[0]
        }
    }

    fun eglInit(width: Int, height: Int): GlError {
        val attributes = intArrayOf(EGL10.EGL_RED_SIZE, red, //指定RGB中的R大小（bits）
                EGL10.EGL_GREEN_SIZE, green, //指定G大小
                EGL10.EGL_BLUE_SIZE, blue, //指定B大小
                EGL10.EGL_ALPHA_SIZE, alpha, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL10.EGL_DEPTH_SIZE, depth, //指定深度缓存(Z Buffer)大小
                EGL10.EGL_RENDERABLE_TYPE, renderType, //指定渲染api版本, EGL14.EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE)  //总是以EGL10.EGL_NONE结尾

        //获取Display
        mEgl = EGLContext.getEGL() as EGL10
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        val version = IntArray(2)    //主版本号和副版本号
        mEgl.eglInitialize(mEglDisplay, version)
        //选择Config
        val configNum = IntArray(1)
        mEgl.eglChooseConfig(mEglDisplay, attributes, null, 0, configNum)
        if (configNum[0] == 0) {
            return GlError.ConfigErr
        }
        val c = arrayOfNulls<EGLConfig>(configNum[0])
        mEgl.eglChooseConfig(mEglDisplay, attributes, c, configNum[0], configNum)
        mEglConfig = c[0]!!
        //创建Surface
        val surAttr = intArrayOf(EGL10.EGL_WIDTH, width, EGL10.EGL_HEIGHT, height, EGL10.EGL_NONE)
        mEglSurface = createSurface(surAttr)
        //创建Context
        val contextAttr = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, shareContext, contextAttr)
        makeCurrent()
        return GlError.OK
    }

    fun makeCurrent() {
        mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)
        mGL = mEglContext.gl as GL10
    }

    fun destroy() {
        mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface)
        mEgl.eglDestroyContext(mEglDisplay, mEglContext)
        mEgl.eglTerminate(mEglDisplay)
    }

    private fun createSurface(attr: IntArray): EGLSurface {
        when (surfaceType) {
            SURFACE_WINDOW -> return mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface_native_obj, attr)
            SURFACE_PIM -> return mEgl.eglCreatePixmapSurface(mEglDisplay, mEglConfig, surface_native_obj, attr)
            else -> return mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig, attr)
        }
    }

    companion object {

        private val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        val SURFACE_PBUFFER = 1
        val SURFACE_PIM = 2
        val SURFACE_WINDOW = 3
    }

}
