package com.classtinginc.image_picker.folders;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.classtinginc.image_picker.images.ImagePickerActivity;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.library.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;

import rx.functions.Action1;

public class LocalFoldersActivity extends AppCompatActivity implements LocalFoldersView, AdapterView.OnItemClickListener {

    LocalFoldersPresenter presenter;
    LocalFoldersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_folders);

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Folder folder = adapter.getItem(position);

        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra("EXTRA_DATA", folder);
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
                        Toast.makeText(LocalFoldersActivity.this, "do not use image picker", Toast.LENGTH_SHORT).show();
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
}
