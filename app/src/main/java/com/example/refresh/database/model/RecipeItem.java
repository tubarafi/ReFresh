package com.example.refresh.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName="recipe_item")
public class RecipeItem implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    private String sourceURL;

    private String imageURL;

    private String SourceId;

    private String missedIngredients;

    public RecipeItem(String name, String SourceId, String imageURL) {
        this.name = name;
        this.SourceId = SourceId;
        this.imageURL = imageURL;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() { return name; }

    public String getSourceId() { return SourceId; }

    public String getSourceURL() { return sourceURL; }

    public void setSourceURL(String sourceURL) { this.sourceURL = sourceURL; }

    public void setMissedIngredients(String missedIngredients) { this.missedIngredients = missedIngredients; }

    public String getMissedIngredients() { return missedIngredients; }

    public String getImageURL() { return imageURL; }
}
