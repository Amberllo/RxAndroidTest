package com.rxtest;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            loading("测试");
                onHttpEvent();
            }
        });

    }

    private Dialog dialog;
    private void loading(String msg){
        dismissLoading();
        dialog = new AlertDialog.Builder(this)
                .setTitle("title")
                .setMessage(msg)
                .create();
        dialog.show();
    }
    private void dismissLoading(){
        if(dialog!=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriber.unsubscribe();
    }

    Subscriber subscriber = new Subscriber() {
        @Override
        public void onCompleted() {
            System.out.println("subscriber onCompleted ");
            dismissLoading();
        }

        @Override
        public void onError(Throwable e) {
            dismissLoading();
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNext(Object o) {
            System.out.println("subscriber onNext "+o);
            Toast.makeText(MainActivity.this,"登录成功 ",Toast.LENGTH_SHORT).show();
        }
    } ;

    void onHttpEvent(){



        final Observable thread1 = otherThread("thread1",2000);
        final Observable thread2 = otherThread("thread2",3000);
        final Observable thread3 = otherThread("thread3",1000);
        final Observable thread4 = otherThread("thread4",5000);
        final Observable validate = validateObservable();
        final Observable checkVersion = checkUpdateObservable();

        loading("开始登录");
        //主逻辑
        validate
                .flatMap(new Func1<LoginEntity, Observable<CheckUpdateEntity>>() {
                    @Override
                    public Observable<CheckUpdateEntity> call(LoginEntity entity) {
                        System.out.println("validate 结束，flatMap to checkVersion ！"+Thread.currentThread().getName());
                        loading("校验成功，正在检查更新...");
                        return checkVersion;
                    }
                })
                .flatMap(new Func1<CheckUpdateEntity, Observable<?>>() {
                    @Override
                    public Observable call(CheckUpdateEntity checkUpdateEntity) {
                        System.out.println("checkVersion 结束，flatMap to OtherThread ！"+Thread.currentThread().getName());
                        loading("应用已经是最新版本，正在加载基础数据...");
                        return Observable.merge(thread1,thread2,thread3,thread4);
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {  System.out.println("登录取消！" +Thread.currentThread().getName());     }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() { System.out.println("登录开始！" +Thread.currentThread().getName());     }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() { System.out.println("登录结束！"+Thread.currentThread().getName());      }
                })
                .subscribe(subscriber);
    }

    Observable<LoginEntity> validateObservable(){
        return Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            System.out.println("validate call ！"+Thread.currentThread().getName());
                            Thread.sleep(3000);
                            String json = new LoginEntity().fake();
                            subscriber.onNext(json);
        //                    subscriber.onNext("连接服务器失败");
        //                    throw new Exception("http异常");

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
                 })
                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    Observable<Boolean> otherThread(final String name,final int time){
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    System.out.println(name+" 正在执行！thread = "+Thread.currentThread().getName());
                    Thread.sleep(time);
                    subscriber.onNext(true);
//                    int count = new Random().nextInt(10);
//                    if(count>5){
//                        subscriber.onNext(true);
//                    }else{
//                        throw new Exception(name+" 执行失败！thread = "+Thread.currentThread().getName());
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {  System.out.println(name+" 开始执行 ！ "+Thread.currentThread().getName());    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {  System.out.println(name+" 执行完毕 ！ "+Thread.currentThread().getName());      }
                })
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(AndroidSchedulers.mainThread());
    }

    Observable<CheckUpdateEntity> checkUpdateObservable(){
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
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
    }


    public class LoginEntity implements Serializable{
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
    public class ResponseParams implements Serializable{
        public String logisticestatus;
        public String logisticetime;
    }
    public class CheckUpdateEntity implements Serializable{
        boolean isSuccess = false;
        public boolean isSuccess(){
            return isSuccess;
        }
    }


}