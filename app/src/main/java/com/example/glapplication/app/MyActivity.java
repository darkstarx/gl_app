package com.example.glapplication.app;

import com.example.glapplication.app.util.Util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MyActivity extends Activity {

    private class State
    {
        public boolean running = false;
        public boolean tick_disabled = true;
    };

    private MyGLSurfaceView mGLView;
    private MyHandlerThread m_ht;
    private long m_tickTime;
    private final State m_state = new State();

    private static MyActivity m_instance = null;


    private final Runnable f_tick = new Runnable() { public void run() {
        tick();
    }};


    public MyActivity()
    {
        super();
        m_ht = new MyHandlerThread("MyHandlerThread_" + this);
    }

    private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }

    public MyHandlerThread ht()
    {
        return m_ht;
    }

    public static MyActivity getInstance()
    {
        return m_instance;
    }

    protected void tick()
    {
        final long ntickTime = SystemClock.elapsedRealtime();
        final long delay = MyGLSurfaceView.GL_TASK_INTERVAL - (ntickTime - m_tickTime);
        m_tickTime = ntickTime;

        if (!m_state.tick_disabled) {
            if (delay <= 0) m_ht.post(f_tick);
            else m_ht.postDelayed(f_tick, delay);
        }
    }

    private void enableTick()
    {
        synchronized (m_state) {
            if (!m_state.running || !m_state.tick_disabled) return;
            m_state.tick_disabled = false;
        }
        m_tickTime = SystemClock.elapsedRealtime();
        m_ht.post(f_tick);
    }

    private void disableTick()
    {
        synchronized (m_state) {
            if (m_state.tick_disabled) return;
            m_state.tick_disabled = true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_instance = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (hasGLES20()) {
            mGLView = new MyGLSurfaceView(this);
            mGLView.setKeepScreenOn(true);
            mGLView.setRenderer(new MyGLRendererExt(getWindow()));
        } else {
            // Time to get a new phone, OpenGL ES 2.0 not supported.
        }

        setContentView(mGLView);
        Log.i(Util.LOG_TAG, "create");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mGLView.setVisibility(View.INVISIBLE);
        mGLView = null;
        m_ht.quit();
        m_instance = null;
        Log.i(Util.LOG_TAG, "destroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_ht.post(new Runnable() { public void run() {
            synchronized (m_state) {
                m_state.running = true;
            }
            enableTick();
        }});
        Log.i(Util.LOG_TAG, "start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLView != null) {
            mGLView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLView != null) {
            mGLView.onPause();
        }
    }

    @Override
    protected void onStop() {
        synchronized (m_state) {
            m_state.running = false;
        }
        m_ht.syncpost(new Runnable() { public void run() {
            disableTick();
        }});
        super.onStop();
        Log.i(Util.LOG_TAG, "stop");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                Log.i(Util.LOG_TAG, "Showing exit dialog");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.exit_text);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(Util.LOG_TAG, "User select to exit. Finish application");
                        MyActivity.this.finish();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                Dialog exit_dialog = builder.create();
                exit_dialog.setOwnerActivity(this);
                exit_dialog.show();

                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
