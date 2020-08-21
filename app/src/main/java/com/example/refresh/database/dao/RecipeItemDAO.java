package com.example.refresh.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.refresh.database.model.RecipeItem;

@Dao
public interface RecipeItemDAO {
    @Query("SELECT * FROM recipe_item")
    List<RecipeItem> getAll();

    @Query("SELECT * FROM recipe_item where name LIKE  :name ")
    RecipeItem findByName(String name);

    @Query("SELECT COUNT(*) from recipe_item")
    int countTotal();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecipeItem item);

    @Insert
    void insertAll(List<RecipeItem> items);

    @Update
    void update(RecipeItem item);

    @Delete
    void delete(RecipeItem item);
}
