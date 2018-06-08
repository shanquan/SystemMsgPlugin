package com.byd.msgplugin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yang.yu11 on 2017/11/29.
 */

public class AppAdapter extends BaseAdapter {
    private Context context;
    private List<InstalledAppActivity.AppInfo> infos;

    public AppAdapter(Context context, List<InstalledAppActivity.AppInfo> infos) {
        this.context = context;
        this.infos = infos;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(MResource.getIdByName(context, "layout", "item_applst"), null);
            vh = new ViewHolder();
            vh.ivIcon = (ImageView) convertView.findViewById(MResource.getIdByName(context, "id", "ivIcon"));
            vh.tvAppName = (TextView) convertView.findViewById(MResource.getIdByName(context, "id", "tvAppName"));
            vh.tvAppPkg = (TextView) convertView.findViewById(MResource.getIdByName(context, "id", "tvAppPkg"));
            vh.cbIsSelect = (CheckBox) convertView.findViewById(MResource.getIdByName(context, "id", "cbIsSelect"));
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.ivIcon.setImageDrawable(infos.get(position).getAppIcon());
        vh.tvAppName.setText(infos.get(position).getAppName());
        vh.tvAppPkg.setText(infos.get(position).getPkgName());
        vh.cbIsSelect.setChecked(infos.get(position).isCheck());
        return convertView;
    }

    class ViewHolder {
        ImageView ivIcon;
        TextView tvAppName, tvAppPkg;
        CheckBox cbIsSelect;
    }
}
