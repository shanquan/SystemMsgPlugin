package com.byd.msgplugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView lv;
    private ArrayList<AppInfo> appList = new ArrayList();
    private AppAdapter adapter;
    private String selectPackages;
    private StringBuffer buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(this, "layout", "activity_installed_app"));
        initViews();
        initDatas();
        adapter = new AppAdapter(this, appList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    private void initDatas() {
        Intent intent = getIntent();
        selectPackages = intent.getStringExtra("selectPackages");
        buffer = new StringBuffer(selectPackages);

        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
                AppInfo info = new AppInfo();
                info.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                info.pkgName = packageInfo.packageName;
                info.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                if (selectPackages.contains(info.pkgName)) {
                    info.setCheck(true);
                } else {
                    info.setCheck(false);
                }
                appList.add(info);
            } else { //系统应用

            }
        }
    }

    private void initViews() {
        lv = (ListView) findViewById(MResource.getIdByName(this, "id", "lv"));
    }

    class AppInfo {  //应用信息实体
        private String appName;
        private String pkgName;
        private Drawable appIcon;
        private boolean isCheck;

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

        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }
    }

    public void back(View view) {
        final Intent intent = new Intent();
        intent.putExtra("selectPackages", buffer.toString());
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(buffer.toString())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(23, intent);
                        finish();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AppInfo info = appList.get(i);
        AppAdapter.ViewHolder vh = (AppAdapter.ViewHolder) view.getTag();
        int index = buffer.indexOf(info.pkgName);
        if (info.isCheck && index != -1) {
            int length = info.pkgName.length()+1;
            buffer.delete(index, index + length);
            info.setCheck(false);
            vh.cbIsSelect.setChecked(false);
        } else if (!info.isCheck && index == -1) {
            if (buffer.toString().split(",").length >= 5) {
                Toast.makeText(this, "最多只能选择五个应用", Toast.LENGTH_SHORT).show();
            } else {
                buffer.append(info.pkgName + ",");
                info.setCheck(true);
                vh.cbIsSelect.setChecked(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent();
        intent.putExtra("selectPackages", buffer.toString());
        setResult(23, intent);
        super.onBackPressed();
    }
}
