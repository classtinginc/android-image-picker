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

        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(50), uris -> {
                    if (!uris.isEmpty()) {
                        Log.d("imagePickerExample", "media selected");

                        try {
                            Log.d("imagePickerExample", "media selected 1");
                            ArrayList<Image> images = convertUriToImage(uris);
                            convertImageFormat(images);

                            Log.d("imagePickerExample", "media selected 2");

                            for(Image image : images) {
                                Log.d("imagePickerExample", image.getImageExtension());
                            }

                            Log.d("imagePickerExample", "media selected 3");
                            Log.d("imagePickerExample", "media selected images length" + uris.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent();
                        intent.putExtra(Extra.DATA, "");

                        setResult(Activity.RESULT_OK, intent);

                        finish();
                    } else {
                        Log.d("imagePickerExample", "No media selected");
                    }
                });

        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
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

    public ArrayList<Image> convertUriToImage(List<Uri> uris) throws IOException {
        ArrayList<Image> images = new ArrayList<>();

        Log.d("imagePickerExample", "loop index : 1, count: " + uris.size());

        try {
            for(Uri uri : uris) {
                Log.d("imagePickerExample", "loop index");

                Cursor cursor = this.getContentResolver().query(
                        uri,
                        ImageUtils.proj,
                        MediaStore.Images.Media.DATA + " like ? ",
                        null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");

                String thumbsID = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String thumbsAbsPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String thumbsImageID = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

                Log.d("imagePickerExample", "loop index : 2");

                if (thumbsImageID != null) {
                    images.add(0, new Image(thumbsID, thumbsAbsPath, thumbsImageID));
                }

                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("imagePickerExample", "loop index : 3");

        return images;
    }
}