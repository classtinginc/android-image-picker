package com.classtinginc.image_picker.images;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityUtils.setNavigation(getSupportActionBar(), R.string.title_upload_photo_select_photos);

        int maxSize = getIntent().getIntExtra(Extra.MAX_SIZE, 0);
        boolean allowMultiple = getIntent().getBooleanExtra(Extra.ALLOW_MULTIPLE, false);

        presenter = new ImagePickerPresenter(this);
        presenter.setMaxSize(maxSize);
        presenter.setAllowMultiple(allowMultiple);

        adapter = new ImagePickerAdapter(this);
        adapter.setListener(this);

        GridView grid = findViewById(R.id.grid);
        grid.setAdapter(adapter);

        select = findViewById(R.id.select);
        select.setVisibility(allowMultiple ? View.VISIBLE : View.GONE);
        select.setOnClickListener(this);
        updateButtonState(0);

        Folder folder = (Folder) getIntent().getSerializableExtra(Extra.DATA);
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
                        Toast.makeText(
                                ImagePickerActivity.this,
                                TranslationUtils.gePermissionGuide(ImagePickerActivity.this),
                                Toast.LENGTH_SHORT
                        ).show();
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
    public void updateButtonState(int selectedImagesCount) {
        select.setEnabled(selectedImagesCount > 0);
        select.setText(TranslationUtils.getButtonTitle(this, selectedImagesCount));
    }

    @Override
    public void showCheckMaxSize(int maxSize) {
        Toast.makeText(ImagePickerActivity.this, TranslationUtils.getMaxSizeGuide(this, maxSize), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void done(String json) {
        Intent intent = new Intent();
        intent.putExtra(Extra.DATA, json);
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
