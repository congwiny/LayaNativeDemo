package layaair.game.browser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.AbsoluteLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import layaair.game.conch.ILayaEventListener;
import layaair.game.conch.LayaConch5;
import layaair.game.config.config;
import layaair.game.wrapper.ILayaLibWrapper;

@SuppressLint({"Wakelock"})
/* loaded from: classes.dex */
public class LayaWrapper implements ILayaLibWrapper {
    public static final int AR_CHECK_UPDATE = 1;
    public static final int AR_INIT_PLATFORM = 2;
    public static final int BACK_TO_MAIN = 0;
    public static final int CLOSE_BIG = 2;
    public static final int REFRESH = 1;
    private static Toast mToast;
    public static Activity m_LayaEngineContext;
    private static int m_nStartActivityType;
    public static AbsoluteLayout m_pAbsEditLayout;
    public static LayaConch5 m_pEngine;
    static LayaWrapper ms_layaEngine;
    private static Activity ms_layaMainActivity;
    public static Context ms_mCtx;
    public boolean m_bPopAD = true;
    private long m_nBackPressTime = 0;
    public View m_pExternalLoadingView = null;
    public SensorManager m_pSensorManager = null;
    public Sensor m_pSensor = null;
    public SensorEventListener m_pSensorListener = null;
    public int SENSOR_OFFSET = 6;
    public double m_fSensorX = 0.0d;
    public double m_fSensorY = 0.0d;
    public double m_fSensorZ = 0.0d;
    public int m_nScreenOrientation = 6;
    private String tempSoPath = "";
    private String tempSoFile = "";

