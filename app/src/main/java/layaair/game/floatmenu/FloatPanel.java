package layaair.game.floatmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.layabox.game.demo.R;

import layaair.game.browser.ConchJNI;
import layaair.game.browser.LayaWrapper;

/* loaded from: classes.dex */
public class FloatPanel extends FrameLayout {
    private static View.OnClickListener onClick = new View.OnClickListener() { // from class: layaair.game.floatmenu.FloatPanel.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.back_btn) {
                ConchJNI.RunJS("window.conchConfig.JSDebugMode = 1;");
                LayaWrapper.onPopMenu(0);
            } else if (id == R.id.refresh_btn) {
                LayaWrapper.onPopMenu(1);
            }
        }
    };
    private Context mContext;
    private WindowManager.LayoutParams mParams;
    private boolean mShow = false;
    private View mView;
    private WindowManager mWindowManager;

    public FloatPanel(Context context) {
        super(context);
        this.mContext = context;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mView = ((LayoutInflater) context.getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.float_panel, (ViewGroup) null);
        View findViewById = this.mView.findViewById(R.id.float_panel);
        View findViewById2 = findViewById.findViewById(R.id.refresh_btn);
        View findViewById3 = findViewById.findViewById(R.id.back_btn);
        findViewById2.setOnClickListener(onClick);
        findViewById3.setOnClickListener(onClick);
    }

    public void show() {
        this.mParams = new WindowManager.LayoutParams();
        this.mParams.flags = 8;
        this.mParams.gravity = 51;
        this.mParams.format = 1;
        this.mParams.width = -2;
        this.mParams.height = -2;
        this.mView.measure(View.MeasureSpec.makeMeasureSpec(1073741823, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(1073741823, Integer.MIN_VALUE));
        int width = this.mWindowManager.getDefaultDisplay().getWidth();
        this.mWindowManager.getDefaultDisplay().getHeight();
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mParams.x = (width - this.mView.getMeasuredWidth()) - 30;
            this.mParams.y = 30;
        }
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            this.mParams.x = (width - this.mView.getMeasuredWidth()) - 30;
            this.mParams.y = 30;
        }
        try {
            this.mWindowManager.addView(this.mView, this.mParams);
            this.mShow = true;
        } catch (Exception e) {
            Log.d("", ">>>>>>>>>>>>>" + e.toString());
        }
        updatePosition();
    }

    public void hide() {
        if (this.mShow) {
            try {
                this.mWindowManager.removeView(this.mView);
                this.mShow = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void destory() {
        if (this.mShow) {
            this.mWindowManager.removeViewImmediate(this.mView);
        }
    }

    public void updatePosition() {
        if (this.mShow) {
            this.mView.measure(View.MeasureSpec.makeMeasureSpec(1073741823, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(1073741823, Integer.MIN_VALUE));
            int width = this.mWindowManager.getDefaultDisplay().getWidth();
            this.mWindowManager.getDefaultDisplay().getHeight();
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                this.mParams.x = (width - this.mView.getMeasuredWidth()) - 30;
                this.mParams.y = 30;
            }
            if (this.mContext.getResources().getConfiguration().orientation == 1) {
                this.mParams.x = (width - this.mView.getMeasuredWidth()) - 30;
                this.mParams.y = 30;
            }
            this.mWindowManager.updateViewLayout(this.mView, this.mParams);
        }
    }
}
