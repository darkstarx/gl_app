package com.example.glapplication.app;

import android.opengl.GLES20;
import android.util.Log;
import android.view.Window;

import com.example.glapplication.app.util.Util;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by darkstarx on 23.06.14.
 */
public class MyGLRendererExt extends MyGLRenderer {

    public MyGLRendererExt(Window window)
    {
        super(window);
    }

    @Override
    public void onCreate(int width, int height, boolean is_tablet) {
        GLES20.glClearColor(0.2f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Log.i(Util.LOG_TAG, "renderer: draw frame");
    }

    @Override
    public boolean swap_required() {
        return true;
    }
}
