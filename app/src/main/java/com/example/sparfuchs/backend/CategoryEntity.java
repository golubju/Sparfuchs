package com.example.sparfuchs.backend;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String name;
    private String keywords;

    public CategoryEntity(String name, String keywords) {
        this.name = name;
        this.keywords = keywords;

    }

    public int getId() {
        return id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getKeywords() {

        return keywords;
    }
    public void setKeywords(String keywords) {

        this.keywords = keywords;
    }

    public void setId(int id) {
        this.id = id;
    }


}

