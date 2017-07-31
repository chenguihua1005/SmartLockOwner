package com.socketclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.socketclient.SocketConfig.MESSAGE_ACTION;
import static com.socketclient.SocketConfig.SOCKET_STATE_CONNECTED;
import static com.socketclient.SocketConfig.SOCKET_STATE_DISCONNECTED;

/*
 * Created by JiangJie on 2017/2/13.
 */

public class SocketClient {

    private Context context;
    private ISocketService iSocketService;
    private OnSocketListener onSocketListener;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 未连接为空
            iSocketService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 已连接
            iSocketService = ISocketService.Stub.asInterface(service);
        }
    };

    public SocketClient(Context context) {
        this.context = context;
        context.bindService(new Intent(this.context, SocketClientService.class), conn, BIND_AUTO_CREATE);
    }

    public void startSocket() throws RemoteException {
        if (iSocketService != null) {
            registerReceiver();
            iSocketService.startSocket();
        } else {
            if (onSocketListener != null) {
                onSocketListener.onDisConnected();
            }
        }
    }

    public void stopSocket() throws RemoteException {
        if (iSocketService != null) {
            unRegisterReceiver();
            iSocketService.stopSocket();
        } else {
            if (onSocketListener != null) {
                onSocketListener.onDisConnected();
            }
        }
    }

    public void sendMessage(String msg) throws RemoteException {
        if (iSocketService != null) {
            iSocketService.sendMessage(msg);
        } else {
            if (onSocketListener != null) {
                onSocketListener.onDisConnected();
            }
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_ACTION);
        intentFilter.addAction(SOCKET_STATE_CONNECTED);
        intentFilter.addAction(SOCKET_STATE_DISCONNECTED);
        context.registerReceiver(mReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        context.unregisterReceiver(mReceiver);
        // 注销服务
        context.unbindService(conn);
    }

    public void setOnSocketListener(OnSocketListener onSocketListener) {
        this.onSocketListener = onSocketListener;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MESSAGE_ACTION)) {
                String stringExtra = intent.getStringExtra("message");
                if (onSocketListener != null) {
                    onSocketListener.onResponse(stringExtra);
                }
            }
            if (action.equals(SOCKET_STATE_CONNECTED)) {
                if (onSocketListener != null) {
                    onSocketListener.onConnected();
                }
            }
            if (action.equals(SOCKET_STATE_DISCONNECTED)) {
                if (onSocketListener != null) {
                    onSocketListener.onDisConnected();
                }
            }
        }
    };

    public interface OnSocketListener {
        void onResponse(String response);

        void onConnected();

        void onDisConnected();
    }
}
