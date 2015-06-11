package com.loki.sdk.host;

import android.content.Context;

/**
 * Created by zhangyong on 15-6-3.
 */
public class RootManager {
    private Context context;

    public RootManager(Context context) {
        this.context = context;
    }

    public RootCallback getRootCallback() {
        return new SuRootSolution();
    }
}
