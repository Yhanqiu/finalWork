package com.example.administrator.exmusic_final.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/21.
 */

public class Music extends DataSupport implements Serializable{
    private String queryId;
    private String name;
    private String artist;
    private String musicURL;
    private String imageURL;
    private String lrcURL;

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

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
