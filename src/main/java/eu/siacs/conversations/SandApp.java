package eu.siacs.conversations;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;

import eu.siacs.conversations.ui.PinActivity;

public class SandApp extends Application implements LifecycleObserver {

    public Activity resumeActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onAppForegrounded() {
        // app in foreground
        if (resumeActivity != null)
            if (resumeActivity instanceof ILockedActivity) {
                Intent intent = new Intent(getApplicationContext(), PinActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
    }

}