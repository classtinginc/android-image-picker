package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.classtinginc.image_picker.consts.Extra;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.multiple).setOnClickListener(v -> ImagePicker
                .with(MainActivity.this)
                .mediaType("imageandvideo")
                .maxSize(10)
                .startActivityForResult(REQUEST_CODE));

        findViewById(R.id.single).setOnClickListener(v -> ImagePicker
                .with(MainActivity.this)
                .mediaType("image")
                .startActivityForResult(REQUEST_CODE));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult resultCode: " + resultCode);

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Canceled");
        } else if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(Extra.DATA)) {
            Log.d(TAG, Objects.requireNonNull(data.getStringExtra(Extra.DATA)));
        } else if (resultCode == REQUEST_CODE) {
            Log.d(TAG, "REQUEST_CODE");
        }
    }
}