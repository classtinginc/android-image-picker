package com.classtinginc.image_picker.images;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;

import java.util.ArrayList;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerView, ItemImageListener, View.OnClickListener {
    private final int REQUEST_CODE = 1;
    private Button select;
    private ImagePickerPresenter presenter;
    private ImagePickerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getIntent().getIntExtra(Extra.STYLE, R.style.AppTheme_NoActionBar));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityUtils.setNavigation(getSupportActionBar(), R.string.title_upload_photo_select_photos);

        int maxSize = getIntent().getIntExtra(Extra.MAX_SIZE, 0);
        int availableSize = getIntent().getIntExtra(Extra.AVAILABLE_SIZE, 0);
        boolean allowMultiple = getIntent().getBooleanExtra(Extra.ALLOW_MULTIPLE, false);

        presenter = new ImagePickerPresenter(this);
        presenter.setMaxSize(maxSize);
        presenter.setAvailableSize(availableSize);
        presenter.setAllowMultiple(allowMultiple);

        adapter = new ImagePickerAdapter(this, allowMultiple);
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
        if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES) == PERMISSION_GRANTED
        ) {
            presenter.showImages(ImagePickerActivity.this, folder);
        } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                        ContextCompat.checkSelfPermission(this, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED
        ) {
            presenter.showImages(ImagePickerActivity.this, folder);
        } else if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
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
    public void onClickedGallery(Image image) {
        presenter.selectImage(this, image);
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
        presenter.select(this);
    }
}
