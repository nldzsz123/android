package com.videoplayer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLHardRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private final String TAG = "GLHardRender";
    private boolean openDDLog = true;


    public GLHardRender() {
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);

        /** 将矩阵mSTMatrix重置为4x4的标准矩阵 4x4 标准矩阵
         *   1 0 0 0
         *   0 1 0 0
         *   0 0 1 0
         *   0 0 0 1
         */
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public void onPause() {
    }

    public void onResume() {
//        mLastTime = SystemClock.elapsedRealtimeNanos();
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized(this) {
            if (updateSurface) {
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }

        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mVertices.position(VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                VERTICES_DATA_STRIDE_BYTES, mVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mVertices.position(VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                VERTICES_DATA_STRIDE_BYTES, mVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

//        // ======= 将矩阵进行旋转 ======== ///
//        long now = SystemClock.elapsedRealtimeNanos();
//        mRunTime += (now - mLastTime);
//        mLastTime = now;
//        double d = ((double)mRunTime) / 1000000000;// 绘制时间差(单位秒)
//        /** 将矩阵mMMatrix重置为4x4的标准矩阵 4x4 标准矩阵
//         *   1 0 0 0
//         *   0 1 0 0
//         *   0 0 1 0
//         *   0 0 0 1
//         */
//        Matrix.setIdentityM(mMMatrix, 0);
//        // 将矩阵进行旋转
//        Matrix.rotateM(mMMatrix, 0, 30, (float)Math.sin(d), (float)Math.cos(d), 0);
//        // 矩阵相乘
//        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
//        // ======= 将矩阵进行旋转 ======== ///

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mProjMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        DDLog.log("onSurfaceChanged");
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);
        mRatio = (float) width / height;
//        Matrix.frustumM(mProjMatrix, 0, -mRatio, mRatio, -1, 1, 3, 7);
        Matrix.orthoM(mProjMatrix,0,-1.0f,1.0f,-1f,1f,-1f,1f);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
//        DDLog.log("onSurfaceCreated");
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.

        /* Set up alpha blending and an Android background color */
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(0.643f, 0.776f, 0.223f, 1.0f);

        /* Set up shaders and handles to their variables */
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uSTMatrix");
        }

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        // Can't do mipmapping with camera source
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        // Clamp to edge is the only option
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameteri mTextureID");

        /*
         * Create the SurfaceTexture that will feed this textureID, and pass it to the camera
         */
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 4f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        synchronized(this) {
            updateSurface = false;
        }
    }

    /** 1、当有视频帧过来时会调用此方法，该方法调用线程不固定(不和解码同线程)，有可能在主线程调用
     *  2、调用频率为平均1秒30fps
     */
    private long lastTime = 0;
    private int frameNum = 0;
    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;

        if (openDDLog) {
            DDLog.log( "onFrameAvailable " + surface.getTimestamp() + "in thread==>"+Thread.currentThread());
            frameNum++;
            DDLog.log("时间==>"+(System.currentTimeMillis()-lastTime) + "num==>"+frameNum%30);
            lastTime = System.currentTimeMillis();
        }
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                DDLog.log("Could not compile shader " + shaderType + ":");
                DDLog.log(GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                DDLog.log("Could not link program: ");
                DDLog.log(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            DDLog.log(op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTICES_DATA_UV_OFFSET = 3;

    // 前面是opengl 坐标系统，后面是贴图的坐标系统
    private final float[] mVerticesData = {
            // X, Y, Z, U, V
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
    };
    private FloatBuffer mVertices;

    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private float mRatio = 1.0f;
    private SurfaceTexture mSurface;
    private boolean updateSurface = false;
    private long mLastTime = -1;
    private long mRunTime = 0;

    // Magic key
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    // 设置surface
    public void setNativeSurface() {

        DDLog.log("thread in==>"+Thread.currentThread());
        Surface s = new Surface(mSurface);
        NativeCode.setSurface(s);
        s.release();
    }
}
