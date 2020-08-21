package com.example.refresh.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.refresh.database.model.FoodItem;

@Dao
public interface FoodItemDAO {
    @Query("SELECT * FROM food_item")
    List<FoodItem> getAll();

    @Query("SELECT * FROM food_item where name LIKE  :name ")
    FoodItem findByName(String name);

    @Query("SELECT COUNT(*) from food_item")
    int countTotal();

    @Insert
    long insert(FoodItem item);

    @Insert
    void insertAll(List<FoodItem> items);

    @Update
    void update(FoodItem item);

    @Delete
    void delete(FoodItem item);
}
