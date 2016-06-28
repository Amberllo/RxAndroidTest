package com.rxtest.observable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by LYL on 2016/6/28.
 */
public class OtherThread {

    public static Observable<Integer> create(final String name, final int time,final int progress){
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    System.out.println(name+" 正在执行！thread = "+Thread.currentThread().getName());
                    Thread.sleep(time);
                    //subscriber.onNext(randomResult());
                    subscriber.onNext(progress);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        })
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {  System.out.println(name+" doOnNext ！ "+Thread.currentThread().getName());  }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {  System.out.println(name+" 开始执行 ！ "+Thread.currentThread().getName());    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {  System.out.println(name+" 执行完毕 ！ "+Thread.currentThread().getName());      }
                });
    }

}
