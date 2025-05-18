package com.example.muzic.utils.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.muzic.R;

public class MaterialCustomSwitch extends LinearLayout {

    private String textHead = "";
    private String textOn = "";
    private String textOff = "";
    private boolean checked = false;

    private TextView textHeadView;
    private TextView textDescView;
    private MaterialSwitch materialSwitch;

    private OnCheckChangeListener onCheckChangedListener;

    public MaterialCustomSwitch(Context context) {
        super(context);
        init(null, 0);
    }

    public MaterialCustomSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MaterialCustomSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes

        inflate(getContext(), R.layout.material_custom_switch, this);

        textHeadView = findViewById(R.id.text_head);
        textDescView = findViewById(R.id.text_desc);
        materialSwitch = findViewById(R.id.materialSwitch);
        findViewById(R.id.root).setOnClickListener(v -> materialSwitch.toggle());

        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            textDescView.setText(isChecked ? textOn : textOff);
            if(onCheckChangedListener!=null)onCheckChangedListener.onCheckChanged(isChecked);
        });

        if(attrs==null) return;

        try (TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialCustomSwitch, defStyle, 0)) {

            textHead = a.getString(R.styleable.MaterialCustomSwitch_textHead);
            textOn = a.getString(R.styleable.MaterialCustomSwitch_textOn);
            textOff = a.getString(R.styleable.MaterialCustomSwitch_textOff);
            checked = a.getBoolean(R.styleable.MaterialCustomSwitch_checked, false);

            textHeadView.setText(textHead);
            textDescView.setText(checked ? textOn : textOff);
            materialSwitch.setChecked(checked);

        }
    }

    public void setOnCheckChangeListener(OnCheckChangeListener onCheckChangedListener) {
        this.onCheckChangedListener = onCheckChangedListener;
    }

    public void setChecked(boolean checked){
        materialSwitch.setChecked(checked);
    }

    public interface OnCheckChangeListener {
        void onCheckChanged(boolean isChecked);
    }

}