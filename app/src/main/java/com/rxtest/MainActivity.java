package com.rxtest;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.rxtest.observable.CheckVersionThread;
import com.rxtest.observable.OtherThread;
import com.rxtest.observable.ValidateThread;
import java.util.Random;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    CircularProgressButton button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        button = (CircularProgressButton) findViewById(R.id.circularButton1);
       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG) .setAction("Action", null).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHttpEvent();
            }
        });

    }

    void onHttpEvent(){

        //主逻辑
        ValidateThread.create().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread())

                .flatMap(new Func1<ValidateThread.LoginEntity, Observable<CheckVersionThread.CheckUpdateEntity>>() {
                    @Override
                    public Observable<CheckVersionThread.CheckUpdateEntity> call(ValidateThread.LoginEntity entity) {
                        System.out.println("validate 结束，flatMap to checkVersion ！"+Thread.currentThread().getName());
//                        loading("校验成功，正在检查更新...");
                        simulateSuccessProgress(button,button.getProgress(),button.getProgress()+20);
                        return CheckVersionThread.create().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());

                    }
                })
                .flatMap(new Func1<CheckVersionThread.CheckUpdateEntity, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(CheckVersionThread.CheckUpdateEntity checkUpdateEntity) {
                        System.out.println("checkVersion 结束，flatMap to OtherThread ！"+Thread.currentThread().getName());
//                        loading("应用已经是最新版本，正在加载基础数据...");
                        simulateSuccessProgress(button,button.getProgress(),button.getProgress()+20);
                        return Observable.merge(
                                OtherThread.create("thread1",2000,15).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()),
                                OtherThread.create("thread2",3000,15).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()),
                                OtherThread.create("thread3",1000,15).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread()),
                                OtherThread.create("thread4",5000,15)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {  System.out.println("登录取消！" +Thread.currentThread().getName());     }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("登录开始！" +Thread.currentThread().getName());
                        button.setProgress(0);
                        button.setProgress(1);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("登录结束！"+Thread.currentThread().getName());
                        button.setProgress(100);
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        simulateSuccessProgress(button,button.getProgress(),button.getProgress()+integer);

                        System.out.println("subscriber onNext ");

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        button.setErrorText("登录失败 " + e.getMessage());
                        button.setProgress(-1);
                        Toast.makeText(MainActivity.this, "登录失败 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, new Action0() {
                    @Override
                    public void call() {

                        System.out.println("subscriber onCompleted ");
                        button.setCompleteText("登录成功");
                        Toast.makeText(MainActivity.this, "登录成功 ", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void simulateSuccessProgress(final CircularProgressButton button,int start,int end) {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(start, end);
        widthAnimation.setDuration(300);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
            }
        });
        widthAnimation.start();
    }




    boolean randomResult(){
        int count = new Random().nextInt(10);
        return count>5;
    }
}