package com.example.glapplication.app;

/**
 * Created by darkstarx on 24.06.14.
 */
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.glapplication.app.util.Util;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;


public class EglHelper
{
    public EGL10 egl;
    private EGLDisplay egl_display;
    private EGLSurface egl_surface;
    private EGLConfig egl_config;
    private EGLContext egl_context;

    private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    public void create(int[] configSpec)
    {
        egl = (EGL10) EGLContext.getEGL();
        if (egl == null) Log.e(Util.LOG_TAG, "EGLContext.getEGL() failed");

        egl_display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (egl_display == null) Log.e(Util.LOG_TAG, "EGL.eglGetDisplay() failed");

        int[] version = new int[2];
        egl.eglInitialize(egl_display, version);
        Log.i(Util.LOG_TAG, "OpenGL ES v" + version[0] + "." + version[1]);

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        egl.eglChooseConfig(egl_display, configSpec, configs, 1, num_config);
        egl_config = configs[0];

        final int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
        egl_context = egl.eglCreateContext(egl_display, egl_config, EGL10.EGL_NO_CONTEXT, attrib_list);
        if (egl_context == EGL10.EGL_NO_CONTEXT) Log.e(Util.LOG_TAG, "EGL.eglCreateCOntext() failed");

        egl_surface = null;
    }


    public GL createSurface(SurfaceHolder holder)
    {
        if (egl_surface != null) {
            egl.eglMakeCurrent(egl_display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(egl_display, egl_surface);
        }

        egl_surface = egl.eglCreateWindowSurface(egl_display, egl_config, holder, null);
        if (egl_surface == EGL10.EGL_NO_SURFACE) Log.e(Util.LOG_TAG, "EGL.eglCreateWindowSurface() failed");

        boolean res = egl.eglMakeCurrent(egl_display, egl_surface, egl_surface, egl_context);
        if (!res) Log.e(Util.LOG_TAG, "EGL.eglMakeCurrent() failed");

        GL gl = egl_context.getGL();
        if (gl == null) Log.e(Util.LOG_TAG, "EGLContext.getGL() failed");

        return gl;
    }


    public boolean swap()
    {
        egl.eglSwapBuffers(egl_display, egl_surface);
        boolean ret = egl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
        if (!ret) Log.e(Util.LOG_TAG, "EGL11.EGL_CONTEXT_LOST");
        return ret;
    }


    public void destroy()
    {
        if (egl_surface != null) {
            egl.eglMakeCurrent(egl_display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(egl_display, egl_surface);
            egl_surface = null;
        }
        if (egl_context != null) {
            egl.eglDestroyContext(egl_display, egl_context);
            egl_context = null;
        }
        if (egl_display != null) {
            egl.eglTerminate(egl_display);
            egl_display = null;
        }
    }


    public EGLConfig getEglConfig()
    {
        return egl_config;
    }
}
