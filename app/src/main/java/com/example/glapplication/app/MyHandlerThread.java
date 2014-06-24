package com.example.glapplication.app;

/**
 * Created by darkstarx on 24.06.14.
 */
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


public class MyHandlerThread
{
    private HandlerThread m_handlerthread = null;
    private Handler m_handler = null;


    public MyHandlerThread(final String name)
    {
        m_handlerthread = new HandlerThread(name);
        init();
    }


    public MyHandlerThread(final String name, final int priority)
    {
        m_handlerthread = new HandlerThread(name, priority);
        init();
    }


    private void init()
    {
        m_handlerthread.start();
        m_handler = new Handler(m_handlerthread.getLooper());
    }


    public void quit()
    {
        m_handlerthread.quit();
    }


    public Handler handler()
    {
        return m_handler;
    }


    public void post(final Runnable r)
    {
        if (!m_handlerthread.isAlive()) return;
        m_handler.post(r);
    }


    public void postDelayed(final Runnable r, final long delayMillis)
    {
        if (!m_handlerthread.isAlive()) return;
        m_handler.postDelayed(r, delayMillis);
    }


    public void syncpost(final Runnable r)
    {
        if (!m_handlerthread.isAlive()) return;
        final boolean[] synced = { false };
        m_handler.post(new Runnable() { public void run() {
            r.run();
            syncnotify();
            synced[0] = true;
        }});
        if (!synced[0]) syncwait();
    }


    private void syncwait()
    {
        synchronized (this) {
            try {
                wait();
            } catch (final Exception e) {
                Log.e("MyHandlerThread", e.toString());
            }
        }
    }


    private void syncnotify()
    {
        synchronized (this) {
            notify();
        }
    }

}
