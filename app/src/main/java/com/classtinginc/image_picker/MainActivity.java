package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.provider.MediaStore;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.classtinginc.image_picker.consts.Extra;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.multiple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker
                        .with(MainActivity.this)
                        .startActivityForResult(REQUEST_CODE);
            }
        });

        findViewById(R.id.single).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker
                        .with(MainActivity.this)
                        .startActivityForResult(REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("imagePickerExample", "onActivityResult resultCode: " + resultCode);

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("imagePickerExample", "canceled");
        } else if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(Extra.DATA)) {
            Log.e("imagePickerExample", data.getStringExtra(Extra.DATA));
        } else if (resultCode == REQUEST_CODE) {
            Log.e("imagePickerExample", "REQUEST_CODE");
        }
    }
}