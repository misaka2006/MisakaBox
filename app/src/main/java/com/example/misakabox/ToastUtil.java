package com.example.misakabox;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;
    private Context context;
    ToastUtil(Context ctx)
    {
        this.context=ctx;
    }
    public void showText(String msg) {
        if (toast == null) {
            toast = Toast.makeText(this.context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
    public void showText(int msg) {
        if (toast == null) {
            toast = Toast.makeText(this.context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
