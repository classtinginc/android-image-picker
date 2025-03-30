package com.classtinginc.image_picker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import com.classtinginc.image_picker.consts.Constants;
import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.folders.LocalFoldersActivity;
import com.classtinginc.image_picker.models.Media;
import com.classtinginc.image_picker.utils.MediaUtil;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.library.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = ImagePickerActivity.class.getSimpleName();

    MediaUtil mediaUtil;
    ActivityResultLauncher<PickVisualMediaRequest> mediaPicker;
    Handler mHandler = new Handler();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_image_picker);
        progressBar = findViewById(R.id.progressBar);

        mediaUtil = new MediaUtil(this);

        boolean canUseSystemPhotoPicker = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
        String mediaTypeStr = getIntent().getStringExtra(Extra.MEDIA_TYPE);
        int maxSize = getIntent().getIntExtra(Extra.MAX_SIZE, Constants.DEFAULT_MAX_SIZE);

        if (canUseSystemPhotoPicker) {
            openSystemPhotoPicker(mediaTypeStr, maxSize);
        } else {
            openCustomPhotoPicker(mediaTypeStr, maxSize);
        }
    }

    private void openSystemPhotoPicker(String mediaTypeStr, int maxSize) {
        MediaType mediaType = MediaType.fromString(mediaTypeStr);

        ActivityResultContracts.PickVisualMedia.VisualMediaType mediaTypeEnum;
        switch (mediaType) {
            case IMAGE_AND_VIDEO:
                mediaTypeEnum = ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE;
                break;
            case IMAGE:
            default:
                mediaTypeEnum = ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE;
                break;
        }

        if (maxSize == 1) {
            mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null)
                    handleMediaSelection(Collections.singletonList(uri));
                else
                    finish();
            });
        } else {
            mediaPicker = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(maxSize), uris -> {
                if (!uris.isEmpty())
                    handleMediaSelection(uris);
                else
                    finish();
            });
        }

        mediaPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(mediaTypeEnum)
                .build());
    }

    private void openCustomPhotoPicker(String mediaTypeStr, int maxSize) {
        ActivityResultLauncher<Intent> localFolderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<Media> media = (ArrayList<Media>) data.getSerializableExtra(Extra.DATA);
                            handleLegacyMediaSelection(media);
                        } else {
                            finish();
                        }
                    } else {
                        finish();
                    }
                }
        );

        Intent intent = new Intent(this, LocalFoldersActivity.class);
        intent.putExtra(Extra.MEDIA_TYPE, mediaTypeStr);
        intent.putExtra(Extra.MAX_SIZE, maxSize);
        localFolderLauncher.launch(intent);
    }

    private void handleLegacyMediaSelection(ArrayList<Media> mediaList) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Gson gson = new GsonBuilder().create();
                ArrayList<Media> mediums = new ArrayList<>();

                for (Media media : mediaList) {
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(media.getExtension());

                    if (mimeType != null) {
                        if (mimeType.startsWith("image/")) {
                            mediaUtil.convertImageFormat(media);
                            mediums.add(media);
                        } else if (mimeType.startsWith("video/")) {
                            mediaUtil.convertVideoFormat(media);
                            mediums.add(media);
                        } else {
                            Log.d(TAG, "Unknown media type.");
                        }
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(Extra.DATA, gson.toJson(mediums));
                setResult(Activity.RESULT_OK, intent);
                Log.d(TAG, "handleMediaSelection ConvertImage Success" + intent);
            } catch (Exception e) {
                Log.e(TAG, "Handle media selection error", e);
            } finally {
                mHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
            }
        }).start();
    }

    private Media convertUriToMedia(Uri uri) {
        Media media = null;

        try {
            String fileName = mediaUtil.getFileName(this, uri);
            String filePath = mediaUtil.getPathFromUri(uri, fileName);

            if (fileName != null && !fileName.isEmpty() && filePath != null && !filePath.isEmpty()) {
                media = new Media(filePath, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Convert uri to video media error", e);
        }

        Log.d(TAG, "Converted uri to media " + media);
        return media;
    };

    private void handleMediaSelection(List<Uri> uris) {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                Gson gson = new GsonBuilder().create();
                ArrayList<Media> mediums = new ArrayList<>();

                for (Uri uri : uris) {
                    String mimeType = getContentResolver().getType(uri);
                    Media media = convertUriToMedia(uri);

                    if (mimeType != null) {
                        if (mimeType.startsWith("image/")) {
                            mediaUtil.convertImageFormat(media);
                            mediums.add(media);
                        } else if (mimeType.startsWith("video/")) {
                            long duration = mediaUtil.getVideoDuration(uri);
                            media.setDuration(duration);
                            mediaUtil.convertVideoFormat(media);
                            mediums.add(media);
                        } else {
                            Log.d(TAG, "Unknown media type.");
                        }
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(Extra.DATA, gson.toJson(mediums));
                setResult(Activity.RESULT_OK, intent);
                Log.d(TAG, "handleMediaSelection ConvertImage Success" + intent);
            } catch (Exception e) {
                Log.e(TAG, "Handle media selection error", e);
            } finally {
                mHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
            }
        }).start();
    }
}