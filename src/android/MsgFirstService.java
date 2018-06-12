package com.byd.msgplugin;


import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.byd.msgplugin.aidl.ProcessService;

/**
 * Created by yang.yu11 on 2017/10/23.
 */

public class MsgFirstService extends Service {

    private MyBinder binder;
    private MyConn conn;


    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        if (conn == null)
            conn = new MyConn();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder = new Notification.Builder(this);
//      builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setSmallIcon(MResource.getIdByName(this, "drawable", "ic_launcher"));
        startForeground(250, builder.build());
        startService(new Intent(this, MsgThirdService.class));
        MsgFirstService.this.bindService(new Intent(this, MsgSecondService.class), conn, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    class MyBinder extends ProcessService.Stub {

        @Override
        public String getServiceName() throws RemoteException {
            return "I am FirstService";
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("info", "与SecondService连接成功");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MsgFirstService.this.startActivity(new Intent(MsgFirstService.this, MsgSecondService.class));
            MsgFirstService.this.bindService(new Intent(MsgFirstService.this, MsgSecondService.class), conn, Context.BIND_IMPORTANT);
        }
    }

}
