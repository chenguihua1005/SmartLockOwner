package com.websocketclient;

/**
 * Created by JiangJie on 2017/2/14.
 */

public interface OnWebSocketListener {
    void onResponse(String response);

    void onConnected();

    void onConnectError(String e);

    void onDisConnected();
}
