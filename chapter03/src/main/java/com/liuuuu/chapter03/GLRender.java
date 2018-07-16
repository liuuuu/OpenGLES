package com.liuuuu.chapter03;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer {

    int one = 0x10000;

    // 用于控制三角形和正方形的旋转角度
    float rotateTri, rotateQuad;

    // 三角形三个顶点
    private int[] triggerBuffer = new int[]{
            0, one, 0, // 上顶点
            -one, -one, 0, // 左下点
            one, -one, 0 // 右下点
    };

    // 正方形的4个顶点
    private int[] quaterBuffer = new int[]{
            one, one, 0, // 右上
            -one, one, 0, // 左上
            one, -one, 0, // 右下
            -one, -one, 0 // 左下
    };

    // 三角形的顶点颜色值（r,g,b,a）
    private int[] colorBuffer = new int[]{
            one, 0, 0, one, // 红色
            0, one, 0, one, // 绿色
            0, 0, one, one // 蓝色
    };

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
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
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

        /* 绘制三角形 */

        // 设置模型位置
        gl.glTranslatef(-3.0f, 0, -4.0f);

        // 设置旋转（Y轴）
        gl.glRotatef(rotateTri, 0.0f, 1.0f, 0.0f);

        // 允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 允许设置颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // 设置颜色数组
        gl.glColorPointer(4, GL10.GL_FIXED, 0, bufferUtil(colorBuffer));

        // 设置三角形顶点数据
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, bufferUtil(triggerBuffer));

        // 放大三角形
        gl.glScalef(2.0f, 2.0f, 2.0f);

        // 绘制三角形
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

        // 关闭颜色数组的设置
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        /* 绘制正方形 */

        // 设置正方形颜色
        gl.glColor4f(0.5f, 0.5f, 1.0f, 1.0f);

        // 重置当前的模型观察矩阵
        gl.glLoadIdentity();

        // 设置模型位置
        gl.glTranslatef(1.0f, 0, -4.0f);

        // 设置旋转（X轴）
        gl.glRotatef(rotateQuad, 1.0f, 0.0f, 0.0f);

        // 设置正方形顶点数组
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, bufferUtil(quaterBuffer));

        // 绘制正方形
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

        /* 绘制线框 */
        // gl.glDrawArrays(GL10.GL_LINES, 0, 4);

        // 关闭设置顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // 改变旋转角度
        rotateTri += 0.5f;
        rotateQuad -= 0.5f;
    }

    /*
     * OpenGL 是一个非常底层的画图接口，它所使用的缓冲区存储结构是和我们的 java 程序中不相同的。
     * Java 是大端字节序(BigEdian)，而 OpenGL 所需要的数据是小端字节序(LittleEdian)。
     * 所以，我们在将 Java 的缓冲区转化为 OpenGL 可用的缓冲区时需要作一些工作。建立buff的方法如下
     * */
    public Buffer bufferUtil(int[] arr) {
        IntBuffer mBuffer;

        //先初始化buffer,数组的长度*4,因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());

        mBuffer = qbb.asIntBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);

        return mBuffer;
    }
}
