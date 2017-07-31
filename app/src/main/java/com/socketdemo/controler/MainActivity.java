package com.socketdemo.controler;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.socketdemo.R;
import com.websocketclient.OnWebSocketListener;
import com.websocketclient.WebSocketClient;
import com.websocketclient.WebSocketMessage;


/*
 * Created by JiangJie on 2017/2/13.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnWebSocketListener {

    private TextView tv;
    private Button btn;
    private Button btn2;
    private Button btn3;
    private WebSocketClient client;
    private Gson gson = new Gson();
    private String notice1 = "delivery man: request for your door open \nyour package sent from Seattle \nreach your house";
    private String notice2 = "Your door is closed by delivery man";
    private int category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        client = new WebSocketClient(this, "ws://10.78.116.122:8080/phone/connect");
        client = new WebSocketClient(this, "ws://115.159.225.195:18080/phone/connect");

        new Handler().postDelayed(new Runnable(){
            public void run() {
                //execute the task
                client.connect();
            }
        }, 1000);

        initViews();
    }

    private void initViews() {
//        tv = (TextView) findViewById(R.id.textView);
//        btn = (Button) findViewById(R.id.button);
//        btn2 = (Button) findViewById(R.id.button2);
//        btn3 = (Button) findViewById(R.id.button3);
//        btn.setOnClickListener(this);
//        btn2.setOnClickListener(this);
//        btn3.setOnClickListener(this);
        client.setOnWebSocketListener(this);
    }


    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.button) {
//            client.connect();
//        }
//        if (v.getId() == R.id.button2) {
//            client.disconnect();
//        }
//        if (v.getId() == R.id.button3) {
//            WebSocketMessage message = new WebSocketMessage("Hello Jesse!");
//            String jsonStr = gson.toJson(message);
//            client.sendMessage(jsonStr);
//        }
    }

    @Override
    public void onResponse(String response) {
        //tv.setText(response);
        Log.i("Websocket", response);
        if(response.contains("1000")) {
            showNormalDialog(notice1, "Accept", "Ignore");
            category = 1;
        }else if(response.contains("1100")) {
            showNormalDialog(notice2, "Ok", "Cancel");
            category = 2;
        }

    }

    @Override
    public void onConnected() {
        Toast.makeText(this, "Websocket连接了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectError(String e) {
        Toast.makeText(this, "Websocket连接出错了：" + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisConnected() {
        Toast.makeText(this, "Websocket断开连接了", Toast.LENGTH_SHORT).show();
    }

    private void showNormalDialog(String message, String option1, String option2){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle("SMART LOCK");
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton(option1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        if(category == 2) return;
                        WebSocketMessage message = new WebSocketMessage("2000");
                        String jsonStr = gson.toJson(message);
                        client.sendMessage(jsonStr);
                    }
                });
        normalDialog.setNegativeButton(option2,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        if(category == 2) return;
                        WebSocketMessage message = new WebSocketMessage("3000");
                        String jsonStr = gson.toJson(message);
                        client.sendMessage(jsonStr);
                    }
                });
        // 显示
        normalDialog.show();
    }
}
