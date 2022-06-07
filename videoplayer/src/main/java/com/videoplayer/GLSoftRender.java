package com.videoplayer;

import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by feipai1 on 2017/4/28.
 */

public class GLSoftRender implements GLSurfaceView.Renderer {

    private GLSurfaceView mSurfaceView;
    private GLProgram mProgram;
    private int mVideoWidth = -1, mVideoHeight = -1;
    private ArrayList<ByteBuffer> yLists;
    private ArrayList<ByteBuffer> uLists;
    private ArrayList<ByteBuffer> vLists;
    private boolean isNeedChangeViewPort;

    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    public GLSoftRender(GLSurfaceView sur) {
        mSurfaceView = sur;
        mProgram = new GLProgram(1);
        yLists = new ArrayList<ByteBuffer>(50);
        uLists = new ArrayList<ByteBuffer>(50);
        vLists = new ArrayList<ByteBuffer>(50);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        DDLog.log("onSurfaceCreated");
        mProgram.doInitProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        DDLog.log("onSurfaceChanged width==>" + width + "height==>" + height);
        mProgram.setSurfaceSize(width, height);
        isNeedChangeViewPort = true;

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (y != null) {
//            DDLog.log("目前缓冲区大小==>"+yLists.size());
//            ByteBuffer y = yLists.get(0);
//            ByteBuffer u = uLists.get(0);
//            ByteBuffer v = vLists.get(0);

                if (isNeedChangeViewPort) {
                    mProgram.changeGLViewPort(mVideoWidth, mVideoHeight);
                    isNeedChangeViewPort = false;
                }
                y.position(0);
                u.position(0);
                v.position(0);
//                DDLog.log("开始画这一帧");
                long begin = SystemClock.currentThreadTimeMillis();
                mProgram.doInitTextures(y, u, v, mVideoWidth, mVideoHeight);
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                mProgram.drawFrame();
//                DDLog.log("结束画这一帧 time " + (SystemClock.currentThreadTimeMillis() - begin) + " ms");

//            yLists.remove(0);
//            uLists.remove(0);
//            vLists.remove(0);
            }
        }

    }

    public void setGridType(int type) {
        mProgram.setLineType(type);
    }
    public void clearFrames() {
        if (y != null) {
            y.clear();
            u.clear();
            v.clear();
        }

    }

    public void update(final byte[] ydata, final byte[] udata, final byte[] vdata, int w, int h) {

        if (mVideoWidth != w || mVideoHeight != h) {
            mVideoWidth = w;
            mVideoHeight = h;
            isNeedChangeViewPort = true;
            int yarraySize = w * h;
            int uvarraySize = yarraySize / 4;
            y = ByteBuffer.allocate(yarraySize);
            u = ByteBuffer.allocate(uvarraySize);
            v = ByteBuffer.allocate(uvarraySize);
        }
        synchronized (this) {
            y.clear();
            u.clear();
            v.clear();
            y.put(ydata, 0, ydata.length);
            u.put(udata, 0, udata.length);
            v.put(vdata, 0, vdata.length);
        }

//        mSurfaceView.queueEvent(new Runnable() {
//            @Override
//            public void run() {
////                yLists.add(oy);
////                uLists.add(ou);
////                vLists.add(ov);
//                y = oy;
//                u = ou;
//                v = ov;
//            }
//        });
//        synchronized (this) {
//            y = oy;
//            u = ou;
//            v = ov;
//        }
        mSurfaceView.requestRender();
    }
}

class GLProgram {

    private static String TAG = GLProgram.class.getSimpleName();

    private static int SIZE_STANDARD = 0;
    private static int SIZE_BEST_FIT = 1;
    private static int SIZE_FULLSCREEN = 2;
    private int displayMode;
    private int dispWidth;
    private int dispHeight;

    private int _program;
    private int _programForLine;
    public final int mWinPosition;
    private int _textureI;
    private int _textureII;
    private int _textureIII;
    private int _tIindex;
    private int _tIIindex;
    private int _tIIIindex;
    private float[] _vertices;
    private int _positionHandle = -1, _coordHandle = -1;
    private int _positionHandleForLie = -1;
    private int _yhandle = -1, _uhandle = -1, _vhandle = -1;
    private int _ytid = -1, _utid = -1, _vtid = -1;
    private ByteBuffer _vertice_buffer;
    private ByteBuffer _coord_buffer;
    private ByteBuffer _vertice_bufferForLineJGG;  //画线的
    private int         _vertice_bufferForLineLenght;    // 画线的
    private ByteBuffer _vertice_bufferForLineJGGDJX;  //画线的
    private int         _vertice_bufferForLineJGGDJXLenght;    // 画线的
    private int mType = 0;

