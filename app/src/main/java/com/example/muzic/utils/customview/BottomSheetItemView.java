package com.example.muzic.utils.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.muzic.R;
import com.squareup.picasso.Picasso;


public class BottomSheetItemView extends LinearLayout {

    private String ID = "";
    private String NAME = "";
    private String IMAGE_URL = "";

    public BottomSheetItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public BottomSheetItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BottomSheetItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public BottomSheetItemView(Context context, String string, String imageUrl, String id){
        super(context);
        init(null, 0);
        getTitleTextView().setText(string);
        if(!imageUrl.isBlank())Picasso.get().load(Uri.parse(imageUrl)).into(getIconImageView());
        ID = id;
        NAME = string;
        IMAGE_URL = imageUrl;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final String title;
        final Drawable mExampleDrawable;

        inflate(getContext(), R.layout.bottom_sheet_items_custom_view, this);

        setFocusable(true);
        setClickable(true);

        setOnClickListener(view -> {
            Log.i("BottomSheetItemView", "init: " + "Clicked!!");
        });

        if(attrs==null) return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BottomSheetItemView, defStyle, 0);

            title = a.getString(
                    R.styleable.BottomSheetItemView_title);

            mExampleDrawable = a.getDrawable(
                        R.styleable.BottomSheetItemView_android_src);


            getTitleTextView().setText(title);
            getIconImageView().setImageDrawable(mExampleDrawable);

            final int padding = a.getDimensionPixelSize(R.styleable.BottomSheetItemView_srcPadding, 4);

            getIconImageView().setPadding(padding,padding,padding,padding);


            a.recycle();
    }

    public TextView getTitleTextView() {
        return findViewById(R.id.text);
    }

    public ImageView getIconImageView() {
        return findViewById(R.id.icon);
    }
}