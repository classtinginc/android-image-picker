package com.classtinginc.image_picker.folders;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.models.Media;
import com.classtinginc.image_picker.modules.MediaType;
import com.classtinginc.library.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity {
    private static final String TAG = AppCompatActivity.class.getSimpleName();
    private final ArrayList<String> targetExtensions = new ArrayList<>(
            Arrays.asList(
                    "heic",
                    "heif"
            ));
    ActivityResultLauncher<PickVisualMediaRequest> mediaPicker;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        String mediaTypeStr = getIntent().getStringExtra(Extra.MEDIA_TYPE);
        int mediaCount = getIntent().getIntExtra(Extra.MAX_SIZE, 1);

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

        if (mediaCount == 1) {
            mediaPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null)
                    handleMediaSelection(Collections.singletonList(uri));
                else
                    finish();
            });
        } else {
            mediaPicker = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(mediaCount), uris -> {
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

    private String getFileName(Context context, Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                cursor.moveToFirst();
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }

        return fileName;
    }

    private Media convertUriToMedia(Uri uri) {
        Media media = null;

        try {
            String fileName = getFileName(this, uri);
            String filePath = getPathFromUri(uri, fileName);

            if (fileName != null && !fileName.isEmpty() && filePath != null && !filePath.isEmpty()) {
                media = new Media(filePath, fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Convert uri to video media error", e);
        }

        Log.d(TAG, "Converted uri to media " + media);
        return media;
    };

    public String getPathFromUri(Uri uri, String fileName) {
        if (uri == null) return null;

        if (fileName == null || fileName.isEmpty()) {
            fileName = "default_media_name";
        }

        File file;
        try {
            file = new File(this.getCacheDir(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "I/O error while creating file in cache directory", e);
            return null;
        }

        try (InputStream inputStream = this.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            if (inputStream == null) {
                Log.e(TAG, "Failed to open InputStream from URI");
                return null;
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found for URI: " + uri, e);
        } catch (IOException e) {
            Log.e(TAG, "I/O error while processing URI: " + uri, e);
        }

        return null;
    }

    private void handleMediaSelection(List<Uri> uris) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
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
                            convertImageFormat(media);
                            mediums.add(media);
                        } else if (mimeType.startsWith("video/")) {
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

    /**
     * 이미지를 다른 포맷으로 변환합니다.
     *
     * @param inputImagePath  원본 이미지의 경로
     * @param outputImagePath 새로 생성될 이미지의 경로
     * @return 파일 형식 변환에 성공하면 true, 그렇지 않으면 false 반환
     * @throws IOException 이미지 쓰기 중 에러가 발생하면 예외 발생
     */
    private boolean convertFormat(String inputImagePath,
                                 String outputImagePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(inputImagePath);
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);

        Bitmap bitmapFactory = BitmapFactory.decodeFile(inputImagePath);
        ExifInterface originalExif = new ExifInterface(inputImagePath);
        String originalOrientation = originalExif.getAttribute(ExifInterface.TAG_ORIENTATION);

        boolean result = bitmapFactory.compress(Bitmap.CompressFormat.JPEG,100, outputStream);

        assert originalOrientation != null;
        
        if (shouldSetOrientation(originalOrientation)) {
            ExifInterface exif = new ExifInterface(outputImagePath);
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, originalOrientation);
            exif.saveAttributes();
        }

        outputStream.close();
        inputStream.close();

        return result;
    }

    private boolean checkIsTargetExtension(String fileExt) {
        return this.targetExtensions.contains(fileExt);
    }

    private boolean shouldSetOrientation(String orientation) {
        return !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_NORMAL))
                && !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));
    }

    private void convertImageFormat(Media media) throws IOException {
            if (this.checkIsTargetExtension(media.getExtension())) {
                String path = media.getMediaPath();
                String currentMediaName = media.getMediaName();
                String newMediaName = currentMediaName + ".jpeg";
                String outputImagePath = this.getCacheDir().toString() + "/" + newMediaName;
                boolean result = this.convertFormat(path, outputImagePath);
                if (result) {
                    media.setMediaName(newMediaName);
                    media.setMediaPath(outputImagePath);
                }
            }
    }
}