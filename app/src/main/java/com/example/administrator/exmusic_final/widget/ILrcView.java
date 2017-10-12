package com.example.administrator.exmusic_final.widget;

/**
 * Created by Administrator on 2017/10/9.
 */

import java.util.List;

public interface ILrcView {

    void setLrcData(List<LrcRow> lrcRows);//设置lrc数据

    void setLrcViewSeekListener(LrcViewSeekListener seekListener);//滑动监听

    void setLrcViewMessage(String messagetext);//设置无数据显示文字

    void setCurrentTime(long time);

    public interface LrcViewSeekListener{
        void onSeek(LrcRow currentlrcrow, long Currenselectrowtime);
    }
}

