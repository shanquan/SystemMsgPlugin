package com.byd.msgplugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private Switch swi;
    private Button btnManage;
    private ListView lv;
    private SelectAppAdapter adapter;
    private String selectPackages;
    private boolean notificationFlag;
    boolean isJump = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(this, "layout", "activity_notification"));
        initViews();
        initDatas();
    }

    private void initDatas() {
        Intent intent = getIntent();
        if (intent != null) {
            selectPackages = intent.getStringExtra("selectPackages");
            notificationFlag = intent.getBooleanExtra("notificationFlag", false);
        }
        //设置选择框
        if (notificationFlag) {
            swi.setChecked(true);
        } else {
            swi.setChecked(false);
        }

        setListData();
    }

    private void setListData() {
        List<AppInfo> appList = new ArrayList<AppInfo>();
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
                if (selectPackages.contains(packageInfo.packageName)) {
                    AppInfo info = new AppInfo();
                    info.pkgName = packageInfo.packageName;
                    info.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                    info.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                    appList.add(info);
                }
            }
        }
        adapter = new SelectAppAdapter(this, appList);
        lv.setAdapter(adapter);
    }

    private void initViews() {
        swi = (Switch) findViewById(MResource.getIdByName(this, "id", "swi"));
        swi.setOnCheckedChangeListener(this);
        lv = (ListView) findViewById(MResource.getIdByName(this, "id", "lv"));
        btnManage = (Button) findViewById(MResource.getIdByName(this, "id", "btnManage"));
        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(NotificationActivity.this, InstalledAppActivity.class);
                    intent.putExtra("selectPackages", selectPackages);
                    startActivityForResult(intent, 22);
                } catch (Exception e) {
                    Toast.makeText(NotificationActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void back(View view) {
        Intent intent = new Intent("com.byd.action.SelectAppAction");
        intent.putExtra("notificationFlag", notificationFlag);
        intent.putExtra("selectPackages", selectPackages);
        sendBroadcast(intent);

        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            if (!isEnabled()) { //判断是否有监听通知权限，没有则打开设置
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("您的应用还没有开启通知监听权限，点击确认进入设置开启权限")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isJump = true;
                                NotificationActivity.this.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                notificationFlag = true;
//                sendNotificationSettings();
            }
        } else {
            notificationFlag = false;
//            sendNotificationSettings();
        }
    }

    class AppInfo {  //应用信息实体
        private String appName;
        private String pkgName;
        private Drawable appIcon;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getPkgName() {
            return pkgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public Drawable getAppIcon() {
            return appIcon;
        }

        public void setAppIcon(Drawable appIcon) {
            this.appIcon = appIcon;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22 && resultCode == 23) {
            selectPackages = data.getStringExtra("selectPackages");
            setListData();

//            Intent intent = new Intent("com.byd.action.SelectAppAction");
//            intent.putExtra("notificationFlag", notificationFlag);
//            intent.putExtra("selectPackages", selectPackages);
//            sendBroadcast(intent);
        }
    }

    /**
     * 判断应用是否有监听通知权限
     *
     * @return
     */
    private boolean isEnabled() {
        String pkgName = this.getPackageName();
        final String flat = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
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

    @Override
    protected void onResume() {
        super.onResume();

        if (isJump) {
            isJump = false;
            if (isEnabled()) {
                notificationFlag = true;
//                sendNotificationSettings();
            }
        }
    }

    /**
     * 发送消息监听设置
     */
//    private void sendNotificationSettings() {
//        Intent intent = new Intent("com.byd.action.SelectAppAction");
//        intent.putExtra("notificationFlag", notificationFlag);
//        intent.putExtra("selectPackages", selectPackages);
//        sendBroadcast(intent);
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent("com.byd.action.SelectAppAction");
        intent.putExtra("notificationFlag", notificationFlag);
        intent.putExtra("selectPackages", selectPackages);
        sendBroadcast(intent);
        super.onBackPressed();
    }
}
