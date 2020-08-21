package com.example.refresh;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class AddFoodItemActivity extends AppCompatActivity {

    private EditText nameEditText, quantityEditText, remindDateEditText, noteEditText;
    private Button createButton, cancelButton;
    private DatePickerDialog picker;

    private AppDatabase db;
    private FoodItem foodItem;
    private boolean dateSelected = false;
    private boolean update = false;
    private int pos;

//    private SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_item);
        nameEditText = findViewById(R.id.foodNameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        remindDateEditText = findViewById(R.id.remindDateEditText);
        remindDateEditText.setInputType(InputType.TYPE_NULL);
        noteEditText = findViewById(R.id.noteEditText);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);
        db = AppDatabase.getAppDatabase(AddFoodItemActivity.this);
        pos = getIntent().getIntExtra("position", -1);

        if ((foodItem = (FoodItem) getIntent().getSerializableExtra("food_item")) != null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Update Food Item");
            update = true;

            createButton.setText("Update");
            nameEditText.setText(foodItem.getName());
            quantityEditText.setText(String.valueOf(foodItem.getQuantity()));
            remindDateEditText.setText(foodItem.getRemindMeOnDate());
            noteEditText.setText(foodItem.getNote());
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Create Food Item");
        }

        remindDateEditText.setOnClickListener(view -> {
            dateSelected = true;
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            picker = new DatePickerDialog(AddFoodItemActivity.this, (view1, year1, monthOfYear, dayOfMonth) -> remindDateEditText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year1), year, month, day);
            picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            picker.show();
        });

        createButton.setOnClickListener(view -> {
            Context context = getApplicationContext();
            String itemName = nameEditText.getText().toString();
            String itemQuantity = quantityEditText.getText().toString();
            if (itemName.equals("")) {
                Toast.makeText(context, "Please enter an item name.", Toast.LENGTH_LONG).show();
            } else if (itemQuantity.equals("")) {
                Toast.makeText(context, "Please enter a quantity.", Toast.LENGTH_LONG).show();
            } else if (!dateSelected && !update) {
                Toast.makeText(context, "Please enter a date.", Toast.LENGTH_LONG).show();
            } else {
                if (update) {
                    foodItem.setName(nameEditText.getText().toString());
                    foodItem.setQuantity(Integer.parseInt(quantityEditText.getText().toString()));
                    foodItem.setRemindMeOnDate(remindDateEditText.getText().toString());
                    foodItem.setNote(noteEditText.getText().toString());

                    try {
                        db.foodItemDAO().update(foodItem);
                        setResult(foodItem, 2); // update
                        Toast.makeText(context, "Updated " + foodItem.getName() + ".", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Log.e("Update Food failed", ex.getMessage() != null ? ex.getMessage() : "");
                    }
                } else {
                    foodItem = new FoodItem(nameEditText.getText().toString(), remindDateEditText.getText().toString(), Integer.parseInt(quantityEditText.getText().toString()), noteEditText.getText().toString());
                    try {
                        db.foodItemDAO().insert(foodItem);
                        setResult(foodItem, 1); //create
                        String name = foodItem.getName();
                        Toast.makeText(context, "Added " + foodItem.getName() + " to fridge.", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        Log.e("Add Food failed", ex.getMessage() != null ? ex.getMessage() : "");
                    }
                }
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                if (sp.getBoolean("reminder", false)) {
                    scheduleNotification(context, update);
                }
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void setResult(FoodItem food, int flag) {
        setResult(flag, new Intent().putExtra("food_item", food).putExtra("position", pos));
        finish();
    }

    private void scheduleNotification(Context context, Boolean isUpdate) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String[] time = sp.getString("time", "").split(":");

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("remind_expiry", "ReFresh", importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        // Create notification content
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AddFoodItemActivity.this, "remind_expiry")
                .setContentTitle("There's food expiring soon!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Use your " + foodItem.getName() + " by today! Check out some recipes now."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setAutoCancel(true);
        // Build notification with nav intent
        Intent notifyIntent = new Intent(AddFoodItemActivity.this, MainActivity.class);
        notifyIntent.putExtra("menuFragment", "RecipeFragment");
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(AddFoodItemActivity.this, 0, notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        // Create intent to be scheduled
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, foodItem.getId());
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        // Schedule
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isUpdate) { // delete old intent
            alarmManager.cancel(alarmIntent);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("EST"));

        try {
            Date expiryDate = new SimpleDateFormat("MM/dd/yyyy").parse(foodItem.getRemindMeOnDate());
            assert expiryDate != null;
            assert alarmManager != null;
            calendar.setTime(expiryDate);
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        } catch (Exception ex) {
            Log.e("Date conversion for notification failed.", ex.getMessage() != null ? ex.getMessage() : "");
        }
    }
}
