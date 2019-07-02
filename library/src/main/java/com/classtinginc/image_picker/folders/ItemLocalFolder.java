package com.classtinginc.image_picker.folders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.classtinginc.image_picker.models.Folder;
import com.classtinginc.image_picker.utils.TranslationUtils;
import com.classtinginc.library.R;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

public class ItemLocalFolder extends LinearLayout {

    ImageView image;
    TextView title;
    TextView subTitle;

    public ItemLocalFolder(Context context) {
        super(context);
        init();
    }

    public ItemLocalFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ItemLocalFolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        LinearLayout.inflate(getContext(), R.layout.item_local_folder, this);

        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.sub_title);
    }

    public void bind(Folder folder) {
        Glide.with(getContext())
                .load("file://" + folder.getThumbPath())
                .apply(diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .placeholder(R.color.grey_300)
                .into(image);

        String folderArray[] = folder.getPath().split("/");
        title.setText(folderArray[folderArray.length - 1]);

        subTitle.setText(TranslationUtils.getImagesCount(getContext(), folder.getSize()));
    }
}
