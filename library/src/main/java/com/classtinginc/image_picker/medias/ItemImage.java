package com.classtinginc.image_picker.medias;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.classtinginc.image_picker.models.Media;
import com.classtinginc.image_picker.utils.ImageUtils;
import com.classtinginc.library.R;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

public class ItemImage extends RelativeLayout {

    ImageView imageView;
    TextView check;
    TextView videoDurationTextView;

    private ItemImageListener listener;

    public ItemImage(Context context) {
        super(context);
        init();
    }

    public ItemImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ItemImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        LinearLayout.inflate(getContext(), R.layout.item_image, this);

        imageView = findViewById(R.id.image_view);
        check = findViewById(R.id.check);
        videoDurationTextView = findViewById(R.id.video_duration);
    }

    public void setListener(ItemImageListener listener) {
        this.listener = listener;
    }

    public void bind(final Media media, boolean visibleCheck) {
        Glide.with(getContext())
                .load("file://" + media.getMediaPath())
                .apply(diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .apply(ImageUtils.getDefaultOptions())
                .into(imageView);

        long duration = media.getDuration();

        if (duration > 0L) {
            videoDurationTextView.setVisibility(View.VISIBLE);
            videoDurationTextView.setText(media.getFormattedDuration());
        } else {
            videoDurationTextView.setVisibility(View.GONE);
        }

        check.setVisibility(visibleCheck ? VISIBLE : GONE);
        check.setActivated(media.getSelectedIndex() > -1);
        check.setText(media.getSelectedIndex() > -1 ? String.valueOf(media.getSelectedIndex() + 1) : "");

        if (listener != null) {
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickedGallery(media);
                }
            });
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}