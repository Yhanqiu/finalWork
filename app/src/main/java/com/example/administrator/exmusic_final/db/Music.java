package com.example.administrator.exmusic_final.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/9/21.
 */

public class Music extends DataSupport{
    private String name;
    private String artist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
