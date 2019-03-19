//package com.test.opengl.gesture
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.opengl.GLES20
//import android.opengl.GLUtils
//import android.util.Log
//import android.view.MotionEvent
//import com.test.opengl.AppContext
//import com.test.opengl.GLView
//import com.test.opengl.R
//import com.test.opengl.filter.NoFilter
//import com.test.opengl.filter.NoReverseFilter
//import com.test.opengl.shape.Shape
//import com.test.opengl.utils.MatrixUtils
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//
//class ScrawlCopy constructor(var glView: GLView) : Shape(), GLView.DispatchTouchListener {
//
//    lateinit var currentLines: GLLine
//    var linesList: ArrayList<GLLine> = ArrayList()
//
//    var pointSize = 20f
//
//
//    var height = 0
//    var width = 0
//    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
//        var x = event!!.x
//        var y = event.y
//
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                currentLines = GLLine(pointSize, width)
//                currentLines.down(toOpenGLCoord(event.x, true), toOpenGLCoord(event.y, false))
//                synchronized(linesList) {
//                    linesList.add(currentLines)
//                }
//                glView.requestRender()
//            }
//            MotionEvent.ACTION_MOVE -> {
//                currentLines.drawPoint(toOpenGLCoord(event.x, true), toOpenGLCoord(event.y, false))
//                glView.requestRender()
//            }
//            MotionEvent.ACTION_UP -> {
//            }
//        }
//
//        return true
//    }
//
//    init {
//        glView.setDispatchTouchListener(this)
//    }
//
//
//    var src = NoFilter(glView.context.resources)
//    var reverse = NoReverseFilter(glView.context.resources)
//
//
//    private var TAG: String = ScrawlCopy::class.java.simpleName
//
//    private val vertexShaderCode = "attribute vec4 vPosition;" +
//            "uniform mat4 vMatrix;" +
//            "varying  vec4 vColor;" +
//            "attribute vec4 aColor;" +
//            "attribute float aPointSize;" +
//            "void main() {" +
//            "  gl_Position = vPosition;" +
//            "  vColor=aColor;" +
//            " gl_PointSize = aPointSize;" +
//            "}"
//
//    private val fragmentShaderCode = (
//            "precision mediump float;" +
//                    "varying vec4 vColor;" +
//                    "void main() {" +
//                    "float dist = length(gl_PointCoord - vec2(0.5));" +
//                    "  float d = distance(gl_PointCoord, vec2(0.5, 0.5));" +
//                    "  if(d < 0.5) " +
//                    "   {gl_FragColor = vColor;} " +
//                    "  else { discard; }" +
//                    "}")
//
//
//    private var mProgram: Int = 0
//
//
//    private val fFrame = IntArray(1)
//    private val fTexture = IntArray(2)
//
//    override fun onDrawFrame(gl: GL10) {
//        Log.e(TAG, "onDrawFrame")
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//
//        GLES20.glEnable(GLES20.GL_BLEND)
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
//
//        //清除颜色 深层 缓冲
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0])
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
//                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0])
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D, fTexture[0], 0)
//        GLES20.glViewport(0, 0, width, height)
//
//
//
//
//        mProgram = GLES20.glCreateProgram()
//        //将定点着色器添加到es程序
//        GLES20.glAttachShader(mProgram, vertexShader)
//        //将片元着色器添加到es程序
//        GLES20.glAttachShader(mProgram, fragmentShader)
//        GLES20.glLinkProgram(mProgram)
//
//        GLES20.glUseProgram(mProgram)
//
//
//        val glGetAttribLocation = GLES20.glGetAttribLocation(mProgram, "aPointSize")
//        GLES20.glVertexAttrib1f(glGetAttribLocation, pointSize)
//        synchronized(linesList) {
//            for (glLine in linesList) {
//                glLine.drawTo(gl, mProgram)
//            }
//        }
//
//
////        var mBuffer = ByteBuffer.allocate(width * height * 4);
////        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA,
////                GLES20.GL_UNSIGNED_BYTE, mBuffer);
////        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
////        bitmap.copyPixelsFromBuffer(mBuffer)
//
//
////        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
//
//
//
//
//        //生成纹理
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[1])
//        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
//        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
//        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
//        //根据以上指定的参数，生成一个2D纹理
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
////
//        GLES20.glViewport(0, 0, width, height)
//
//
//        src.textureId = fTexture[1]
//        src.draw()
//
//        reverse.textureId = fTexture[0]
//        reverse.draw()
//
//    }
//
//
//    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
//        Log.e(TAG, "onSurfaceChanged")
//        this.width = width
//        this.height = height
//        GLES20.glViewport(0, 0, width, height)
//        src.setSize(width, height)
//        reverse.setSize(width, height)
//        MatrixUtils.getMatrix(reverse.matrix, MatrixUtils.TYPE_CENTERCROP, width, height,
//                width, height)
//        MatrixUtils.getMatrix(src.matrix, MatrixUtils.TYPE_CENTERINSIDE, mBitmap!!.width, mBitmap!!.height,
//                width, height)
//    }
//
//
//    var vertexShader = 0
//    var fragmentShader = 0
//    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
//        Log.e(TAG, "onSurfaceCreated")
//
//        mBitmap = createBp()
//
//        src.create()
//        reverse.create()
//
//        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
//        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
//
//        GLES20.glGenFramebuffers(1, fFrame, 0)
//        GLES20.glGenTextures(2, fTexture, 0)
//
//
//
//    }
//
//
//    fun toOpenGLCoord(value: Float, isWidth: Boolean): Float {
//        if (isWidth) {
//            return (value / glView.width) * 2 - 1
//        } else {
//            return -((value / glView.height) * 2 - 1)
//        }
//    }
//
//    private fun createBp(): Bitmap {
//        var icon = BitmapFactory.decodeResource(AppContext.getContext().resources, R.mipmap.attack)
//        return icon
//    }
//
//    private var mBitmap: Bitmap? = null
//}