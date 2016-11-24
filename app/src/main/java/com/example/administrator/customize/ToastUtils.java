package com.example.administrator.customize;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by wangx on 2016/7/2.
 */
public class ToastUtils {

    private static Toast toast;

    public static void showToast(Context context, String text) {
        if (toast == null)
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.setText(text);

        toast.show();
    }
}
