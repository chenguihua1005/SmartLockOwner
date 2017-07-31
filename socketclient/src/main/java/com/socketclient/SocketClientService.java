package com.socketclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Arrays;
import static com.socketclient.SocketConfig.HEART_BEAT_MESSAGE;
import static com.socketclient.SocketConfig.HEART_BEAT_RATE;
import static com.socketclient.SocketConfig.HOST;
import static com.socketclient.SocketConfig.MESSAGE_ACTION;
import static com.socketclient.SocketConfig.PORT;
import static com.socketclient.SocketConfig.REST_CONNECT_TIME;
import static com.socketclient.SocketConfig.SOCKET_STATE_CONNECTED;
import static com.socketclient.SocketConfig.SOCKET_STATE_DISCONNECTED;

/*
 * Created by JiangJie on 2017/2/13.
 */

public class SocketClientService extends Service {

    private long sendTime = 0L;
    private WeakReference<Socket> mSocket;
    private ReadThread mReadThread;
    // 发送心跳包
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE * 1000) {
                boolean isSuccess = sendMsg(HEART_BEAT_MESSAGE);// 就发送一个\r\n过去, 如果发送失败，就重新初始化一个socket
                if (!isSuccess) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendBroadcast(new Intent(SOCKET_STATE_DISCONNECTED));
                            mHandler.removeCallbacks(heartBeatRunnable);
                            mReadThread.release();
                            releaseLastSocket(mSocket);
                            new InitSocketThread().start();
                        }
                    }, REST_CONNECT_TIME * 1000);
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE * 1000);
        }
    };

    private ISocketService.Stub iSocketService = new ISocketService.Stub() {
        @Override
        public boolean sendMessage(String message) throws RemoteException {
            return sendMsg(message);
        }

        @Override
        public void startSocket() throws RemoteException {
            startSocketClient();
        }

        @Override
        public void stopSocket() throws RemoteException {
            stopSocketClient();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return iSocketService;
    }

    private void startSocketClient() {
        new InitSocketThread().start();
    }

    private void stopSocketClient() {
        Socket socket = mSocket.get();
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean sendMsg(String msg) {
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        Socket soc = mSocket.get();
        try {
            if (!soc.isClosed() && !soc.isOutputShutdown()) {
                OutputStream os = soc.getOutputStream();
                os.write(msg.getBytes());
                os.flush();
                sendTime = System.currentTimeMillis();// 每次发送成功数据，就改一下最后成功发送的时间，节省心跳间隔时间
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    // 初始化socket
    private void initSocket() throws IOException {
        Socket socket = new Socket(HOST, PORT);
        mSocket = new WeakReference<>(socket);
        mReadThread = new ReadThread(socket);
        mReadThread.start();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE * 1000);// 初始化成功后，就准备发送心跳包
        if (socket.isConnected()) {
            sendBroadcast(new Intent(SOCKET_STATE_CONNECTED));
        }
    }

    // 释放socket
    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    sk.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                initSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int length;
                    while (!socket.isClosed() && !socket.isInputShutdown() && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer, length)).trim();
                            Intent intent = new Intent(MESSAGE_ACTION);
                            intent.putExtra("message", message);
                            sendBroadcast(intent);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
