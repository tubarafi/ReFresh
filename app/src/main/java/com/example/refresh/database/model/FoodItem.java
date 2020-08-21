package com.example.refresh.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity(tableName = "food_item")
public class FoodItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name;

    @ColumnInfo(name = "remind_me_on_date")
    private String remindMeOnDate;

    private int quantity;

    private String note;

    public FoodItem(@NonNull String name, String remindMeOnDate, int quantity, String note) {
        this.name = name;
        this.remindMeOnDate = remindMeOnDate;
        this.quantity = quantity;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getRemindMeOnDate() {
        return remindMeOnDate;
    }

    public void setRemindMeOnDate(String remindMeOnDate) {
        this.remindMeOnDate = remindMeOnDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
