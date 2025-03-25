package com.classtinginc.image_picker.medias;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Media;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;

import java.util.ArrayList;
import java.util.Objects;

public class MediaPickerActivity extends AppCompatActivity implements MediaPickerView, ItemImageListener, View.OnClickListener {
    private Button select;
    private MediaPickerPresenter presenter;
    private MediaPickerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityUtils.setNavigation(Objects.requireNonNull(getSupportActionBar()));

        int maxSize = getIntent().getIntExtra(Extra.MAX_SIZE, 0);
        boolean allowMultiple = maxSize > 1;
        String mediaTypeStr = getIntent().getStringExtra(Extra.MEDIA_TYPE);

        presenter = new MediaPickerPresenter(this);
        presenter.setMediaType(MediaType.fromString(mediaTypeStr));
        presenter.setMaxSize(maxSize);
        presenter.setAllowMultiple(allowMultiple);

        adapter = new MediaPickerAdapter(this, allowMultiple);
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

    private void checkPermission(Folder folder) {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            presenter.showImages(MediaPickerActivity.this, folder);
        } else {
            Toast.makeText(
                    MediaPickerActivity.this,
                    TranslationUtils.gePermissionGuide(MediaPickerActivity.this),
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    @Override
    public void showImages(ArrayList<Media> media) {
        adapter.setItems(media);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickedGallery(Media media) {
        presenter.selectImage(media);
    }

    @Override
    public void updateButtonState(int selectedImagesCount) {
        select.setEnabled(selectedImagesCount > 0);
        select.setText(TranslationUtils.getButtonTitle(this, selectedImagesCount));
    }

    @Override
    public void showCheckMaxSize(int maxSize) {
        Toast.makeText(MediaPickerActivity.this, TranslationUtils.getMaxSizeGuide(this, maxSize), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void done(ArrayList<Media> media) {
        Intent intent = new Intent();
        intent.putExtra(Extra.DATA, media);
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