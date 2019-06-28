package com.classtinginc.image_picker.images;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.library.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;

import rx.functions.Action1;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerView, ItemImageListener, View.OnClickListener {

    private Button select;
    private ImagePickerPresenter presenter;
    private ImagePickerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        ActivityUtils.setNavigation(getSupportActionBar(), "select Image");

        presenter = new ImagePickerPresenter(this);
        adapter = new ImagePickerAdapter(this);
        adapter.setListener(this);

        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(adapter);

        select = findViewById(R.id.select);
        select.setOnClickListener(this);

        Folder folder = (Folder) getIntent().getSerializableExtra("EXTRA_DATA");
        checkPermission(folder);
    }

    @Override
    protected void onDestroy() {
        presenter.unsubscribe();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(16)
    private void checkPermission(final Folder folder) {
        RxPermissions.getInstance(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean granted) {
                    if (granted) {
                        presenter.showImages(ImagePickerActivity.this, folder);
                    } else {
                        Toast.makeText(ImagePickerActivity.this, "do not use image picker", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
    }

    @Override
    public void showImages(ArrayList<Image> images) {
        adapter.setItems(images);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickedGallery(Image image, ItemImage item) {
        presenter.selectImage(image);
    }

    @Override
    public void updateButtonTitle(int selectedImagesCount) {
        select.setText(selectedImagesCount + " selected");
    }

    @Override
    public void done(String images) {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_DATA", images);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void cancel() {
        finish();
    }

    @Override
    public void onClick(View v) {
        presenter.select();
    }
}
