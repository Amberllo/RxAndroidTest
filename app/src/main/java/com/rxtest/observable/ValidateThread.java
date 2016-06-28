package com.rxtest.observable;

import com.google.gson.Gson;

import java.io.Serializable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by LYL on 2016/6/28.
 */
public class ValidateThread {

    public static Observable<LoginEntity> create(){
        return Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            System.out.println("validate call ！"+Thread.currentThread().getName());
                            Thread.sleep(3000);
                            String json = new LoginEntity().fake();
//                            subscriber.onNext(json);
                            subscriber.onNext("连接服务器失败");
                            //throw new Exception("http异常");

                        } catch (Exception e) {
                            e.printStackTrace();
                            subscriber.onError(e);
                        }finally {
                            subscriber.onCompleted();
                        }
                    }
                })
                .map(new Func1<String, LoginEntity>() {
                    @Override
                    public LoginEntity call(String s) {
                        System.out.println("validate 得到json, 正在转换成entity ！"+Thread.currentThread().getName());
                        return new Gson().fromJson(s,LoginEntity.class);
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("validate 开始doOnSubscribe ！"+Thread.currentThread().getName());
                    }
                })

                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("validate 执行完毕doOnCompleted ！"+Thread.currentThread().getName());
                    }
                });
    }
    public static class LoginEntity implements Serializable {
        public String request_id;
        public ResponseParams[] response_params;
        public String fake(){

            request_id = "a08cdead-d337-4837-93a7-248b23a3de0d";
            response_params = new ResponseParams[3];
            ResponseParams params = new ResponseParams();
            params.logisticestatus = "订单已审核【目的地:东台RDC】";
            params.logisticetime = "2016-04-18 11:50:46";
            response_params[0] = params;
            return new Gson().toJson(this);
        }

    }
    public static class ResponseParams implements Serializable{
        public String logisticestatus;
        public String logisticetime;
    }

}
