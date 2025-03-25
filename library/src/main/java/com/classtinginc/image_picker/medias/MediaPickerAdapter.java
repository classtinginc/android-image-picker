package com.classtinginc.image_picker.medias;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.classtinginc.image_picker.models.Media;

import java.util.ArrayList;


class MediaPickerAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<Media> media;
    private ItemImageListener listener;
    private final boolean visibleCheck;

    MediaPickerAdapter(Context context, boolean visibleCheck) {
        this.media = new ArrayList<>();
        this.context = context;
        this.visibleCheck = visibleCheck;
    }

    void setListener(ItemImageListener listener) {
        this.listener = listener;
    }

    void setItems(ArrayList<Media> items) {
        this.media = items;
    }

    @Override
    public int getCount() {
        return this.media.size();
    }

    @Override
    public Media getItem(int position) {
        return this.media.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemImage view = (ItemImage) convertView;

        if (convertView == null) {
            view = new ItemImage(context);
        }

        view.setListener(listener);
        view.bind(getItem(position), visibleCheck);

        return view;
    }
}