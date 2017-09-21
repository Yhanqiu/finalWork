package com.example.administrator.exmusic_final;

import com.example.administrator.exmusic_final.db.Music;

/**
 * Created by Administrator on 2017/9/21.
 */

public class Test {

    public static void saveMusic() {
        for (int i = 0;i<15;i++) {
            Music music = new Music();
            music.setName("music"+i);
            music.setArtist("artist"+i);
            music.save();
        }

    }

}
