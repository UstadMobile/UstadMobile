package com.ustadmobile.lib.db.retrofit;

import com.ustadmobile.core.impl.UmCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitUmCallbackAdapter<T> implements Callback<T> {

    private UmCallback<T> callback;

    public RetrofitUmCallbackAdapter(UmCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        callback.onSuccess(response.body());
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        callback.onFailure(t);
    }
}
