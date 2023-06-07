package com.example.test;

import android.content.Context;

public class Utils {

    // converting dp to pixels
    static int pxFromDp(Context context, int dp) {
        return (int)Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}
