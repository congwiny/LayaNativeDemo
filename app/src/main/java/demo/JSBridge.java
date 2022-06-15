package demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import layaair.game.browser.ConchJNI;
import layaair.game.browser.ExportJavaFunction;
import layaair.game.browser.ScanActivity;


/* loaded from: classes.dex */
public class JSBridge {
    public static Activity mMainActivity;
    public static Handler m_Handler = new Handler(Looper.getMainLooper());

    public static void hideSplash() {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.1
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.mSplashDialog.dismissSplash();
            }
        });
    }

    public static void setFontColor(final String str) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.2
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.mSplashDialog.setFontColor(Color.parseColor(str));
            }
        });
    }

    public static void setTips(final JSONArray jSONArray) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    String[] strArr = new String[jSONArray.length()];
                    for (int i = 0; i < jSONArray.length(); i++) {
                        strArr[i] = jSONArray.getString(i);
                    }
                    MainActivity.mSplashDialog.setTips(strArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void bgColor(final String str) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.4
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.mSplashDialog.setBackgroundColor(Color.parseColor(str));
            }
        });
    }

    public static void loading(final double d) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.5
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.mSplashDialog.setPercent((int) d);
            }
        });
    }

    public static void showTextInfo(final boolean z) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.6
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.mSplashDialog.showTextInfo(z);
            }
        });
    }

    public static void showFloatPanel(final boolean z) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.7
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.m_FloatPanel == null) {
                    return;
                }
                if (z) {
                    MainActivity.m_FloatPanel.show();
                } else {
                    MainActivity.m_FloatPanel.hide();
                }
            }
        });
    }

    public static void showScanner(final boolean z) {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.8
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.m_FloatPanel == null) {
                    return;
                }
                if (z) {
                    new IntentIntegrator(JSBridge.mMainActivity).setCaptureActivity(ScanActivity.class).initiateScan();
                } else if (ScanActivity.m_instance != null) {
                    ScanActivity.m_instance.finish();
                }
            }
        });
    }

    public static void getIP() {
        m_Handler.post(new Runnable() { // from class: layaair.game.browser.JSBridge.9
            @Override // java.lang.Runnable
            public void run() {
                ExportJavaFunction.CallBackToJS(JSBridge.class, "getIP", JSBridge.getIP(JSBridge.mMainActivity));
            }
        });
    }

    public static void onOrientationChange() {
        MainActivity.m_FloatPanel.updatePosition();
    }

    public static void onScanResult(String str) {
        Log.d("JSBridge", "url " + str);
        if (ScanActivity.m_instance != null) {
            ScanActivity.m_instance.finish();
        }
        ConchJNI.RunJS(("script.UIController.instance.onScanResult('" + str) + "');");
    }

    public static String getIP(Context context) {
        String str;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress nextElement = inetAddresses.nextElement();
                    if (!nextElement.isLoopbackAddress() && (nextElement instanceof Inet4Address)) {
                        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                        if (activeNetworkInfo.getType() == 1) {
                            str = "WifiNetworkIP: ";
                        } else if (activeNetworkInfo.getType() == 0) {
                            str = "MobileNetworkIP: ";
                        } else {
                            str = "UnknowNetworkIP: ";
                        }
                        return str + nextElement.getHostAddress().toString();
                    }
                }
            }
            return "";
        } catch (SocketException e) {
            e.printStackTrace();
            return "";
        }
    }
}
