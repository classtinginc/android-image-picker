package com.classtinginc.image_picker.folders;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.medias.MediaPickerActivity;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;

import java.util.ArrayList;
import java.util.Objects;

public class LocalFoldersActivity extends AppCompatActivity implements LocalFoldersView, AdapterView.OnItemClickListener {

    private final int REQUEST_CODE = 1;

    private LocalFoldersPresenter presenter;
    private LocalFoldersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_folders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActivityUtils.setNavigation(getSupportActionBar(), R.drawable.ic_close);

        String mediaTypeStr = getIntent().getStringExtra(Extra.MEDIA_TYPE);
        MediaType mediaType = MediaType.fromString(mediaTypeStr);

        presenter = new LocalFoldersPresenter(this, mediaType);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Folder folder = adapter.getItem(position);
        String mediaTypeStr = getIntent().getStringExtra(Extra.MEDIA_TYPE);

        Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(Extra.MEDIA_TYPE, mediaTypeStr);
        intent.putExtra(Extra.DATA, folder);
        intent.putExtras(Objects.requireNonNull(getIntent().getExtras()));

        startActivityForResult(intent, REQUEST_CODE);
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void showFolders(ArrayList<Folder> folders) {
        adapter.setItems(folders);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(Extra.DATA)) {
            Bundle extras = data.getExtras();
            assert extras != null;
            for (String key : extras.keySet()) {
                Log.d("CTImagePicker", "extra data Key: " + key + ", Value: " + extras.get(key));
            }

            setResult(Activity.RESULT_OK, data);

            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean granted = false;
            for (int i=0; i< grantResults.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                }
            }

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
    }
}