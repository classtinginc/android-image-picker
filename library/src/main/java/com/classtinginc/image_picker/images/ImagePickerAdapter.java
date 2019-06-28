package com.classtinginc.image_picker.images;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.classtinginc.image_picker.models.Image;

import java.util.ArrayList;


class ImagePickerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Image> images;
    private ItemImageListener listener;

    ImagePickerAdapter(Context context) {
        this.images = new ArrayList<>();
        this.context = context;
    }

    void setListener(ItemImageListener listener) {
        this.listener = listener;
    }

    void setItems(ArrayList<Image> items) {
        this.images = items;
    }

    @Override
    public int getCount() {
        return this.images.size();
    }

    @Override
    public Image getItem(int position) {
        return this.images.get(position);
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
        view.bind(getItem(position));

        return view;
    }
}