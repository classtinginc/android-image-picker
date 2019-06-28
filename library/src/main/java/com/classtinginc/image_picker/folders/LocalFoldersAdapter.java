package com.classtinginc.image_picker.folders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.classtinginc.image_picker.models.Folder;

import java.util.ArrayList;

class LocalFoldersAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<Folder> items;

    LocalFoldersAdapter(Context context) {
        this.items = new ArrayList<>();
        this.context = context;
    }

    void setItems(ArrayList<Folder> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Folder getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ItemLocalFolder view = (ItemLocalFolder) convertView;

        if (convertView == null) {
            view = new ItemLocalFolder(context);
        }

        view.bind(getItem(position));

        return view;
    }
}