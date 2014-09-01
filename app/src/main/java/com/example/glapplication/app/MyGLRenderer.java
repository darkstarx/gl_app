package com.example.glapplication.app;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Window;

import com.example.glapplication.app.util.Util;


public abstract class MyGLRenderer implements MyGLSurfaceView.Renderer
{
    private Window m_window;


    public MyGLRenderer(Window window)
    {
        super();
        m_window = window;
    }


    public int[] getConfigSpec()
    {
        final int EGL_OPENGL_ES2_BIT = 4;
        int[] configSpec = {
//            EGL10.EGL_RED_SIZE, 8,
//            EGL10.EGL_GREEN_SIZE, 8,
//            EGL10.EGL_BLUE_SIZE, 8,
//            EGL10.EGL_ALPHA_SIZE, 0,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        };
        return configSpec;
    }


    public void onSurfaceCreated(GL10 gl10, EGLConfig eglc)
    {
        Log.i(Util.LOG_TAG, "Surface created");
    }


    public void onSurfaceChanged(GL10 gl10, int width, int height)
    {
        boolean is_tablet = false;
        final MyActivity context = MyActivity.getInstance();
        if (context!=null) {
            is_tablet = (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        }
        String msg = "Surface changed. " + String.valueOf(width) + "x" + String.valueOf(height) + (is_tablet ? " tablet" : " phone");
        Log.i(Util.LOG_TAG, msg);
        onCreate(width, height, is_tablet);
    }


    public void onSurfaceDestroyed()
    {
        Log.i(Util.LOG_TAG, "Surface destroyed.");
    }


    public abstract void onCreate(int width, int height, boolean is_tablet);
    public abstract void onDrawFrame(GL10 gl10);
    public abstract boolean swap_required();
}
