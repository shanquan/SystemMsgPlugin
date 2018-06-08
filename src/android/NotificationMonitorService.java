package com.byd.msgplugin;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;


public class NotificationMonitorService extends NotificationListenerService {

    int seconds = 0;

    // 在收到消息时触发
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String notificationPkg = sbn.getPackageName();
        // 获取接收消息的抬头
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        if (notificationText != null && !"null".equals(notificationText) && notificationPkg != null && !notificationPkg.equals("android") && !"com.android.settings".equals(notificationPkg)) {
            if (seconds == 0 && notificationPkg.startsWith("com.android.mms")) {
                seconds++;
//                AlertDialog.Builder builder = new AlertDialog.Builder(this)
//                        .setTitle("提示")
//                        .setMessage("有消息了:" + notificationPkg + "--" + notificationTitle + "--" + notificationText)
//                        .setPositiveButton("确定", null);
//                AlertDialog dialog = builder.create();
//                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//                dialog.show();
                Intent intent = new Intent("com.byd.action.NotificationAction");
                intent.putExtra("notificationPkg", notificationPkg);  //发送包名
                sendBroadcast(intent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        seconds = 0;
                    }
                }, 2000);
            } else if (!"com.android".startsWith(notificationPkg)) {
                Intent intent = new Intent("com.byd.action.NotificationAction");
                intent.putExtra("notificationPkg", notificationPkg);  //发送包名
                sendBroadcast(intent);
//                AlertDialog.Builder builder = new AlertDialog.Builder(this)
//                        .setTitle("提示")
//                        .setMessage("有消息了:" + notificationPkg + "--" + notificationTitle + "--" + notificationText)
//                        .setPositiveButton("确定", null);
//                AlertDialog dialog = builder.create();
//                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//                dialog.show();
            }
        }
    }

    // 在删除消息时触发
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

}
