package com.liuuuu.chapter08;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.view.KeyEvent;

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

    private Tunnel3D tunnel;
    private boolean created;

    private float centerX = 0.0f;
    private float centerY = 0.0f;

    public GLRender(Context context) {
        mBitmapTexture = BitmapFactory.decodeResource(context.getResources(), R.drawable.img);
        mTexture = new int[1];

        tunnel = new Tunnel3D(10, 20);
        created = false;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 告诉系统需要对透视进行修正
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        // 设置清理屏幕颜色
        gl.glClearColor(0, 0, 0, 1);

        // 启用深度缓存
        gl.glEnable(GL10.GL_DEPTH_TEST);

        initApp(gl);

        /* 用户看到的是由 光效 和 材质 共同决定的！二者缺一不可 */
        // 设置光效
        setupLight(gl);

    }

    private void initApp(GL10 gl) {
        created = true;

        // 启动2D纹理贴图
        gl.glEnable(GL10.GL_TEXTURE_2D);

        // 装载纹理贴图
        loadTexture(gl, mBitmapTexture);
    }

    /**
     * 装载纹理贴图
     *
     * @param gl
     * @param bmp
     */
    private void loadTexture(GL10 gl, Bitmap bmp) {
        ByteBuffer bb = ByteBuffer.allocateDirect(bmp.getHeight() * bmp.getWidth() * 4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();

        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                ib.put(bmp.getPixel(x, y));
            }
        }
        ib.position(0);
        bb.position(0);

        // 创建纹理
        gl.glGenTextures(1, mTexture, 0);

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 加载纹理
        gl.glTexImage2D(
                GL10.GL_TEXTURE_2D, // 此纹理是2D纹理
                0, // 代表图像的详细程度，默认0即可
                GL10.GL_RGBA, // 颜色成分R-红色分量、G-绿色分量、B-蓝色分量三部分，若为4则是R、G、B、A-透明分量
                bmp.getWidth(), // 纹理宽度
                bmp.getHeight(), // 纹理高度
                0, // 边框的值
                GL10.GL_RGBA, // int format 告诉OpenGL图像数据由红绿蓝三色和透明度数据组成
                GL10.GL_UNSIGNED_BYTE, // int type 组成图像数据是无符号字节类型
                bb); // Buffer pixels 告诉OpenGL纹理数据来源

        // 设置线性插值算法
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        float ratio = (float) width / height;

        // 设置窗口（OpenGL场景大小）
        gl.glViewport(0, 0, width, height);

        // 设置投影矩阵为透明投影 注：三种模式 GL_PROJECTION(投影)、GL_MODELVIEW(模型视图)、GL_TEXTURE(纹理)
        gl.glMatrixMode(GL10.GL_PROJECTION);

        // 重置投影矩阵（置为单位矩阵）
        gl.glLoadIdentity();

        // 创建一个对称的透视矩阵 （透视视口带视角的模式）
        GLU.gluPerspective(gl, 45.0f, ratio, 1f, 100f);

        // 创建一个透视投影矩阵（设置视口大小）（透视视口左右边界模式）
//        gl.glFrustumf(-ratio, ratio, -1, 1, 1.0f, 1000.0f);

        // 创建一个正交投影矩阵 （正交视口）
//        gl.glOrthof(-ratio, ratio, -1, 1, 1.0f, 1000.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // 检查是否创建
        boolean c = false;
        synchronized (this) {
            c = created;
        }
        if (!c) return;

        // 首先清理屏幕
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // 设置模型视图矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        // 重置矩阵
        gl.glLoadIdentity();

        // 视点变换
        GLU.gluLookAt(gl, 0, 0, 1, centerX, centerY, 0, 0, 1, 0);

        // 设置渲染模式
        gl.glShadeModel(GL10.GL_SMOOTH);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 允许设置颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        // 允许设置纹理坐标数组
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // 渲染隧道
        tunnel.render(gl,-0.6f);

        // 隧道动画
        tunnel.nextFrame();

        // 禁止设置顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        // 禁止设置颜色数组
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        // 禁止设置纹理坐标数组
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    /**
     * 设置光效
     *
     * @param gl
     */
    public void setupLight(GL10 gl) {

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

        // 高光的位置
        FloatBuffer light0Position = (FloatBuffer) bufferUtil(new float[]{10.0f, 10.0f, 10.0f});
        // 设置高光位置
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0Position);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                centerX -= 0.1f;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                centerX += 0.1f;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                centerY += 0.f;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                centerY += 0.f;
                break;

        }
        return false;
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
