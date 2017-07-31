package com.socketclient;

/*
 * Created by JiangJie on 2017/2/13.
 */

public class SocketConfig {
    public static final long HEART_BEAT_RATE = 5;//心跳检测时间(秒)
    public static final int REST_CONNECT_TIME = 3; //重新连接时间间隔(秒)
    public static final String HOST = "ws://192.168.3.115:8080/lhzsmm/websocket/test";
    public static final int PORT = 30003;
    public static final String HEART_BEAT_MESSAGE = "heartBeat";//默认心跳内容
    public static final String MESSAGE_ACTION = "android.socket.message_action";
    public static final String SOCKET_STATE_CONNECTED = "android.socket.connected_action";
    public static final String SOCKET_STATE_DISCONNECTED = "android.socket.disconnected_action";
}
