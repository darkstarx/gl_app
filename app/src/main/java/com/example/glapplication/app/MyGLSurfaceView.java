package com.example.glapplication.app;

import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.opengl.GLSurfaceView;

import com.example.glapplication.app.util.Util;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by darkstarx on 24.06.14.
 */
public class MyGLSurfaceView extends SurfaceView  implements SurfaceHolder.Callback {

    public interface Renderer extends GLSurfaceView.Renderer
    {
        public boolean swap_required();
        public int[] getConfigSpec();
        public void onSurfaceDestroyed();

//        public void onStart();
//        public void onStop();
    }


    private class State
    {
        public boolean gl_ready = false;
        public boolean running = false;
        public boolean drawframe_disabled = true;

        public boolean ready()
        {
            return gl_ready && running;
        }
    };


    public static final long GL_TASK_INTERVAL = 16; /* ms */
    private MyHandlerThread m_ht = null;
    private Renderer m_renderer = null;
    private EglHelper m_egl = null;
    private GL10 m_gl = null;
    private final State m_state = new State();
    private Runnable m_on_init = null;
    private long m_drawFrameTime = 0;

    private final Runnable f_drawFrame = new Runnable() { public void run() {
        drawFrame();
    }};


    public MyGLSurfaceView(MyActivity activity)
    {
        super(activity);
        m_ht = activity.ht();

        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }


    public final void setRenderer(Renderer renderer)
    {
        m_renderer = renderer;
        m_ht.syncpost(new Runnable() { public void run() {
            m_egl = new EglHelper();
        }});
    }


    @Override
    public final void surfaceCreated(final SurfaceHolder holder)
    {
        //Вызываем колбэк инициализации - из главного потока.
        callOnInit();

        m_ht.post(new Runnable() { public void run() {
            if (!holder.getSurface().isValid()) {
                Log.e(Util.LOG_TAG, "Ошибка: поверхность невалидна! Не создаём рендерер.");
                return;
            }
            m_egl.create(m_renderer.getConfigSpec());
            m_gl = (GL10)m_egl.createSurface(holder);
            m_renderer.onSurfaceCreated(m_gl, m_egl.getEglConfig());
            synchronized (m_state) {
                m_state.gl_ready = true;
            }
            enableDrawFrame();
        }});
    }


    @Override
    public final void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height)
    {
        m_ht.post(new Runnable() { public void run() {
            if (m_gl!=null) {
                m_renderer.onSurfaceChanged(m_gl, width, height);
            }
        }});
    }


    @Override
    public final void surfaceDestroyed(final SurfaceHolder holder)
    {
        m_renderer.onSurfaceDestroyed();
        synchronized (m_state) {
            m_state.gl_ready = false;
        }
        m_ht.syncpost(new Runnable() { public void run() {
            if (m_gl != null) {
                m_gl = null;
                m_egl.destroy();
            }
        }});
    }


    private void drawFrame()
    {
        // Уже выполняется в m_ht.HandlerThread

        synchronized (m_state) {
            if (!m_state.ready()) {
                m_state.drawframe_disabled = true;
                return;
            }
        }

        m_renderer.onDrawFrame(m_gl);
        if (m_renderer.swap_required()) m_egl.swap();

        final long ndrawFrameTime = SystemClock.elapsedRealtime();
        final long delay = GL_TASK_INTERVAL - (ndrawFrameTime - m_drawFrameTime);
        m_drawFrameTime = ndrawFrameTime;

        if (delay <= 0) m_ht.post(f_drawFrame);
        else m_ht.postDelayed(f_drawFrame, delay);
    }


    public void onPause()
    {
        synchronized (m_state) {
            m_state.running = false;
        }
        Log.i(Util.LOG_TAG, "pause");
    }


    public void onResume()
    {
        synchronized (m_state) {
            m_state.running = true;
        }
        Log.i(Util.LOG_TAG, "resume");
        enableDrawFrame();
    }

    /**
     * Переданный сюда колбэк вызывается из главного потока при создании поверхности (surface), что соответствует созданию опенгл контекста.
     */
    public void setOnInit(final Runnable on_init)
    {
        m_on_init = on_init;
    }

    private void callOnInit()
    {
        if (m_on_init == null) return;
        m_on_init.run();
    }

    @Override
    final public boolean onTouchEvent(final MotionEvent event)
    {
//        if (event.getActionIndex() == 0) {
//            final int action = event.getAction();
//            final float x = event.getX(0);
//            final float y = event.getY(0);
//            m_ht.post(new Runnable() { public void run() {
//                synchronized (m_state) {
//                    if (!m_state.ready()) return;
//                    nativeOnTouchEvent(action, x, y);
//                }
//            }});
//        }

        return true;
    }


    @Override
    final public boolean onKeyDown(final int keyCode, final KeyEvent event)
    {
        final MyActivity activity = MyActivity.getInstance();
        if (activity == null ) {
            return false;
        }

        final boolean[] ret = { false };
        m_ht.syncpost(new Runnable() { public void run() {
            if (!m_state.ready()) return;
            ret[0] = KeyDown(keyCode);
        }});
        return ret[0];
    }


    public boolean KeyDown(final int keyCode)
    {
        Log.i(Util.LOG_TAG, String.valueOf(keyCode));
        return false;
    }


    private void enableDrawFrame()
    {
        synchronized (m_state) {
            if (!m_state.ready() || !m_state.drawframe_disabled) return;
            m_state.drawframe_disabled = false;
        }
        m_drawFrameTime = SystemClock.elapsedRealtime();
        m_ht.post(f_drawFrame);
    }

}
