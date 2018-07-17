package com.liuuuu.chapter06;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer {

    // 旋转角度
    private float rot = 0.0f;

    // 缩放倍数
    private float scale = 0.5f;

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

    // 法线数组
    private float[] normals = new float[]{
            0.000000f, -0.417775f, 0.675974f,
            0.675973f, 0.000000f, 0.417775f,
            0.675973f, -0.000000f, -0.417775f,
            -0.675973f, 0.000000f, -0.417775f,
            -0.675973f, -0.000000f, 0.417775f,
            -0.417775f, 0.675974f, 0.000000f,
            0.417775f, 0.675973f, -0.000000f,
            0.417775f, -0.675974f, 0.000000f,
            -0.417775f, -0.675974f, 0.000000f,
            0.000000f, -0.417775f, -0.675973f,
            0.000000f, 0.417775f, -0.675974f,
            0.000000f, 0.417775f, 0.675973f,

    };


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 告诉系统需要对透视进行修正
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        // 设置清理屏幕颜色
        gl.glClearColor(0, 0, 0, 1);

        // 启用深度缓存
        gl.glEnable(GL10.GL_DEPTH_TEST);

        /* 用户看到的是由 光效 和 材质 共同决定的！二者缺一不可 */
        // 设置光效
        setupLight(gl);

        // 设置材质
        setupMaterial(gl);
    }

    /**
     * 设置材质
     *
     * @param gl
     */
    private void setupMaterial(GL10 gl) {
        /*
        // 环境元素和散射元素颜色
        FloatBuffer ambientAndDiffuse = (FloatBuffer) bufferUtil(new float[]{0.0f, 0.1f, 0.9f, 1.0f});
        // 设置环境元素和散射元素
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, ambientAndDiffuse);
        */

        // 环境元素颜色
        FloatBuffer ambient = (FloatBuffer) bufferUtil(new float[]{0.0f, 0.1f, 0.9f, 1.0f});
        // 设置环境元素
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, ambient);

        // 散射元素颜色
        FloatBuffer diffuse = (FloatBuffer) bufferUtil(new float[]{0.9f, 0.0f, 0.1f, 1.0f});
        // 设置散射元素
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, diffuse);

        // 高光元素颜色
        FloatBuffer specular = (FloatBuffer) bufferUtil(new float[]{0.9f, 0.9f, 0.0f, 1.0f});
        // 设置高光元素
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular);
        // 设置反射度
        gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 25.0f);

        // 自发光颜色
        FloatBuffer emission = (FloatBuffer) bufferUtil(new float[]{0.0f, 0.4f, 0.0f, 1.0f});
        // 设置自发光
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, emission);
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
        gl.glScalef(scale, scale, scale);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 允许设置颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // 允许设置法线数组
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        // 设置法线数组
        gl.glNormalPointer(GL10.GL_FLOAT, 0, bufferUtil(normals));

        // 设置顶点数组
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferUtil(vertices));

        // 设置颜色数组
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, bufferUtil(colors));

        // 绘制
        gl.glDrawElements(GL10.GL_TRIANGLES, 60, GL10.GL_UNSIGNED_BYTE, icosahedronFaces);

        /*
        // 循环多重绘制
        for (int i = 0; i < 30; i++) {
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, -1.5f, -3.0f * i);
            gl.glRotatef(rot, 1.0f, 1.0f, 1.0f);
            gl.glDrawElements(GL10.GL_TRIANGLES, 60, GL10.GL_UNSIGNED_BYTE, icosahedronFaces);
        }
        */

        // 取消颜色数组和顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        // 取消设置法线数组
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

        // 更改旋转角度
        rot += 0.5;
    }


    /**
     * 设置光效
     *
     * @param gl
     */
    public void setupLight(GL10 gl) {

        // 设置平滑阴影模式
        gl.glEnable(GL10.GL_SMOOTH);

        // 设置阴影模式（恒定模式）
//        gl.glShadeModel(GL10.GL_FLAT);


        // 开启光效
        gl.glEnable(GL10.GL_LIGHTING);

        // 开启 0 号光源
        gl.glEnable(GL10.GL_LIGHT0);

        // 环境光颜色
        FloatBuffer light0Ambient = (FloatBuffer) bufferUtil(new float[]{0.1f, 0.1f, 0.1f, 1.0f});
        // 设置环境光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0Ambient);

        // 散射光的颜色
        FloatBuffer light0Diffuse = (FloatBuffer) bufferUtil(new float[]{0.7f, 0.7f, 0.7f, 1.0f});
        // 设置散射光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0Diffuse);

        // 高光的颜色
        FloatBuffer light0Specular = (FloatBuffer) bufferUtil(new float[]{0.9f, 0.9f, 0.0f, 1.0f});
        // 设置高光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0Specular);

        // 光源的位置
        FloatBuffer light0Position = (FloatBuffer) bufferUtil(new float[]{0.0f, 10.0f, 10.0f, 0.0f});
        // 设置光源位置
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0Position);

        // 光线的方向
        FloatBuffer light0Direction = (FloatBuffer) bufferUtil(new float[]{0.0f, 0.0f, -1.0f});
        // 设置光线的方向
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, light0Direction);

        // 设置光线的角度
        gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 45.0f);
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
