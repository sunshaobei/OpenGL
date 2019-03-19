package com.test.opengl.gesture

import android.opengl.GLES20
import android.util.Log
import android.view.MotionEvent
import com.test.opengl.GLView
import com.test.opengl.shape.Shape
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ScrawlRender constructor(var glView: GLView) : Shape(), GLView.DispatchTouchListener {

    var currentLines: GLLine ?= null
    var linesList: ArrayList<GLLine> = ArrayList()

    var pointSize = 20f


    var height = 0
    var width = 0
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var x = event!!.x
        var y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentLines = GLLine(this,pointSize, width)
                currentLines!!.down(toOpenGLCoord(event.x, true), toOpenGLCoord(event.y, false))
                synchronized(linesList) {
                    linesList.add(currentLines!!)
                }
                glView.requestRender()
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentLines==null){
                    currentLines = GLLine(this,pointSize, width)
                    currentLines!!.down(toOpenGLCoord(event.x, true), toOpenGLCoord(event.y, false))
                    synchronized(linesList) {
                        linesList.add(currentLines!!)
                    }
                }
                currentLines!!.drawPoint(toOpenGLCoord(event.x, true), toOpenGLCoord(event.y, false))
                glView.requestRender()
            }
            MotionEvent.ACTION_UP -> {
            }
        }

        return true
    }

    init {
        glView.setDispatchTouchListener(this)
    }


    fun setTouch(){
        glView.setDispatchTouchListener(this)
    }

    private var TAG: String = ScrawlRender::class.java.simpleName

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "attribute float aPointSize;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "  vColor=aColor;" +
            " gl_PointSize = aPointSize;" +
            "}"

    private val fragmentShaderCode = (
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "float dist = length(gl_PointCoord - vec2(0.5));" +
                    "  float d = distance(gl_PointCoord, vec2(0.5, 0.5));" +
                    "  if(d < 0.5) " +
                    "   {gl_FragColor = vColor;} " +
                    "  else { discard; }" +
                    "}")


    private var mProgram: Int = 0



    var mPositionHandle = 0
    var mColorHandler = 0
    var mPointSize = 0;
    override fun onDrawFrame(gl: GL10) {
        Log.e(TAG, "onDrawFrame")
        GLES20.glUseProgram(mProgram)
        GLES20.glVertexAttrib1f(mPointSize, pointSize)
        //启用三角形顶点
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // 启用颜色句柄
        GLES20.glEnableVertexAttribArray(mColorHandler)
        synchronized(linesList) {
            for (glLine in linesList) {
                glLine.drawTo(gl, mProgram)
            }
        }
        // 禁用顶点坐标id
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mColorHandler)
    }


    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged")
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
    }


    var vertexShader = 0
    var fragmentShader = 0
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.e(TAG, "onSurfaceCreated")
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        //将定点着色器添加到es程序
        GLES20.glAttachShader(mProgram, vertexShader)
        //将片元着色器添加到es程序
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)

        mPointSize = GLES20.glGetAttribLocation(mProgram, "aPointSize")
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //获取元着色器的vColor成员句柄
        mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor")
    }


    fun toOpenGLCoord(value: Float, isWidth: Boolean): Float {
        if (isWidth) {
            return (value / glView.width) * 2 - 1
        } else {
            return -((value / glView.height) * 2 - 1)
        }
    }


}