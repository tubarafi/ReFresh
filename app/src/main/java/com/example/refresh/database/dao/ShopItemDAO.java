package com.example.refresh.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.refresh.database.model.ShopItem;


@Dao
public interface ShopItemDAO {
    @Query("SELECT * FROM shop_item")
    List<ShopItem> getAll();

    @Query("SELECT * FROM shop_item where name LIKE  :name ")
    ShopItem findByName(String name);

    @Query("SELECT COUNT(*) from shop_item")
    int countTotal();

    @Insert
    long insert(ShopItem item);

    @Update
    void update(ShopItem item);

    @Delete
    void delete(ShopItem item);
}
