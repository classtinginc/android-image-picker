package com.classtinginc.image_picker.images;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.library.R;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

public class ItemImage extends RelativeLayout {

    ImageView imageView;

    public ItemImage(Context context) {
        super(context);
        init();
    }

    public ItemImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ItemImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        LinearLayout.inflate(getContext(), R.layout.item_image, this);

        imageView = findViewById(R.id.image_view);
    }

    public void bind(Image image) {
        Glide.with(getContext())
                .load("file://" + image.getThumbPath())
                .apply(diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .into(imageView);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
