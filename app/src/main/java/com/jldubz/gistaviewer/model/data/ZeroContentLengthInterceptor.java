package com.jldubz.gistaviewer.model.data;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ZeroContentLengthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        request.headers().newBuilder().set("Content-Length", "0").build();
        return chain.proceed(request);
    }
}
