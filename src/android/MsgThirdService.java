package com.byd.msgplugin;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.List;
import com.byd.ghydy.bracelet.R;

/**
 * Created by yang.yu11 on 2017/10/23.
 */

public class MsgThirdService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, NotificationMonitorService.class);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null) {
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                if (service.pid == Process.myPid()) {
                    collectorRunning = true;
                }
            }
        }

        if (collectorRunning) {
            return;
        }
        toggleNotificationListenerService();
    }

    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(this, NotificationMonitorService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensureCollectorRunning();
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        startForeground(250, builder.build());
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                stopForeground(true);
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(250);
                stopSelf();
            }
        }).start();
        return START_STICKY;
    }
}
