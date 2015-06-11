package com.loki.sdk.host;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loki安装器
 */
public class LokiInstaller {

    private static final String PACKAGE = "package";
    private static final String INSTALLER = Build.VERSION.SDK_INT >= 21 ? "installer-pie" : "installer";
    private static final String VERSION = "version";

    private Context context;
    private File extractDir;
    private RootManager rootManager;

    /**
     * 默认构造函数
     * @param context attached Context
     */
    public LokiInstaller(Context context) {
        this.context = context;
        this.extractDir = context.getDir("loki_ota", Context.MODE_PRIVATE);
        this.rootManager = new RootManager(context);
    }

    /**
     * 安装Loki OTA Package，此函数会block当前线程，因此请勿从UI线程中调用
     * @param localPackage    本地OTA package，通常为AssetManager.open后得到的InputStream，null则完全从网络下载
     * @param localVersion    本地OTA package版本
     * @param localChannel    本地OTA package的渠道号
     * @param enableUpdate    安装时是否自动从网络检测新版本
     * @return true表示安装成功，false表示安装失败
     */
    public boolean install(InputStream localPackage, int localVersion, String localChannel, boolean enableUpdate) {
        boolean extracted = false;
        if (enableUpdate) {
            try {
                String url = checkForUpgrade(localVersion, localChannel);
                if (url != null) {
                    InputStream input = getUpgrade(url);
                    extractUpgrade(input);
                    input.close();
                    extracted = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!extracted && localPackage != null) {
            try {
                extractUpgrade(localPackage);
                localPackage.close();
                extracted = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!extracted) {
            return false;
        }

        File targetInstaller = new File(extractDir, "installer");
        return rootManager.getRootCallback().executeInstaller(targetInstaller.getAbsolutePath(), extractDir.getAbsolutePath());
    }

    private String checkForUpgrade(int version, String channel) {
        // TODO query cloud for upgrade
        return null;
    }

    private InputStream getUpgrade(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.connect();
        return conn.getInputStream();
    }

    private void extractUpgrade(InputStream input) throws IOException {
        extractDir.mkdirs();

        ZipInputStream zis = new ZipInputStream(input);
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.startsWith("META-INF") || entry.isDirectory()) {
                continue;
            }

            if (PACKAGE.equals(name) || VERSION.equals(name) || INSTALLER.equals(name)) {
                File o = new File(extractDir, name);
                o.delete();
                OutputStream output = new FileOutputStream(o);

                copyStream(zis, output);
                output.close();

                FileUtils.setPermissions(o, 00755);
            }
        }
        zis.close();

        File extractedInstaller = new File(extractDir, INSTALLER);
        File targetInstaller = new File(extractDir, "installer");

        if (!extractedInstaller.equals(targetInstaller)) {
            extractedInstaller.renameTo(targetInstaller);
        }

    }

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte buffer[] = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }
}
