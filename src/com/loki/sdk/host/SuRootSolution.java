package com.loki.sdk.host;

import java.io.OutputStream;

/**
 * Created by zhangyong on 15-6-3.
 */
public class SuRootSolution implements RootCallback {
    @Override
    public boolean executeInstaller(String installer, String path) {
        try {
            Process proc = Runtime.getRuntime().exec("su");
            OutputStream output = proc.getOutputStream();
            output.write(String.format("%s %s\n", installer, path).getBytes());
            output.write("exit\n".getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
