package com.classtinginc.image_picker.utils;

import com.classtinginc.image_picker.models.Media;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MediaUtil {
    private static final String TAG = MediaUtil.class.getSimpleName();

    private final ArrayList<String> targetExtensions;
    private final Context context;

    public MediaUtil(Context context) {
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
    private boolean convertFormat(String inputImagePath,
                                 String outputImagePath, Bitmap.CompressFormat format) throws IOException {
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

    public void convertImageFormat(Media media) throws IOException {
        if (this.checkIsTargetExtension(media.getExtension())) {
            String path = media.getMediaPath();
            String currentMediaName = media.getMediaName();
            String newMediaName = currentMediaName + ".jpeg";
            String outputImagePath = context.getCacheDir().toString() + "/" + newMediaName;
            boolean result = this.convertFormat(path, outputImagePath, Bitmap.CompressFormat.JPEG);
            if (result) {
                media.setMediaName(newMediaName);
                media.setMediaPath(outputImagePath);
            }
        }
    }

    public long getVideoDuration(Uri uri) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (IOException e) {
            Log.e(TAG, "Get video duration error", e);
            return 0;
        } finally {
            mediaPlayer.release();
        }
    }

    public String getPathFromUri(Uri uri, String fileName) {
        if (uri == null) return null;

        if (fileName == null || fileName.isEmpty()) {
            fileName = "default_media_name";
        }

        File file;
        try {
            file = new File(context.getCacheDir(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "I/O error while creating file in cache directory", e);
            return null;
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
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

    public String getFileName(Context context, Uri uri) {
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
}