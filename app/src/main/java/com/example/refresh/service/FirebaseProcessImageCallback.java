package com.example.refresh.service;

import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.util.List;

public interface FirebaseProcessImageCallback {
    void onSuccess(List<FirebaseVisionImageLabel> labels);
    void onFailure(Exception e);
}
