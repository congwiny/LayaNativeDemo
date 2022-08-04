package com.ximalaya.ting.android.host.view.lrcview;

import android.util.Log;
import android.view.View;

import com.ximalaya.ting.android.host.model.play.PlayingSoundInfo;

public class Test {

    LrcViewNew lrcViewNew = new LrcViewNew();
    LrcViewNew lrcViewNew2 = new LrcViewNew();


    void test(View view){
        Info info = new Info();
        PlayingSoundInfo info1 = new PlayingSoundInfo();

        String name = LrcEntryParser.getLrcTitle(info1);
        LrcViewNew v = lrcViewNew;
        LrcEntryParser.parseAsync(view.getContext(),v.getLrcEntryList(),name);
    }

    void testIf(){
       if (lrcViewNew!=null&&lrcViewNew2!=null){
            Log.e("Test","ttt");
       }
        Log.e("Test","mmm");

    }

    static class Info{
        String title1="22";
        String title2="11";
    }
}