    public GLProgram(int position) {
        if (position < 0 || position > 4) {
            throw new RuntimeException("Index can only be 0 to 4");
        }
        mWinPosition = position;
        displayMode = SIZE_BEST_FIT;
        setup(mWinPosition);
    }

    public void setSurfaceSize(int width, int height) {
        dispWidth = width;
        dispHeight = height;
    }

    private Rect destRect(int bmw, int bmh) {
        int tempx;
        int tempy;
        if (displayMode == SIZE_STANDARD) {
            tempx = (dispWidth / 2) - (bmw / 2);
            tempy = (dispHeight / 2) - (bmh / 2);
            return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
        } else if (displayMode == SIZE_BEST_FIT) {
            float bmasp = (float) bmw / (float) bmh;
            bmw = dispWidth;
            bmh = (int) (dispWidth / bmasp);
            if (bmh > dispHeight) {
                bmh = dispHeight;
                bmw = (int) (dispHeight * bmasp);
            }
            tempx = (dispWidth / 2) - (bmw / 2);
            tempy = (dispHeight / 2) - (bmh / 2);
            return new Rect(tempx, 0, tempx + bmw, dispHeight);
        } else if (displayMode == SIZE_FULLSCREEN)
            return new Rect(0, 0, dispWidth, dispHeight);
        return null;
    }
    public void setLineType(int type) {
        mType = type;
        if (type == 1) {
            if (_vertice_bufferForLineJGG == null) {
                _vertice_bufferForLineJGG = ByteBuffer.allocateDirect(verDataJGG.length * 4);
                _vertice_bufferForLineJGG.order(ByteOrder.nativeOrder());
                _vertice_bufferForLineJGG.asFloatBuffer().put(verDataJGG);
                _vertice_bufferForLineJGG.position(0);
                _vertice_bufferForLineLenght = verDataJGG.length/2;
            }

        } else {
            if (_vertice_bufferForLineJGGDJX == null) {
                _vertice_bufferForLineJGGDJX = ByteBuffer.allocateDirect(verDataJGGDJX.length * 4);
                _vertice_bufferForLineJGGDJX.order(ByteOrder.nativeOrder());
                _vertice_bufferForLineJGGDJX.asFloatBuffer().put(verDataJGGDJX);
                _vertice_bufferForLineJGGDJX.position(0);
                _vertice_bufferForLineJGGDJXLenght = verDataJGGDJX.length/2;
            }
        }
    }
    public void setup(int position) {
        switch (position) {
            case 1:
                _vertices = squareVertices1;
                _textureI = GLES20.GL_TEXTURE0;
                _textureII = GLES20.GL_TEXTURE1;
                _textureIII = GLES20.GL_TEXTURE2;
                _tIindex = 0;
                _tIIindex = 1;
                _tIIIindex = 2;
                break;
            case 2:
                _vertices = squareVertices2;
                _textureI = GLES20.GL_TEXTURE3;
                _textureII = GLES20.GL_TEXTURE4;
                _textureIII = GLES20.GL_TEXTURE5;
                _tIindex = 3;
                _tIIindex = 4;
                _tIIIindex = 5;
                break;
            case 3:
                _vertices = squareVertices3;
                _textureI = GLES20.GL_TEXTURE6;
                _textureII = GLES20.GL_TEXTURE7;
                _textureIII = GLES20.GL_TEXTURE8;
                _tIindex = 6;
                _tIIindex = 7;
                _tIIIindex = 8;
                break;
            case 4:
                _vertices = squareVertices4;
                _textureI = GLES20.GL_TEXTURE9;
                _textureII = GLES20.GL_TEXTURE10;
                _textureIII = GLES20.GL_TEXTURE11;
                _tIindex = 9;
                _tIIindex = 10;
                _tIIIindex = 11;
                break;
            case 0:
            default:
                _vertices = squareVertices;
                _textureI = GLES20.GL_TEXTURE0;
                _textureII = GLES20.GL_TEXTURE1;
                _textureIII = GLES20.GL_TEXTURE2;
                _tIindex = 0;
                _tIIindex = 1;
                _tIIIindex = 2;
                break;
        }
    }

