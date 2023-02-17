package com.dtp.example.adapter.okhttp3;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Okhttp3Service related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
@Service
public class Okhttp3Service {

    @Resource
    private OkHttpClient okHttpClient;

    public void call(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
                log.error("okhttp3 async error", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle the response
                log.info("okhttp3 async result: {}", new String(response.body().bytes()));
            }
        });
    }
}
