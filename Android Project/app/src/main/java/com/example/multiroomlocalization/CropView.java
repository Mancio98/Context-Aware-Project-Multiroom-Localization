package com.example.multiroomlocalization;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.canhub.cropper.CropImageView;

public class CropView extends Dialog {
    private final Uri inputImage;
    private Button salva;
    //private Uri outputImage;
    private CropImageView cropImageView;

    public CropView(@NonNull Context context,Uri inputImage) {
        super(context);
        this.inputImage = inputImage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crop_view);
        cropImageView = findViewById(R.id.cropImageView);
        salva = findViewById(R.id.salvaCrop);
        cropImageView.setImageUriAsync(inputImage);
        cropImageView.setAspectRatio(1440, 2308);
    }

    protected void setTouchButton(View.OnTouchListener touch){
        salva.setOnTouchListener(touch);
    }
    protected CropImageView getCropImageView(){
        return this.cropImageView;
    }

}
