package com.example.administrator.exmusic_final.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/9/21.
 */

public class Music extends DataSupport {
    private String name;
    private String artist;
    private String musicURL;
    private String imageURL;
    private String lrcURL;

    public String getMusicURL() {
        return musicURL;
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getLrcURL() {
        return lrcURL;
    }

    public void setLrcURL(String lrcURL) {
        this.lrcURL = lrcURL;
    }


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
