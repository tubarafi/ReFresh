package com.example.refresh.service;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.util.List;

public class FirebaseImageLabelService implements ImageLabelService {

    private FirebaseVisionCloudImageLabelerOptions cloudOptions;
    private FirebaseVisionOnDeviceImageLabelerOptions onDeviceOptions;
    private boolean isCloud = false;
    private FirebaseProcessImageCallback callback;

    public FirebaseImageLabelService(boolean isCloud, FirebaseProcessImageCallback callback) {
        this.cloudOptions = new FirebaseVisionCloudImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.85f)
                .build();
        this.onDeviceOptions = new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.85f)
                .build();
        this.isCloud = isCloud;
        this.callback = callback;
    }

    @Override
    public void processImage(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        getLabeler().processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        callback.onSuccess(labels);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }


    private FirebaseVisionImageLabeler getLabeler(){
        return isCloud ? FirebaseVision.getInstance()
                .getCloudImageLabeler(cloudOptions) : FirebaseVision.getInstance().getOnDeviceImageLabeler(onDeviceOptions);
    }
}
