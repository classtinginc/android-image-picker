package com.classtinginc.image_picker.images;

import com.classtinginc.image_picker.models.Image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

        boolean result = bitmapFactory.compress(format,100, outputStream);

        outputStream.close();
        inputStream.close();

        return result;
    }

    public boolean checkIsTargetExtension(String fileExt) {
        return this.targetExtensions.contains(fileExt);
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
}

