package com.rxtest.observable;

import java.io.Serializable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by LYL on 2016/6/28.
 */
public class CheckVersionThread {
    public static Observable<CheckUpdateEntity> create(){
        return Observable.create(new Observable.OnSubscribe<CheckUpdateEntity>() {
            @Override
            public void call(Subscriber<? super CheckUpdateEntity> subscriber) {
                try {
                    Thread.sleep(2000);
                    subscriber.onNext(new CheckUpdateEntity());
//                    throw new Exception("检查更新失败");
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }finally {
                    subscriber.onCompleted();
                }
            }
        }).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                System.out.println("checkVersionTask 开始执行 ！"+Thread.currentThread().getName());
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                System.out.println("checkVersionTask 执行完毕 ！"+Thread.currentThread().getName());
            }
        });
    }

    public static class CheckUpdateEntity implements Serializable {
        boolean isSuccess = false;
        public boolean isSuccess(){
            return isSuccess;
        }
    }

}
