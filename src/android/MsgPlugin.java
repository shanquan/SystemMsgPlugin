package com.byd.msgplugin;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by yang.yu11 on 2017/10/23.
 */

public class MsgPlugin extends CordovaPlugin {

    private CallbackContext msmCallback = null;
    private CallbackContext notificationCallback = null;
    private CallbackContext phoneCallback = null;
    private CallbackContext selectListCallback = null;

    boolean msmFlag, notificationFlag, phoneFlag;//短信、来电、通知配置参数
    boolean isAns = true;
    String selectPackages;
    boolean isJump = false;

    int second = 0;
    int offSecond = 0;
    private MsmReceiver msmReceiver;
    private NotificationReceiver notificationReceiver;
    private CallReceiver callReceiver;
    private SelectAppReceiver selectAppReceiver;

    public static final String[] permissions = {Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE};
    private ExecutorService executorService;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("checkState".equals(action)) { //检测配置参数
            msmFlag = args.getBoolean(0);
            phoneFlag = args.getBoolean(1);

            if (msmFlag) {
                msmCallback = callbackContext;
            }
//            if (notificationFlag) {
//                notificationCallback = callbackContext;
//            }
            if (phoneFlag) {
                phoneCallback = callbackContext;
            }
            //先开守护服务
//          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            openJobService();
//          else
//            Toast.makeText(getContext(), "进入", Toast.LENGTH_SHORT).show();
            openTwoService();
            doJudge();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

            return true;
        } else if ("listenMsm".equals(action)) { //监听短信
            msmFlag = true;
            msmCallback = callbackContext;
            judgeMsm();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            msmCallback.sendPluginResult(pluginResult);
            return true;
        } else if ("listNotification".equals(action)) {//监听通知
            selectPackages = args.getString(0);
            notificationCallback = callbackContext;
            if (!isEnabled()) { //判断是否有监听通知权限，没有则打开设置
                new AlertDialog.Builder(getContext())
                        .setTitle("提示")
                        .setMessage("您的应用还没有开启通知监听权限，点击确认进入设置开启权限")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isJump = true;
                                getContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                notificationFlag = true;
                judgeNotification();
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            notificationCallback.sendPluginResult(pluginResult);
            return true;
        } else if ("listenCall".equals(action)) { //监听来电
            phoneFlag = true;
            if (phoneCallback == null)
                phoneCallback = callbackContext;
            judgePhone();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            phoneCallback.sendPluginResult(pluginResult);
            return true;
        } else if ("cancelListenMsg".equals(action)) { //取消监听短信
            msmFlag = false;
            callbackContext.success();
            judgeMsm();
            return true;
        } else if ("cancelNotification".equals(action)) { //取消监听通知
            notificationFlag = false;
            callbackContext.success();
            judgeNotification();
            return true;
        } else if ("cancelListenCall".equals(action)) { //取消监听来电
            phoneFlag = false;
            callbackContext.success();
            judgePhone();
            return true;
        } else if ("jumpToNotifiActivity".equals(action)) {
            selectListCallback = callbackContext;

            selectPackages = args.getString(0);
            notificationFlag = args.getBoolean(1);

//            if (notificationFlag && notificationCallback == null)
//                notificationCallback = callbackContext;

            Intent intent = new Intent(getContext(), NotificationActivity.class);
            intent.putExtra("selectPackages", selectPackages);
            intent.putExtra("notificationFlag", notificationFlag);
            cordova.startActivityForResult(this, intent, 0);

            if (selectAppReceiver == null) {
                IntentFilter filter = new IntentFilter("com.byd.action.SelectAppAction");
                selectAppReceiver = new SelectAppReceiver();
                getContext().registerReceiver(selectAppReceiver, filter);
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }

    private boolean hasPermissions() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    private void doJudge() {
        judgeMsm();
//        judgeNotification();
        judgePhone();
    }

    private void judgePhone() {
        if (phoneFlag && callReceiver == null) { //来电
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            filter.setPriority(1000);
            callReceiver = new CallReceiver();
            getContext().registerReceiver(callReceiver, filter);
        } else if (!phoneFlag && callReceiver != null) {
            getContext().unregisterReceiver(callReceiver);
            callReceiver = null;
            phoneCallback = null;
        }
    }

    private void judgeNotification() {
        if (notificationFlag && notificationReceiver == null) { //通知
            IntentFilter filter = new IntentFilter("com.byd.action.NotificationAction");
            notificationReceiver = new NotificationReceiver();
            getContext().registerReceiver(notificationReceiver, filter);

//            PluginResult result = new PluginResult(PluginResult.Status.OK, "setNotificationOk");
//            result.setKeepCallback(true);
//            notificationCallback.sendPluginResult(result);
        } else if (!notificationFlag && notificationReceiver != null) {
            getContext().unregisterReceiver(notificationReceiver);
            notificationReceiver = null;
            notificationCallback = null;
        }
    }

    private void judgeMsm() {
        if (msmFlag && msmReceiver == null) { //短信
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.addAction("android.provider.Telephony.SMS_DELIVER");
            intentFilter.setPriority(1000);
            msmReceiver = new MsmReceiver();
            getContext().registerReceiver(msmReceiver, intentFilter);
        } else if (!msmFlag && msmReceiver != null) {
            getContext().unregisterReceiver(msmReceiver);
            msmReceiver = null;
            msmCallback = null;
        }
    }

    /**
     * 判断应用是否有监听通知权限
     *
     * @return
     */
    private boolean isEnabled() {
        String pkgName = getContext().getPackageName();
        final String flat = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void openTwoService() {
        if (!isServiceWorking(MsgFirstService.class.getName())) {
            Intent intent = new Intent(getContext(), MsgFirstService.class);
            getContext().startService(intent);
            getContext().startService(new Intent(getContext(), MsgSecondService.class));
        }
    }

    /**
     * 判断服务有没有在运行
     *
     * @param serviceName
     * @return
     */
    private boolean isServiceWorking(String serviceName) {
        boolean isWorking = false;
        ActivityManager myAm = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAm.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWorking = true;
                break;
            }
        }
        return isWorking;
    }

//    private void openJobService() {
//        Intent intent = new Intent();
//        intent.setClass(getContext(), JobHandlerService.class);
//        getContext().startService(intent);
//    }

    private Context getContext() {
        return webView.getContext();
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (isJump) {
            isJump = false;
            if (isEnabled()) {
                notificationFlag = true;
                judgeNotification();
            }
        }
    }

    class MsmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (msmCallback != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "msm");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "message");
                    result.setKeepCallback(true);
                    msmCallback.sendPluginResult(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String notificationPkg = intent.getStringExtra("notificationPkg");
            if (notificationCallback != null && notificationPkg != null) {
                if (selectPackages.contains(notificationPkg)) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type", "notification");
                        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                        result.setKeepCallback(true);
                        notificationCallback.sendPluginResult(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    class CallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                TelephonyManager manager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            if (state == TelephonyManager.CALL_STATE_RINGING) { //来电
                if (second == 0) {
                    second++;
                    if (phoneCallback != null) {
                        isAns = false;
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "oncall");
                            jsonObject.put("incomingnumber", incomingNumber);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                            result.setKeepCallback(true);
                            phoneCallback.sendPluginResult(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            second = 0;
                        }
                    }, 2000);
                }

            } else if (state == TelephonyManager.CALL_STATE_IDLE) { //挂电话
                if (offSecond == 0) {
                    offSecond++;
                    if (phoneCallback != null && !isAns) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "offcall");
                            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                            result.setKeepCallback(true);
                            phoneCallback.sendPluginResult(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            offSecond = 0;
                        }
                    }, 2000);
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) { //接通电话
                if (offSecond == 0) {
                    offSecond++;
                    if (phoneCallback != null) {
                        isAns = true;
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("type", "offcall");
                            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                            result.setKeepCallback(true);
                            phoneCallback.sendPluginResult(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            offSecond = 0;
                        }
                    }, 2000);
                }
            }
        }
    };

    class SelectAppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.byd.action.SelectAppAction".equals(intent.getAction()) && selectListCallback != null) {
                selectPackages = intent.getStringExtra("selectPackages");
                notificationFlag = intent.getBooleanExtra("notificationFlag", false);
                if (notificationFlag) {
                    if (notificationCallback == null)
                        notificationCallback = selectListCallback;
                }

                judgeNotification();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("type", "setting");
                    jsonObject.put("notificationFlag", notificationFlag);
                    jsonObject.put("selectPackages", selectPackages);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
                    result.setKeepCallback(true);
                    selectListCallback.sendPluginResult(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        msmCallback = null;
        notificationCallback = null;
        phoneCallback = null;
        selectListCallback = null;

        if (msmReceiver != null) {
            getContext().unregisterReceiver(msmReceiver);
            msmReceiver = null;
        }
        if (notificationReceiver != null) {
            getContext().unregisterReceiver(notificationReceiver);
            notificationReceiver = null;
        }
        if (callReceiver != null) {
            getContext().unregisterReceiver(callReceiver);
            callReceiver = null;
        }
        if (selectAppReceiver != null) {
            getContext().unregisterReceiver(selectAppReceiver);
            selectAppReceiver = null;
        }
        super.onDestroy();
    }
}
