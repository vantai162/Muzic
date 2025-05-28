package com.example.muzic.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import jp.wasabeef.glide.transformations.BlurTransformation;
import android.graphics.Color;

public class BlurUtils {
    private static final int BLUR_RADIUS = 100; // Increased blur radius
    private static final int BLUR_SAMPLING = 1;

    public static void applyBlur(Context context, ImageView source, ImageView target) {
        if (source.getDrawable() == null) return;

        // Create a multi-transformation for enhanced blur effect
        MultiTransformation<android.graphics.Bitmap> multiTransformation = new MultiTransformation<>(
            new CenterCrop(),
            new BlurTransformation(BLUR_RADIUS, BLUR_SAMPLING)
        );

        // Load and transform the image with crossfade
        Glide.with(context)
            .load(source.getDrawable())
            .transform(multiTransformation)
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .override(2048, 2048) // Higher resolution for better quality
            .into(target);
    }

    public static void clearBlur(ImageView view) {
        if (view != null) {
            Glide.with(view.getContext()).clear(view);
            view.setImageDrawable(null);
        }
    }
} 