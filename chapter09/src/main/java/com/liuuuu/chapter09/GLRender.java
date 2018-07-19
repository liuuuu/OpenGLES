package com.liuuuu.chapter09;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.IBinder;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer {

    private Bitmap mBitmapTexture = null;

    // 材质名称列表
    private int mTexture[];

    // X轴变量
    private int xloop;
    // Y轴变量
    private int yloop;

    private float xrot, yrot, zrot;

    // 雾的颜色设为白色
    private float fogColor[] = {0.5f, 0.5f, 0.5f, 1.0f};

    // 保存盒子的显示列表
    FloatBuffer boxVertices = FloatBuffer.allocate(60); // 5个面*12个点分量
    FloatBuffer boxTexCoords = FloatBuffer.allocate(40); // 5个面*8个向量分量

    // 保存盒子的顶部显示列表
    FloatBuffer topVertices = FloatBuffer.allocate(12);
    FloatBuffer topTexCoords = FloatBuffer.allocate(8);

    float[][] boxcol = {
            {1.0f, 0.0f, 0.0f},
            {1.0f, 0.5f, 0.0f},
            {1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 1.0f}
    };

    float[][] topcol = {
            {0.5f, 0.0f, 0.0f},
            {0.5f, 0.25f, 0.0f},
            {0.5f, 0.5f, 0.0f},
            {0.0f, 0.5f, 0.0f},
            {0.0f, 0.5f, 0.5f}
    };

    public GLRender(Context ctx) {

        mBitmapTexture = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.img);

        mTexture = new int[1];
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // 告诉系统需要对透视进行修正
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

        // 设置清理屏幕颜色
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        // 启用深度缓存
        gl.glEnable(GL10.GL_DEPTH_TEST);

        // 剔除背面（在这里不关心背面）
        gl.glEnable(GL10.GL_CULL_FACE);

        // 启用阴影平滑
        gl.glShadeModel(GL10.GL_SMOOTH);

        // 清理深度缓存
        gl.glClearDepthf(30.0f);

        // 深度测试的类型(深度小或者相等的时候也渲染)
        gl.glDepthFunc(GL10.GL_LEQUAL); // GL_LESS 深度小的时候渲染

        // 载入材质
        loadTexture(gl);

        // 设置光效
        setupLight(gl);

        // 使用颜色材质
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // 宽高比例
        float ratio = (float) width / height;

        // 设置视口（OpenGL场景大小）
        gl.glViewport(0, 0, width, height);

        // 设置矩阵为透视投影
        gl.glMatrixMode(GL10.GL_PROJECTION);

        // 重置投影矩阵
        gl.glLoadIdentity();

        // 创建一个透视投影矩阵（设置视口大小）截锥体一样的视角
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 1000f);


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

        // 绘制列表
        drawList(gl);

        xrot += 0.5f;
        yrot += 0.6f;
        zrot += 0.3f;
    }

    private void loadTexture(GL10 gl) {
        // 开启2D纹理贴图
        gl.glEnable(GL10.GL_TEXTURE_2D);

        IntBuffer intBuffer = IntBuffer.allocate(1);
        // 创建纹理(创建n个纹理，保存在buffer中)
        gl.glGenTextures(1, intBuffer);
        mTexture[0] = intBuffer.get(); // 获取buffer当前位置值传递给mTexture[0]也就是纹理名称

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 生成纹理
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmapTexture, 0);
        // 配置图像
        /*
        GL_NEAREST	                在mip基层上使用最邻近过滤
        GL_LINEAR	                在mip基层上使用线性过滤
        GL_NEAREST_MIPMAP_NEAREST	选择最邻近的mip层，并使用最邻近过滤
        GL_NEAREST_MIPMAP_LINEAR	在mip层之间使用线性插值和最邻近过滤
        GL_LINEAR_MIPMAP_NEAREST	选择最邻近的mip层，使用线性过滤
        GL_LINEAR_MIPMAP_LINEAR	    在mip层之间使用线性插值和使用线性过滤，又称三线性mipmap
         */
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        // 创建显示列表
        BuildLists(gl);
    }

    private void BuildLists(GL10 gl) {
        // 立方体 bottom
        boxTexCoords.put(new float[]{1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f}); // 2 * 4
        boxVertices.put(new float[]{-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f}); // 3 * 4

        // 立方体 front
        boxTexCoords.put(new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f});
        boxVertices.put(new float[]{-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f});

        // 立方体 back
        boxTexCoords.put(new float[]{1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f});
        boxVertices.put(new float[]{-1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f});

        // 立方体 right
        boxTexCoords.put(new float[]{1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f});
        boxVertices.put(new float[]{1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f});

        // 立方体 left
        boxTexCoords.put(new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f});
        boxVertices.put(new float[]{-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f});

        boxTexCoords = bufferNativeOrder(boxTexCoords);
        boxVertices = bufferNativeOrder(boxVertices);

        // 立方体 top
        topTexCoords.put(new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f});
        topVertices.put(new float[]{-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f});

        topTexCoords = bufferNativeOrder(topTexCoords);
        topVertices = bufferNativeOrder(topVertices);



    }

    public void setupLight(GL10 gl) {
        // 开启光效
        gl.glEnable(GL10.GL_LIGHTING);

        // 开启0号光源
        gl.glEnable(GL10.GL_LIGHT0);

        // 环境光颜色
        FloatBuffer light0Ambient = (FloatBuffer) bufferUtil(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
        // 设置环境光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0Ambient);

        // 散射光颜色
        FloatBuffer light0Diffuse = (FloatBuffer) bufferUtil(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        // 设置散射光
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0Diffuse);

        // 高光位置 （x, y, z, w）平行光 w=0，点光源 w=1.0（非0，一般设为1.0）xyz为光源位置
        FloatBuffer light0Position = (FloatBuffer) bufferUtil(new float[]{0.0f, 0.0f, 2.0f, 1.0f});
        // 设置高光位置
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0Position);
    }


    private void drawList(GL10 gl) {
        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);

        // 开启设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // 开启设置纹理贴图坐标数组
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        for (yloop = 1; yloop < 6; yloop++) {
            for (xloop = 0; xloop < yloop; xloop++) {

                // 重置模型视图矩阵
                gl.glLoadIdentity();

                // 设置盒子的位置 1.4f+((float)(xloop)*2.8f)-((float)(yloop)*1.4f),
                gl.glTranslatef(
                        1.4f + ((float) (xloop) * 2.8f) - ((float) (yloop) * 1.4f),
                        (6.0f - (float) (yloop)) * 2.4f,
                        0);

                // 选择X轴 x轴乘1，其他乘0
                gl.glRotatef(45.0f - (2.0f * yloop) + xrot, 1.0f, 0.0f, 0.0f);
                // 选择Y轴 y轴乘1，其他乘0
                gl.glRotatef(45.0f + yrot, 0.0f, 1.0f, 0.0f);

                // 设置颜色
                gl.glColor4f(boxcol[yloop - 1][0], boxcol[yloop - 1][1], boxcol[yloop - 1][2], 1.0f);
                // 设置顶点数组
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, boxVertices);
                // 设置纹理坐标数组
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, boxTexCoords);
                // 绘制（五个面）
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 4);// 点 0-3
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 4, 4);// 点 4-7
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 8, 4);// 点 8-11
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 12, 4);// 点 12-15
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 16, 4);// 点 16-19

                /* 另行设置 立方体 top 面 */
                // 设置单一颜色
                gl.glColor4f(topcol[yloop - 1][0], topcol[yloop - 1][1], topcol[yloop - 1][2], 1.0f);
                // 设置顶点数组
                gl.glVertexPointer(3, GL10.GL_FLOAT,0,topVertices);
                // 设置纹理坐标数组
                gl.glTexCoordPointer(2, GL10.GL_FLOAT,0,topTexCoords);
                // 绘制
                gl.glDrawArrays(GL10.GL_TRIANGLE_FAN,0,4);
            }
        }

        // 禁止设置纹理坐标数组
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        // 禁止设置顶点数组
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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

    private FloatBuffer bufferNativeOrder(FloatBuffer buffer){
        FloatBuffer mBuffer;
        //先初始化buffer,数组的长度*4,因为一个float占4个字节
        float[] arr = buffer.array();
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        //数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());

        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);

        return mBuffer;
    }
}
