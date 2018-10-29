package com.example.administrator.memoplusplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    LayoutInflater inflater = null;
    private ArrayList<ItemData> m_oData;
    private int nListCnt = 0;

    public ListAdapter(ArrayList<ItemData> m_oData) {
        this.m_oData = m_oData;
        nListCnt = this.m_oData.size();
    }

    @Override
    public int getCount() {
        return nListCnt;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            final Context context = viewGroup.getContext();
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            view = inflater.inflate(R.layout.listview_item, viewGroup, false);
            ViewHolder holder = new ViewHolder();
            holder.oTextTitle = (TextView) view.findViewById(R.id.textTitle);
            holder.oTextDate = (TextView) view.findViewById(R.id.textDate);
            view.setTag(holder);
        }
        ItemData entry = m_oData.get(i);
        if (entry != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.oTextTitle.setText(m_oData.get(i).strTitle);
            holder.oTextDate.setText(m_oData.get(i).strDate);
        }
        return view;
    }

    static class ViewHolder {//ViewHolder Patten 사용
        TextView oTextTitle;
        TextView oTextDate;
    }

}
