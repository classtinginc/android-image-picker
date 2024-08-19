package com.classtinginc.image_picker;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.classtinginc.image_picker.consts.Extra;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 1;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                            // Callback is invoked after the user selects a media item or closes the
                            // photo picker.
                            if (uri != null) {
                                Log.d("PhotoPicker", "Selected URI: " + uri);
                            } else {
                                Log.d("PhotoPicker", "No media selected");
                            }
                        }
                );

        findViewById(R.id.multiple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker
                    .with(pickMedia)
                    .maxSize(3)
                    .availableSize(3)
                    .allowMultiple(true)
                    .startActivityForResult(REQUEST_CODE);
            }
        });

        findViewById(R.id.single).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker
                    .with(pickMedia)
                    .allowMultiple(false)
                    .startActivityForResult(REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("imagePickerExample", "canceled");
        } else if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(Extra.DATA)) {
            Log.e("imagePicker", data.getStringExtra(Extra.DATA));
        }
    }
}