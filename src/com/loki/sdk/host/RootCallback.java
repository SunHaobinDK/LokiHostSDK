package com.loki.sdk.host;

/**
 * 自定义Root回调方案
 */
public interface RootCallback {
    /**
     * Root回调方案需实现此方法
     * @param installer 安装器完整文件名
     * @param path 安装器所在目录
     * @return 安装器是否成功执行
     */
    public boolean executeInstaller(String installer, String path);
}
