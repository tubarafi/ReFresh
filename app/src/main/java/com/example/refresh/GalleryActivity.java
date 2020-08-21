package com.example.refresh;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;
import com.example.refresh.service.FirebaseImageLabelService;
import com.example.refresh.service.FirebaseProcessImageCallback;
import com.example.refresh.service.ImageLabelService;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GalleryActivity extends AppCompatActivity implements FirebaseProcessImageCallback {

    private ImageLabelService imageLabelService;
    private final int REQUEST_GET_SINGLE_FILE = 102;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //Determine use cloud api or on device
        imageLabelService = new FirebaseImageLabelService(true, this);
        db = AppDatabase.getAppDatabase(GalleryActivity.this);

        if(allPermissionsGranted()){
            pickImage();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void pickImage(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,REQUEST_GET_SINGLE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_SINGLE_FILE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();
            Bitmap imageBitmap = BitmapFactory.decodeFile(imgDecodableString);
            imageLabelService.processImage(imageBitmap);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                pickImage();
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

    @Override
    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
        addFoodItems(labels);
    }

    @Override
    public void onFailure(Exception e) {

    }
}
