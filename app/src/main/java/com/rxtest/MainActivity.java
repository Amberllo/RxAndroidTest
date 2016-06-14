package com.rxtest;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;

import rx.Observable;
import rx.Scheduler;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    abstract class ActionUi implements Action0 {

        @Override
        public void call() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onCall();
                }
            });
        }

        public abstract void onCall();
    }

    void onHttpEvent(){

        Observable<LoginEntity> loginThread = loginObservable();
        final Observable<CheckUpdateEntity> checkVersionTask = checkUpdateObservable();
        final Observable thread1 = otherThread("thread1",1000);
        final Observable thread2 = otherThread("thread2",3000);
        final Observable thread3 = otherThread("thread3",4000);
        final Observable thread4 = otherThread("thread4",5000);

        loginThread.flatMap(new Func1<LoginEntity, Observable<CheckUpdateEntity>>() {
            @Override
            public Observable<CheckUpdateEntity> call(LoginEntity entity) {
                return checkVersionTask;
            }
        })
        .flatMap(new Func1<CheckUpdateEntity, Observable<?>>() {
            @Override
            public Observable call(CheckUpdateEntity checkUpdateEntity) {
                return Observable.merge(thread1,thread2,thread3,thread4);
            }
        })
        .doOnSubscribe(new ActionUi() {
            @Override
            public void onCall() {
                System.out.println("开始登陆,发起动作,loading...");
            }
        })
        .subscribe(onSuccess, onError);
    }

    Action1 onSuccess = new Action1() {
        @Override
        public void call(Object object) {
            Toast.makeText(MainActivity.this,"登录成功 ",Toast.LENGTH_SHORT).show();
        }
    };

    Action1<Throwable> onError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Toast.makeText(MainActivity.this,"登录异常",Toast.LENGTH_SHORT).show();
        }
    };

    Action0 onComplete = new Action0() {
        @Override
        public void call() {
            System.out.println("结束登陆,dismissLoading...");
        }
    };

    Observable<Boolean> otherThread(final String name,final int time){
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    System.out.println(name+" 正在执行"+name+" ！thread = "+Thread.currentThread().getName());
                    Thread.sleep(time);
                    subscriber.onNext(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
        }).doOnSubscribe(new ActionUi() {
            @Override
            public void onCall() {
                System.out.println(name+" 开始执行 ！");
            }
        }).doOnCompleted(new ActionUi() {
            @Override
            public void onCall() {
                System.out.println(name+" 执行完毕 ！");
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
    }

    Observable<CheckUpdateEntity> checkUpdateObservable(){
        return Observable.create(new Observable.OnSubscribe<CheckUpdateEntity>() {
            @Override
            public void call(Subscriber<? super CheckUpdateEntity> subscriber) {
                try {
                    Thread.sleep(2000);
                    subscriber.onNext(new CheckUpdateEntity());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        }).doOnSubscribe(new Action0() {
            @Override
            public void call() {
//                loading("正在检查更新");
                System.out.println("checkVersionTask 开始执行 ！");
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                System.out.println("checkVersionTask 执行完毕 ！");
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread());
    }

    Observable<LoginEntity> loginObservable(){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    Thread.sleep(3000);
                    String json = new LoginEntity().fake();
//                    subscriber.onNext(json);
//                    subscriber.onNext("连接服务器失败");

                    throw new Exception("http异常");

                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }finally {
                    subscriber.onCompleted();
                }
            }
        }).map(new Func1<String, LoginEntity>() {
            @Override
            public LoginEntity call(String s) {
                return new Gson().fromJson(s,LoginEntity.class);
            }
        }).doOnSubscribe(new ActionUi() {
            @Override
            public void onCall() {
                System.out.println("validate 开始 ！");
//                loading("正在校验，请稍候...");
            }
        }).doOnCompleted(new ActionUi() {
            @Override
            public void onCall() {
                System.out.println("validate 执行完毕 ！");
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
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
    }

    Dialog dialog;

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

}