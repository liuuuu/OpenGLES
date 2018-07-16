package com.liu.chapter04;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer {

    private float rot = 0.0f;

    // 顶点数组
    private float[] vertices = new float[]{
            0.f, -0.525731f, 0.850651f,
            0.850651f, 0.f, 0.525731f,
            0.850651f, 0.f, -0.525731f,
            -0.850651f, 0.f, -0.525731f,
            -0.850651f, 0.f, 0.525731f,
            -0.525731f, 0.850651f, 0.f,
            0.525731f, 0.850651f, 0.f,
            0.525731f, -0.850651f, 0.f,
            -0.525731f, -0.850651f, 0.f,
            0.f, -0.525731f, -0.850651f,
            0.f, 0.525731f, -0.850651f,
            0.f, 0.525731f, 0.850651f,
    };

    // 颜色数组
    private float[] colors = new float[]{
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.5f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.5f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.5f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 0.5f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.5f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.5f, 1.0f,

    };

    // 索引数组
    private ByteBuffer icosahedronFaces = ByteBuffer.wrap(new byte[]{
            1, 2, 6,
            1, 7, 2,
            3, 4, 5,
            4, 3, 8,
            6, 5, 11,
            5, 6, 10,
            9, 10, 2,
            10, 9, 3,
            7, 8, 9,
            8, 7, 0,
            11, 0, 1,
            0, 11, 4,
            6, 2, 10,
            1, 6, 11,
            3, 5, 10,
            5, 4, 11,
            2, 7, 9,
            7, 1, 0,
            3, 9, 8,
            4, 8, 0
    });


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 告诉系统需要对透视进行修正
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        // 设置清理屏幕颜色
        gl.glClearColor(0, 0, 0, 1);

        // 启用深度缓存
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float) width / height;

        // 设置窗口（OpenGL场景大小）
        gl.glViewport(0, 0, width, height);

        // 设置投影矩阵为透明投影
        gl.glMatrixMode(GL10.GL_PROJECTION);

        // 重置投影矩阵（置为单位矩阵）
        gl.glLoadIdentity();

        // 创建一个透视投影矩阵（设置视口大小）
//        gl.glFrustumf(-ratio, ratio, -1, 1, 1.0f, 1000.0f);

        // 创建一个正交投影矩阵
        gl.glOrthof(-ratio, ratio, -1, 1, 1.0f, 1000.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 首先清理屏幕
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // 设置模型视图矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        // 重置矩阵
        gl.glLoadIdentity();

        // 视点变换
        GLU.gluLookAt(gl, 0, 0, 3, 0, 0, 0, 0, 1, 0);

        // 平移操作
        gl.glTranslatef(0.0f, 0.0f, -3.0f);

        // 旋转操作
        gl.glRotatef(rot, 1.0f, 1.0f, 1.0f);

        // 缩放操作
        gl.glScalef(3.0f, 3.0f, 3.0f);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 设置顶点数组
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferUtil(vertices));

        // 允许设置颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // 设置颜色数组
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, bufferUtil(colors));

        // 绘制
//        gl.glDrawElements(GL10.GL_TRIANGLES, 60, GL10.GL_UNSIGNED_BYTE, icosahedronFaces);

        // 循环多重绘制
        for (int i = 0; i < 30; i++) {
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, -1.5f, -3.0f * i);
            gl.glRotatef(rot, 1.0f, 1.0f, 1.0f);
            gl.glDrawElements(GL10.GL_TRIANGLES, 60, GL10.GL_UNSIGNED_BYTE, icosahedronFaces);
        }

        // 取消颜色数组和顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        // 更改旋转角度
        rot += 0.5;
    }


    /*
     * OpenGL 是一个非常底层的画图接口，它所使用的缓冲区存储结构是和我们的 java 程序中不相同的。
     * Java 是大端字节序(BigEdian)，而 OpenGL 所需要的数据是小端字节序(LittleEdian)。
     * 所以，我们在将 Java 的缓冲区转化为 OpenGL 可用的缓冲区时需要作一些工作。建立buff的方法如下
     */
    public Buffer bufferUtil(float[] arr) {
        FloatBuffer mBuffer;

        //先初始化buffer,数组的长度*4,因为一个float占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());

        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);

        return mBuffer;
    }
}
