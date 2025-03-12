package com.classtinginc.image_picker.images;

import com.classtinginc.image_picker.models.Image;
import com.classtinginc.image_picker.utils.ImageUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ImageConverter {
    private ArrayList<String> targetExtensions;
    private Context context;

    ImageConverter(Context context) {
        this.context = context;
        targetExtensions = new ArrayList<>(
                Arrays.asList(
                        "heic",
                        "heif"
                ));
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
                String outputImagePath = context.getCacheDir().toString() + "/" + image.getImageName() + ".jpeg";
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

        try {
            for(Uri uri : uris) {
                Cursor cursor = context.getContentResolver().query(
                        uri,
                        ImageUtils.proj,
                        MediaStore.Images.Media.DATA + " like ? ",
                        null,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");

                String thumbsID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String thumbsAbsPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String thumbsImageID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));

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

        return images;
    }
}

