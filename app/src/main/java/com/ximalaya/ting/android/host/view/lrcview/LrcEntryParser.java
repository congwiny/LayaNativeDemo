package com.ximalaya.ting.android.host.view.lrcview;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ximalaya.ting.android.adsdk.base.util.FileUtil;
import com.ximalaya.ting.android.host.model.play.PlayingSoundInfo;
import com.ximalaya.ting.android.xmutil.thread.CommonThreadPool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LrcEntryParser {
    private static final String TAG = "LrcEntryParser";

    public static LrcParseResult parse(List<LrcEntry> list) {
        LrcParseResult result = new LrcEntryParser.LrcParseResult();
        if (list == null) {
            result.isSuccess = false;
            result.message = "Parse Lrc Failed, LrcEntry list is null";
            return result;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            CharSequence text = list.get(i).getTextInLayout();
            if (text != null) {
                sb.append(text).append("\n");
            }
        }
        result.content = sb.toString();
        result.message = "Parse Lrc success, Lrc size=" + list.size();
        result.isSuccess = true;
        return result;
    }

    public static void parseAsync(Context context, List<LrcEntry> list, String fileName) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                toast(context, "Start parse LrcEntry...");
                LrcParseResult result = parse(list);
                toast(context, result.message);
                if (result.isSuccess && result.content != null) {
                    Log.e(TAG, "Lrc content: " + result.content);
                    String fn;
                    if (fileName == null) {
                        fn = currentTimeStr() + ".txt";
                    } else {
                        fn = fileName + "_" + currentTimeStr() + ".txt";
                    }
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File file = new File(dir, fn);
                    FileUtil.writeStr2File(result.content, file.getAbsolutePath());
                    toast(context, "Write file：" + fn + " to path: " + dir + " success!");
                }
            }
        };
        CommonThreadPool.runTask(runnable);
    }

    public static String currentTimeStr() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        return format.format(date);
    }

    public static void toast(Context context, String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static String getLrcTitle(PlayingSoundInfo info) {
        if (info == null) {
            return "Unknown_Lrc";
        }
        PlayingSoundInfo.AlbumInfo albumInfo = info.albumInfo;
        String title1;
        if (albumInfo == null || albumInfo.title == null) {
            title1 = "Unknown_Album";
        } else {
            title1 = albumInfo.title;
        }
        PlayingSoundInfo.TrackInfo trackInfo = info.trackInfo;
        String title2;
        if (trackInfo == null || trackInfo.title == null) {
            title2 = "Unknown_Track";
        } else {
            title2 = trackInfo.title;
        }
        return title1 + "_" + title2;
    }

    public static class LrcParseResult {
        public boolean isSuccess;
        public String message;
        public String content;
    }
}