    public void doInitProgram() {
        createBuffers();
        createProgram();
    }

    public void changeGLViewPort(int videoWidth, int videoHeight) {
        Rect newRect = destRect(videoWidth, videoHeight);
        Log.d("kk", "l==>" + newRect.left + "top==>" + newRect.top + "width==>" + newRect.width());
        GLES20.glViewport(newRect.left, newRect.top, newRect.width(), newRect.height());
    }

    private void createBuffers() {
        _vertice_buffer = ByteBuffer.allocateDirect(squareVertices.length * 4);
        _vertice_buffer.order(ByteOrder.nativeOrder());
        _vertice_buffer.asFloatBuffer().put(squareVertices);
        _vertice_buffer.position(0);

        _coord_buffer = ByteBuffer.allocateDirect(coordVertices.length * 4);
        _coord_buffer.order(ByteOrder.nativeOrder());
        _coord_buffer.asFloatBuffer().put(coordVertices);
        _coord_buffer.position(0);
    }

    private void createProgram() {

        if (_program <= 0) {
            _program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        }
        if (_programForLine <= 0) {
            _programForLine = createProgram(VERTEX_SHADER, WHITE_SHADER);
        }

        _positionHandle = GLES20.glGetAttribLocation(_program, "vPosition");
        if (_positionHandle == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        _positionHandleForLie = GLES20.glGetAttribLocation(_programForLine, "vPosition");
        if (_positionHandleForLie == -1) {
            throw new RuntimeException("Could not get attribute location for vPosition");
        }
        _coordHandle = GLES20.glGetAttribLocation(_program, "a_texCoord");
        if (_coordHandle == -1) {
            throw new RuntimeException("Could not get attribute location for a_texCoord");
        }

        _yhandle = GLES20.glGetUniformLocation(_program, "tex_y");
        if (_yhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_y");
        }
        _uhandle = GLES20.glGetUniformLocation(_program, "tex_u");
        if (_uhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_u");
        }
        _vhandle = GLES20.glGetUniformLocation(_program, "tex_v");
        if (_vhandle == -1) {
            throw new RuntimeException("Could not get uniform location for tex_v");
        }
    }

    public void doInitTextures(Buffer y, Buffer u, Buffer v, int width, int height) {

        if (_ytid >= 0) {
            GLES20.glDeleteTextures(1, new int[]{_ytid}, 0);
            checkGlError("glDeleteTextures Y");
        }

        int[] texturesY = new int[1];
        GLES20.glGenTextures(1, texturesY, 0);
        checkGlError("glGenTextures Y");
        _ytid = texturesY[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
        checkGlError("glTexImage2D Y");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (_utid >= 0) {
//            DDLog.log("glDeleteTextures U");
            GLES20.glDeleteTextures(1, new int[]{_utid}, 0);
            checkGlError("glDeleteTextures U");
        }

        int[] texturesU = new int[1];
        GLES20.glGenTextures(1, texturesU, 0);
        checkGlError("glGenTextures U");
        _utid = texturesU[0];
//        DDLog.log("glGenTextures U = " + _utid);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width / 2, height / 2, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (_vtid >= 0) {
//            DDLog.log("glDeleteTextures V");
            GLES20.glDeleteTextures(1, new int[]{_vtid}, 0);
            checkGlError("glDeleteTextures V");
        }
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("glGenTextures V");
        _vtid = textures[0];
//        DDLog.log("glGenTextures V = " + _vtid);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width / 2, height / 2, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }


    public void drawFrame() {
        GLES20.glUseProgram(_program);
        checkGlError("glUseProgram");

        GLES20.glVertexAttribPointer(_positionHandle, 2, GLES20.GL_FLOAT, false, 8, _vertice_buffer);
        checkGlError("glVertexAttribPointer _positionHandle");
        GLES20.glEnableVertexAttribArray(_positionHandle);

        GLES20.glVertexAttribPointer(_coordHandle, 2, GLES20.GL_FLOAT, false, 8, _coord_buffer);
        checkGlError("glVertexAttribPointer _coordHandle");
        GLES20.glEnableVertexAttribArray(_coordHandle);

        GLES20.glActiveTexture(_textureI);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _ytid);
        GLES20.glUniform1i(_yhandle, _tIindex);

        GLES20.glActiveTexture(_textureII);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _utid);
        GLES20.glUniform1i(_uhandle, _tIIindex);

        GLES20.glActiveTexture(_textureIII);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _vtid);
        GLES20.glUniform1i(_vhandle, _tIIIindex);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        if (mType == 1) {
            GLES20.glUseProgram(_programForLine);
            checkGlError("glUseProgramforline");
            GLES20.glVertexAttribPointer(_positionHandleForLie, 2, GLES20.GL_FLOAT, false, 0, _vertice_bufferForLineJGG);
            checkGlError("glVertexAttribPointer _positionHandleforline");
            GLES20.glEnableVertexAttribArray(_positionHandleForLie);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, _vertice_bufferForLineLenght);
        } else if (mType == 2) {
            GLES20.glUseProgram(_programForLine);
            checkGlError("glUseProgramforline");
            GLES20.glVertexAttribPointer(_positionHandleForLie, 2, GLES20.GL_FLOAT, false, 0, _vertice_bufferForLineJGGDJX);
            checkGlError("glVertexAttribPointer _positionHandleforline");
            GLES20.glEnableVertexAttribArray(_positionHandleForLie);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, _vertice_bufferForLineJGGDJXLenght);
        }
        GLES20.glFinish();

        GLES20.glDisableVertexAttribArray(_positionHandle);
        GLES20.glDisableVertexAttribArray(_positionHandleForLie);
        GLES20.glDisableVertexAttribArray(_coordHandle);
    }

    private int createProgram(String vertexSource, String fragmentSource) {

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, pixelShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            DDLog.log(GLES20.glGetProgramInfoLog(program));

            GLES20.glDeleteProgram(program);
            program = 0;
        }

        if (vertexShader > 0) {
            GLES20.glDeleteShader(vertexShader);
        }
        if (pixelShader > 0) {
            GLES20.glDeleteShader(pixelShader);
        }

        return program;
    }

    private int loadShader(int shaderType, String source) {

        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                DDLog.log(GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            DDLog.log("***** " + op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }

    static float[] squareVertices = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,}; // fullscreen

    static float[] squareVertices1 = {-1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f,}; // left-top

    static float[] squareVertices2 = {0.0f, -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,}; // right-bottom

    static float[] squareVertices3 = {-1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,}; // left-bottom

    static float[] squareVertices4 = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,}; // right-top

    private static float[] coordVertices = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,};// whole-texture
    static float verDataJGGDJX[] = {
                -1.0f,-1.0f,// 左下角
                1.0f,1.0f, // 右下角
                -1.0f,1.0f, // 左上角
                1.0f,-1.0f,  // 右上角

                -1.0f,0.33f,// 左下角
                1.0f,0.33f, // 右下角
                -1.0f,-0.33f,// 左下角
                1.0f,-0.33f, // 右下角

                -0.33f,1.0f,
                -0.33f,-1.0f,
                0.33f,1.0f,
                0.33f,-1.0f
    };
    static float verDataJGG[] = {
                -1.0f,0.33f,// 左下角
                1.0f,0.33f, // 右下角
                -1.0f,-0.33f,// 左下角
                1.0f,-0.33f, // 右下角

                -0.33f,1.0f,
                -0.33f,-1.0f,
                0.33f,1.0f,
                0.33f,-1.0f
    };
    private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n" + "attribute vec2 a_texCoord;\n"
            + "varying vec2 tc;\n" + "void main() {\n" + "gl_Position = vPosition;\n" + "tc = a_texCoord;\n" + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n" + "uniform sampler2D tex_y;\n"
            + "uniform sampler2D tex_u;\n" + "uniform sampler2D tex_v;\n" + "varying vec2 tc;\n" + "void main() {\n"
            + "vec4 c = vec4((texture2D(tex_y, tc).r - 16./255.) * 1.164);\n"
            + "vec4 U = vec4(texture2D(tex_u, tc).r - 128./255.);\n"
            + "vec4 V = vec4(texture2D(tex_v, tc).r - 128./255.);\n" + "c += V * vec4(1.596, -0.813, 0, 0);\n"
            + "c += U * vec4(0, -0.392, 2.017, 0);\n" + "c.a = 1.0;\n" + "gl_FragColor = c;\n" + "}\n";
    private static final String WHITE_SHADER = "void main() {\n"
            + "gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);}\n";
}