    public static LayaWrapper GetInstance() {
        if (ms_layaEngine == null) {
            ms_layaEngine = new LayaWrapper();
        }
        return ms_layaEngine;
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setLayaEventListener(ILayaEventListener iLayaEventListener) {
        if (m_pEngine != null) {
            m_pEngine.setLayaEventListener(iLayaEventListener);
        }
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void initEngine(Activity activity) {
        ms_layaMainActivity = activity;
        ms_layaEngine = this;
        m_LayaEngineContext = activity;
        ms_mCtx = m_LayaEngineContext;
        m_pEngine = new LayaConch5(ms_mCtx);
        m_pEngine.game_conch3_SetIsPlug(false);
        if (this.tempSoPath.length() > 0) {
            m_pEngine.setSoPath(this.tempSoPath);
        }
        if (this.tempSoFile.length() > 0) {
            m_pEngine.setSoPath(this.tempSoFile);
        }
    }

    public void setSoPath(String str) {
        this.tempSoPath = str;
        if (m_pEngine != null) {
            m_pEngine.setSoPath(str);
        }
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setAlertTitle(String str) {
        if (m_pEngine != null) {
            m_pEngine.setAlertTitle(str);
        }
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setStringOnBackPressed(String str) {
        if (m_pEngine != null) {
            m_pEngine.setStringOnBackPressed(str);
        }
    }

    public void setSoFile(String str) {
        this.tempSoFile = str;
        if (m_pEngine != null) {
            m_pEngine.setSoFile(str);
        }
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setGameUrl(String str) {
        m_pEngine.setGameUrl(str);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setLocalizable(boolean z) {
        m_pEngine.setLocalizable(z);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void startGame() {
        EngineStart();
    }

    public String getCacheDir() {
        String[] split = m_LayaEngineContext.getCacheDir().toString().split("/");
        String str = "";
        for (int i = 0; i < split.length - 1; i++) {
            str = (str + split[i]) + "/";
        }
        return str;
    }

    public void EngineStart() {
        String cacheDir = getCacheDir();
        String str = cacheDir + "/LayaCache";
        File file = new File(str);
        if (!file.exists()) {
            file.mkdir();
        }
        AssetManager layaApplicationAsset = getLayaApplicationAsset();
        m_pEngine.game_conch3_setAppWorkPath(cacheDir);
        m_pEngine.game_conch3_setAssetInfo(layaApplicationAsset);
        m_pEngine.game_conch3_init();
        File file2 = new File(str + "/localstorage");
        if (!file2.exists() && !file2.mkdirs()) {
            Log.e("", "创建localstorage目录失败！");
        }
        ConchJNI.SetLocalStoragePath(str + "/localstorage");
        if (PermisionUtils.checkExternalStoragePermission(ms_layaMainActivity)) {
            checkApkUpdate();
        }
    }

    public boolean isOpenNetwork(Context context) {
        if (!config.GetInstance().m_bCheckNetwork) {
            return true;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void settingNetwork(final Context context, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("连接失败，请检查网络或与开发商联系").setMessage("是否对网络进行设置?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() { // from class: layaair.game.browser.LayaWrapper.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                Intent intent;
                try {
                    if (Integer.valueOf(Build.VERSION.SDK).intValue() > 10) {
                        intent = new Intent("android.settings.WIRELESS_SETTINGS");
                    } else {
                        intent = new Intent();
                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.WirelessSettings"));
                        intent.setAction("android.intent.action.VIEW");
                    }
                    ((Activity) context).startActivityForResult(intent, i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() { // from class: layaair.game.browser.LayaWrapper.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.cancel();
                ((Activity) context).finish();
            }
        });
        AlertDialog create = builder.create();
        create.setCanceledOnTouchOutside(false);
        create.show();
    }

    public void checkApkUpdate(Context context, ValueCallback<Integer> valueCallback) {
        if (isOpenNetwork(context)) {
            valueCallback.onReceiveValue(1);
        } else {
            settingNetwork(context, 1);
        }
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void checkApkUpdate() {
        checkApkUpdate(m_LayaEngineContext, new ValueCallback<Integer>() { // from class: layaair.game.browser.LayaWrapper.3
            public void onReceiveValue(Integer num) {
                if (num.intValue() == 1) {
                    LayaWrapper.this.InitView();
                } else {
                    LayaWrapper.m_LayaEngineContext.finish();
                }
            }
        });
    }

    @TargetApi(11)
    public void InitView() {
        View game_conch3_get_view = m_pEngine.game_conch3_get_view();
        if (Build.VERSION.SDK_INT >= 11) {
            game_conch3_get_view.setSystemUiVisibility(4);
        }
        m_LayaEngineContext.setContentView(game_conch3_get_view);
    }

    public static String getAppVersionName() {
        String str;
        Exception e;
        PackageInfo packageInfo = null;
        try {
            packageInfo = ms_mCtx.getPackageManager().getPackageInfo(ms_mCtx.getPackageName(), 0);
            str = packageInfo.versionName;
        } catch (Exception e2) {
            e = e2;
            str = "";
        }
        try {
            int i = packageInfo.versionCode;
        } catch (Exception e3) {
            e = e3;
            Log.e("VersionInfo", "Exception", e);
        }
        return str != null ? str.length() <= 0 ? "" : str : "";
    }

    public void CopyFileFromAssets(String str, String str2) {
        File file = new File(str2);
        if (!file.exists()) {
            Log.i("", "mkdir:" + str2);
            if (!file.mkdirs()) {
                Log.e("", "copyasserts error： 创建文件夹出错，dir=" + str2);
            }
        }
        try {
            File file2 = new File(file, str);
            try {
                InputStream open = m_LayaEngineContext.getAssets().open(str);
                Log.i("", "copy file: " + str);
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = open.read(bArr);
                    if (read > 0) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        open.close();
                        fileOutputStream.close();
                        Log.e("", "拷贝文件" + str + "成功");
                        return;
                    }
                }
            } catch (IOException unused) {
                Log.e("", "open file err:" + str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InitSensor() {
        this.m_pSensorManager = (SensorManager) m_LayaEngineContext.getSystemService("sensor");
        this.m_pSensor = this.m_pSensorManager.getDefaultSensor(1);
        this.m_pSensorListener = new SensorEventListener() { // from class: layaair.game.browser.LayaWrapper.4
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int i) {
            }

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent sensorEvent) {
                double d = sensorEvent.values[0];
                double d2 = sensorEvent.values[1];
                double d3 = sensorEvent.values[2];
                if (Math.abs(LayaWrapper.this.m_fSensorX - d) > 0.1d || Math.abs(LayaWrapper.this.m_fSensorY - d2) > 0.1d || Math.abs(LayaWrapper.this.m_fSensorZ - d3) > 0.1d) {
                    LayaWrapper.this.m_fSensorX = d;
                    LayaWrapper.this.m_fSensorY = d2;
                    LayaWrapper.this.m_fSensorZ = d3;
                    if (d < LayaWrapper.this.SENSOR_OFFSET - 1 || d > LayaWrapper.this.SENSOR_OFFSET + 1 || Math.abs(d2) >= 2.0d) {
                        double atan2 = Math.atan2(d2, d - LayaWrapper.this.SENSOR_OFFSET) - 1.5707963267948966d;
                        if (atan2 < 0.0d) {
                            atan2 += 6.283185307179586d;
                        }
                        ConchJNI.onSensorChanged((float) atan2);
                        return;
                    }
                    ConchJNI.onSensorChanged(-1.0f);
                }
            }
        };
        this.m_pSensorManager.registerListener(this.m_pSensorListener, this.m_pSensor, 1);
    }

    public void handleUncaughtException(Thread thread, Throwable th) {
        th.printStackTrace();
        Intent intent = new Intent();
        intent.setAction("com.dawawa.SEND_LOG");
        intent.setFlags(268435456);
        m_LayaEngineContext.startActivity(intent);
        System.exit(1);
    }

    public static void onPopMenu(int i) {
        switch (i) {
            case 0:
                ConchJNI.onRunCmd(4461, -1, 0);
                return;
            case 1:
                ConchJNI.onRunCmd(4459, 0, 0);
                return;
            case 2:
            default:
                return;
        }
    }

    public void PlatformInitOK(int i) {
        Log.e("0", "==============Java流程 InitMainCanvas()");
        InitView();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 2) {
            InitView();
        } else if (i == 1) {
            checkApkUpdate();
        }
        m_pEngine.onActivityResult(i, i2, intent);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setResolution(int i, int i2) {
        m_pEngine.setResolution(i, i2);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void _enableOnLayout(boolean z) {
        m_pEngine._enableOnLayout(z);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onPause() {
        if (this.m_pSensorManager != null) {
            this.m_pSensorManager.unregisterListener(this.m_pSensorListener);
        }
        m_pEngine.game_conch3_onPause();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onResume() {
        if (this.m_pSensorManager != null) {
            this.m_pSensorManager.registerListener(this.m_pSensorListener, this.m_pSensor, 1);
        }
        m_pEngine.game_conch3_onResume();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onDestroy() {
        m_pEngine.onDestroy();
        DelInstance();
    }

    private static void DelInstance() {
        m_pEngine = null;
        m_LayaEngineContext = null;
        ms_layaEngine = null;
        ms_layaMainActivity = null;
        ms_mCtx = null;
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onStop() {
        m_pEngine.onStop();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onNewIntent(Intent intent) {
        m_pEngine.onNewIntent(intent);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void onRestart() {
        m_pEngine.onRestart();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setInterceptKey(boolean z) {
        m_pEngine.setInterceptKey(z);
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setOptions(HashMap<String, Object> hashMap) {
        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Log.i("setOption", "setOptions() key=" + key + " value=" + entry.getValue());
            if (key.compareToIgnoreCase("url") == 0) {
                m_pEngine.setGameUrl((String) entry.getValue());
            } else if (key.compareToIgnoreCase("sopath") == 0) {
                setSoPath((String) entry.getValue());
            } else if (key.compareToIgnoreCase("sofile") == 0) {
                setSoFile((String) entry.getValue());
            }
        }
    }

    public static boolean IsFinishing(Activity activity) {
        return activity == null || activity.isFinishing();
    }

    public static void ShowMessage(String str, LayaConch5 layaConch5) {
        layaConch5.showMessage(str);
    }

    public static int GetScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getLayaApplicationActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int GetScreenHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getLayaApplicationActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static float GetScreenInch() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getLayaApplicationActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        return (float) (Math.sqrt((i * i) + (i2 * i2)) / displayMetrics.densityDpi);
    }

    public Bitmap getResImage(String str) {
        return BitmapFactory.decodeResource(m_LayaEngineContext.getResources(), m_LayaEngineContext.getResources().getIdentifier(str, "drawable", m_LayaEngineContext.getApplicationInfo().packageName));
    }

    public static void reloadApp() {
        ConchJNI.reloadJS();
    }

    public void restartApp() {
        ((AlarmManager) m_LayaEngineContext.getSystemService("alarm")).set(1, System.currentTimeMillis() + 1000, PendingIntent.getActivity(m_LayaEngineContext.getApplicationContext(), 0, new Intent(m_LayaEngineContext.getApplicationContext(), LayaWrapper.class), 268435456));
        m_LayaEngineContext.finishActivity(0);
    }

    public static void MyJSAlert(String str, String str2, int i, LayaConch5 layaConch5) {
        if (layaConch5 != null) {
            layaConch5.alertJS(str, str2, i);
        }
    }

    public static void setScreenWakeLock(boolean z) {
        if (getLayaApplicationActivity() != null) {
            if (z) {
                getLayaApplicationActivity().getWindow().addFlags(128);
            } else {
                getLayaApplicationActivity().getWindow().clearFlags(128);
            }
        }
    }

    public static Context getLayaApplicationContext() {
        return ms_mCtx;
    }

    public static Activity getLayaApplicationActivity() {
        return ms_layaMainActivity;
    }

    public static AssetManager getLayaApplicationAsset() {
        return ms_layaMainActivity.getAssets();
    }

    @Override // layaair.game.wrapper.ILayaLibWrapper
    public void setLoadingView(View view) {
        this.m_pExternalLoadingView = view;
    }
}
