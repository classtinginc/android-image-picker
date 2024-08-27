package com.classtinginc.image_picker.folders;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.classtinginc.image_picker.consts.Extra;
import com.classtinginc.image_picker.images.ImageConverter;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalFoldersActivity extends AppCompatActivity {
    private ArrayList<String> targetExtensions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        targetExtensions = new ArrayList<>(
                Arrays.asList(
                        "heic",
                        "heif"
                ));

        boolean allowMultiple = getIntent().getBooleanExtra(Extra.ALLOW_MULTIPLE, false);

        Log.d("imagePickerExample", "aa: " + allowMultiple);

        if(allowMultiple) {
            ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                    registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(50), uris -> {
                        if (!uris.isEmpty()) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                Intent intent = new Intent();

                                ArrayList<Image> images = convertUrisToImages(uris);
                                convertImageFormat(images);

                                String value = gson.toJson(images);
                                intent.putExtra(Extra.DATA, value);

                                setResult(Activity.RESULT_OK, intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            finish();
                        } else {
                            Log.d("imagePickerExample", "No media selected");
                        }
                    });

            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        } else {
            ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                    registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                        // Callback is invoked after the user selects a media item or closes the
                        // photo picker.
                        if (uri != null) {
                            try {
                                Gson gson = new GsonBuilder().create();
                                Intent intent = new Intent();

                                ArrayList<Image> images = new ArrayList<>();
                                Image image = convertUriToImage(uri, 0);
                                images.add(image);

                                convertImageFormat(images);

                                String value = gson.toJson(images);
                                intent.putExtra(Extra.DATA, value);

                                setResult(Activity.RESULT_OK, intent);

                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("PhotoPicker", "No media selected");
                        }
                    });

            // Launch the photo picker and let the user choose only images.
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }


    /**
     * 이미지를 다른 포맷으로 변환합니다.
     *
     * @param inputImagePath 원본 이미지의 경로
     * @param outputImagePath 새로 생성될 이미지의 경로
     * @param format 변경할 이미지 형식 (jpg, png, bmp, wbmp, gif 등)
     * @return 파일 형식 변환에 성공하면 true, 그렇지 않으면 false 반환
     * @throws IOException 이미지 쓰기 중 에러가 발생하면 예외 발생
     */
    public boolean convertFormat(String inputImagePath,
                                 String outputImagePath, Bitmap.CompressFormat format) throws IOException {
        FileInputStream inputStream = new FileInputStream(inputImagePath);
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);

        Bitmap bitmapFactory = BitmapFactory.decodeFile(inputImagePath);
        ExifInterface originalExif = new ExifInterface(inputImagePath);
        String originalOrientation = originalExif.getAttribute(ExifInterface.TAG_ORIENTATION);

        boolean result = bitmapFactory.compress(format,100, outputStream);

        if (shouldSetOrientation(originalOrientation)) {
            ExifInterface exif = new ExifInterface(outputImagePath);
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, originalOrientation);
            exif.saveAttributes();
        }

        outputStream.close();
        inputStream.close();

        return result;
    }

    public boolean checkIsTargetExtension(String fileExt) {
        return this.targetExtensions.contains(fileExt);
    }

    private boolean shouldSetOrientation(String orientation) {
        return !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_NORMAL))
                && !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));
    }

    public void convertImageFormat(ArrayList<Image> selectedImages) throws IOException {
        for(Image image : selectedImages) {
            if (this.checkIsTargetExtension(image.getImageExtension())) {
                String path = image.getThumbPath();
                String outputImagePath = this.getCacheDir().toString() + "/" + image.getImageName() + ".jpeg";
                try {
                    boolean result = this.convertFormat(path, outputImagePath, Bitmap.CompressFormat.JPEG);
                    if (result == true) {
                        image.setThumbPath(outputImagePath);
                    }
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
    }

    public Image convertUriToImage(Uri uri, int index) throws IOException {
        Image image = null;

        try {
            Cursor cursor = this.getContentResolver().query(
                    uri,
                    ImageUtils.proj,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                String thumbsID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String thumbsAbsPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String thumbsImageID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));

                if (thumbsImageID != null) {
                    image = new Image(thumbsID, thumbsAbsPath, thumbsImageID);
                    image.setSelectedIndex(index);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }

    public ArrayList<Image> convertUrisToImages(List<Uri> uris) throws IOException {
        ArrayList<Image> images = new ArrayList<>();

        int index = 0;
        for(Uri uri : uris) {
            Image image = convertUriToImage(uri, index);
            images.add(image);
            index++;
        }

        return images;
    }
}