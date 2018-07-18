package com.liuuuu.chapter07;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer {

    private Bitmap mBitmapTexture = null;

    int mTexture[];

    // 旋转角度
    private float rot = 0.0f;

    // 缩放倍数
    private float scale = 3.0f;

    public GLRender(Context context) {
        mBitmapTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.texture);
        mTexture = new int[1];
    }

    // 正方形的顶点数组
    private float[] verticesSquare = new float[]{
            -1.0f, 1.0f, -0.0f,
            1.0f, 1.0f, -0.0f,
            -1.0f, -1.0f, -0.0f,
            1.0f, -1.0f, -0.0f
    };

    // 正方形法线数组
    private float[] normalsSquare = new float[]{
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    // 正方形的贴图数组
//    private float[] texCoordsSquare = new float[]{
//            0.0f, -1.0f,
//            1.0f, -1.0f,
//            0.0f, 0.0f,
//            1.0f, 0.0f
//    };

//    private float[] texCoordsSquare = new float[]{
//            0.0f, -0.5f,
//            0.5f, -0.5f,
//            0.0f, 0.0f,
//            0.5f, 0.0f
//    };

//    private float[] texCoordsSquare = new float[]{
//            0.25f, -0.75f,
//            0.75f, -0.75f,
//            0.25f, -0.25f,
//            0.75f, -0.25f
//    };

    private float[] texCoordsSquare = new float[]{
            0.0f, 0.0f,
            2.0f, 0.0f,
            0.0f, 2.0f,
            2.0f, 2.0f
    };


    // 三角形的顶点数组
    private float[] verticesTriangle = new float[]{
            -1.0f, 1.0f, -0.0f,
            1.0f, 1.0f, -0.0f,
            0.0f, -1.0f, -0.0f,
    };

    // 三角形法线数组
    private float[] normalsTriangle = new float[]{
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f
    };

    // 三角形贴图数组
    private float[] texCoordsTriangle = new float[]{
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
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
//        setupMaterial(gl);

        // 设置纹理
        setupTexture(gl);
    }

    /**
     * 设置纹理
     *
     * @param gl
     */
    private void setupTexture(GL10 gl) {

        // 打开2D贴图
        gl.glEnable(GL10.GL_TEXTURE_2D);

        // 打开混色功能
        gl.glEnable(GL10.GL_BLEND);

        // 指定混色方法
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR);

        IntBuffer intBuffer = IntBuffer.allocate(1);
        // 创建纹理
        gl.glGenTextures(1, intBuffer);
        mTexture[0] = intBuffer.get();

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 当纹理需要被放大和缩小时都使用线性插值方法调整图像
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);

        // 设置重复效果（需要纹理坐标大于1.0）
//        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
//        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        // 限制拉伸边缘
        gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);

        // 生成纹理（加载图像）
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmapTexture, 0);
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
        gl.glFrustumf(-ratio, ratio, -1, 1, 1.0f, 1000.0f);

        // 创建一个正交投影矩阵
//        gl.glOrthof(-ratio, ratio, -1, 1, 1.0f, 1000.0f);
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

        // 绘制正方形
        drawSquare(gl);

        // 绘制三角形
//        drawTriangle(gl);
    }

    private void drawTriangle(GL10 gl) {
        // 平移操作
        gl.glTranslatef(0.0f, 0.0f, -3.0f);

        // 旋转操作
        gl.glRotatef(rot, 1.0f, 1.0f, 1.0f);

        // 缩放操作
        gl.glScalef(scale, scale, scale);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 允许设置法线数组
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        // 允许设置纹理坐标数组
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 设置顶点数组
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferUtil(verticesTriangle));

        // 设置法线数组
        gl.glNormalPointer(GL10.GL_FLOAT, 0, bufferUtil(normalsTriangle));

        // 设置纹理坐标数组
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, bufferUtil(texCoordsTriangle));

        // 绘制三角形
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

        // 关闭顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // 关闭法线数组
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);

        // 关闭纹理坐标数组
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    private void drawSquare(GL10 gl) {
        // 平移操作
        gl.glTranslatef(0.0f, 0.0f, -3.0f);

        // 旋转操作
        gl.glRotatef(rot, 1.0f, 1.0f, 1.0f);

        // 缩放操作
        gl.glScalef(scale, scale, scale);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 允许设置法线数组
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        // 允许设置纹理坐标数组
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 设置顶点数组
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bufferUtil(verticesSquare));

        // 设置法线数组
        gl.glNormalPointer(GL10.GL_FLOAT, 0, bufferUtil(normalsSquare));

        // 设置纹理坐标数组
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, bufferUtil(texCoordsSquare));

        // 绘制正方形
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

        // 关闭顶点数组、法线数组、纹理坐标数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
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
        FloatBuffer light0Ambient = (FloatBuffer) bufferUtil(new float[]{0.4f, 0.4f, 0.4f, 1.0f});
        // 设置环境光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0Ambient);

        // 散射光的颜色
        FloatBuffer light0Diffuse = (FloatBuffer) bufferUtil(new float[]{0.8f, 0.8f, 0.8f, 1.0f});
        // 设置散射光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0Diffuse);

        // 高光的颜色
        FloatBuffer light0Specular = (FloatBuffer) bufferUtil(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        // 设置高光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0Specular);

        /*
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
        */
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
