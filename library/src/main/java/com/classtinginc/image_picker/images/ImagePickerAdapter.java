package com.classtinginc.image_picker.images;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.classtinginc.image_picker.models.Image;

import java.util.ArrayList;


public class ImagePickerAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<Image> images;

    public ImagePickerAdapter(Context context) {
        this.images = new ArrayList<>();
        this.context = context;
    }

    public void setItems(ArrayList<Image> items) {
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

        view.bind(getItem(position));

        return view;
    }
}