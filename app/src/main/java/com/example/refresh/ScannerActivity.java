package com.example.refresh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;
import com.example.refresh.service.FirebaseImageLabelService;
import com.example.refresh.service.FirebaseProcessImageCallback;
import com.example.refresh.service.ImageLabelService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScannerActivity extends AppCompatActivity implements FirebaseProcessImageCallback {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private TextureView textureView;
    private ImageLabelService imageLabelService;
    private FloatingActionButton imageCap;
    private ProgressBar progressBar;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        imageCap = findViewById(R.id.imgCapture);
        progressBar = findViewById(R.id.progressbar);
        db = AppDatabase.getAppDatabase(ScannerActivity.this);

        //Determine use cloud api or on device
        imageLabelService = new FirebaseImageLabelService(true, this);

        textureView = findViewById(R.id.view_finder);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();

        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen
        Rational aspectRatio = new Rational (textureView.getWidth(), textureView.getHeight());

        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                output -> {
                    ViewGroup parent = (ViewGroup) textureView.getParent();
                    parent.removeView(textureView);
                    parent.addView(textureView, 0);

                    textureView.setSurfaceTexture(output.getSurfaceTexture());
                    updateTransform();
                });


        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        imageCap.setOnClickListener(view -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".png");
            imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    progressBar.setVisibility(View.VISIBLE);
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    imageLabelService.processImage(bitmap);
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    String msg = "Pic capture failed : " + message;
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    if (cause != null) {
                        cause.printStackTrace();
                    }
                }
            });

        });

        // bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imgCap);
    }

    private void addFoodItems(List<FirebaseVisionImageLabel> labels) {
        Map<String, Integer> labelMap = labels
                .stream()
                .collect(Collectors.groupingBy(FirebaseVisionImageLabel::getText, Collectors.reducing(0, e -> 1, Integer::sum)));
        List<FoodItem> foodItems = new ArrayList<>();
        for(Map.Entry mapElement : labelMap.entrySet()) {
            foodItems.add(new FoodItem((String) mapElement.getKey(), (new SimpleDateFormat("M/d/yyyy")).format(Calendar.getInstance().getTime()), (int) mapElement.getValue(), ""));
            break;
        }
        try {
            db.foodItemDAO().insertAll(foodItems);
            setResult(foodItems, 3); //create
            Toast.makeText(getApplicationContext(), "Added food items to the fridge.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e("Add Food failed", ex.getMessage() != null ? ex.getMessage() : "");
        }

    }

    private void setResult(List<FoodItem> food, int flag) {
        setResult(flag, new Intent().putExtra("food_items", (Serializable) food));
        finish();
    }

    private void updateTransform(){
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
        addFoodItems(labels);
    }

    @Override
    public void onFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
    }
}
