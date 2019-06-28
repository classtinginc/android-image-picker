package com.classtinginc.image_picker.images;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.classtinginc.image_picker.models.Image;
import com.classtinginc.library.R;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

public class ItemImage extends RelativeLayout {

    ImageView imageView;
    TextView check;

    private ItemImageListener listener;

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
        check = findViewById(R.id.check);
    }

    public void setListener(ItemImageListener listener) {
        this.listener = listener;
    }

    public void bind(final Image image) {
        Glide.with(getContext())
                .load("file://" + image.getThumbPath())
                .apply(diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .into(imageView);

        check.setText(String.valueOf(image.getSelectedIndex() + 1));
        check.setVisibility(image.getSelectedIndex() > -1 ? VISIBLE : GONE);
        if (listener != null) {
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickedGallery(image, ItemImage.this);
                }
            });
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
