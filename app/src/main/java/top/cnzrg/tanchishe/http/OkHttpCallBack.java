package top.cnzrg.tanchishe.http;

import java.io.IOException;

import okhttp3.Call;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/08/19
 */
public interface OkHttpCallBack {

    void onFailure(Call call, IOException e);

    void onSuccess(Call call, String response);
}