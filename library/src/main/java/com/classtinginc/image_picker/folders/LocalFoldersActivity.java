package com.classtinginc.image_picker.folders;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.images.ImagePickerActivity;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.utils.ActivityUtils;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;

import rx.functions.Action1;

public class LocalFoldersActivity extends AppCompatActivity implements LocalFoldersView, AdapterView.OnItemClickListener {

    private LocalFoldersPresenter presenter;
    private LocalFoldersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_folders);

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

        startActivityForResult(intent, 1);
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

        if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(Extra.DATA)) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }
}
