package com.zac4j.yoda.util;

import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * ReactiveX Utilities
 * Created by zac on 3/20/2017.
 */

public class RxUtils {

  public static <T> SingleTransformer<T, T> applySchedulers() {
    return upstream -> upstream.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public static <T> ObservableTransformer<T, T> applyObservableSchedulers() {
    return upstream -> upstream.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  //public static SingleTransformer<Response<Object>, Response<Object>> handleResponse(
  //    final MvpView mvpView) {
  //  return new SingleTransformer<Response<Object>, Response<Object>>() {
  //    @Override public SingleSource<Response<Object>> apply(Single<Response<Object>> upstream) {
  //      return upstream.flatMap(
  //          new Function<Response<Object>, SingleSource<? extends Response<Object>>>() {
  //            @Override public SingleSource<? extends Response<Object>> apply(
  //                @NonNull Response<Object> response) throws Exception {
  //              if (!response.isSuccessful()) {
  //                mvpView.showMainView(false);
  //                String errorBody = response.errorBody().string();
  //                System.out.println("error body: " + errorBody);
  //                if (!TextUtils.isEmpty(errorBody) && errorBody.contains("213")) {
  //                  mvpView.onTokenInvalid();
  //                } else {
  //                  Timber.e(errorBody);
  //                  ObjectMapper mapper = new ObjectMapper();
  //                  Error error = mapper.readValue(errorBody, Error.class);
  //                  mvpView.showError(error.getError());
  //                }
  //              }
  //              return Single.just(response);
  //            }
  //          });
  //    }
  //  };
  //}
}
