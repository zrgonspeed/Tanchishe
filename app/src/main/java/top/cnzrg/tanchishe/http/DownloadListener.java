package top.cnzrg.tanchishe.http;

import java.io.File;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/20
 */
public interface DownloadListener {

    /**
     * 下载成功
     * @param file
     */
    void onDownloadSuccess(File file);

    /**
     * 下载进度
     * @param progress
     */
    void onDownLoading(int progress);

    /**
     * 下载异常
     * @param e
     */
    void onDownloadFailed(Exception e);
}