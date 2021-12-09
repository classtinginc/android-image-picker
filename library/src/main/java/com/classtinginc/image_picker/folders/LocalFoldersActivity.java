package com.classtinginc.image_picker.folders;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.images.ImagePickerActivity;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import rx.functions.Action1;

public class LocalFoldersActivity extends AppCompatActivity implements LocalFoldersView, View.OnClickListener, AdapterView.OnItemClickListener {

    private final int REQUEST_CODE = 1;
    private final int TAKE_PHOTO_CODE = 2;

    private LocalFoldersPresenter presenter;
    private LocalFoldersAdapter adapter;
    private File outputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getIntent().getIntExtra(Extra.STYLE, R.style.AppTheme_NoActionBar));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_folders);
        RelativeLayout cameraArea = findViewById(R.id.take_photo);
        cameraArea.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityUtils.setNavigation(getSupportActionBar(), R.string.title_upload_photo_select_photos, R.drawable.ic_close);

        presenter = new LocalFoldersPresenter(this);
        adapter = new LocalFoldersAdapter(this);

        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        checkPermission();
    }

    @Override
    protected void onDestroy() {
        presenter.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;

        try {
            outputImage = getCapturedFile();
            Uri outputImageUri = getOutputUri(outputImage);
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputImageUri);
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Folder folder = adapter.getItem(position);

        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(Extra.DATA, folder);
        intent.putExtras(getIntent().getExtras());

        startActivityForResult(intent, REQUEST_CODE);
    }

    @TargetApi(16)
    private void checkPermission() {
        RxPermissions.getInstance(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean granted) {
                    if (granted) {
                        presenter.showFolders(LocalFoldersActivity.this);
                    } else {
                        Toast.makeText(
                                LocalFoldersActivity.this,
                                TranslationUtils.gePermissionGuide(LocalFoldersActivity.this),
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }
                }
            });
    }

    @Override
    public void showFolders(ArrayList<Folder> folders) {
        adapter.setItems(folders);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Activity.RESULT_OK) {
            if (resultCode == TAKE_PHOTO_CODE) {
                if (outputImage == null) {
                    Log.w("ClasstingImagePicker", "outputImage is null");
                    finish();
                    return;
                } else {
                    data.putExtra(Extra.DATA, new Image("camera", outputImage.getAbsolutePath()));
                    setResult(Activity.RESULT_OK, data);
                }
            } else if (data != null && data.hasExtra(Extra.DATA)) {
                setResult(Activity.RESULT_OK, data);
            }
            finish();
        }
    }

    private File getCapturedFile() throws IOException {
        String prefix = "image-";
        String suffix = ".jpg";
        String dir = Environment.DIRECTORY_PICTURES;

        String filename = prefix + String.valueOf(System.currentTimeMillis()) + suffix;
        File outputFile = null;

        // for versions below 6.0 (23) we use the old File creation & permissions model
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // only this Directory works on all tested Android versions
            // ctx.getExternalFilesDir(dir) was failing on Android 5.0 (sdk 21)
            File storageDir = Environment.getExternalStoragePublicDirectory(dir);
            outputFile = new File(storageDir, filename);
        } else {
            File storageDir = getExternalFilesDir(null);
            outputFile = File.createTempFile(prefix, suffix, storageDir);
        }

        return outputFile;
    }

    private Uri getOutputUri(File capturedFile) {
        // for versions below 6.0 (23) we use the old File creation & permissions model
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return Uri.fromFile(capturedFile);
        }

        // for versions 6.0+ (23) we use the FileProvider to avoid runtime permissions
        String packageName = getPackageName();
        return FileProvider.getUriForFile(this, packageName + ".fileprovider", capturedFile);
    }
}
