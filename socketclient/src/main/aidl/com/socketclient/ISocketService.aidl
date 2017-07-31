// ISocketService2.aidl
package com.socketclient;

// Declare any non-default types here with import statements

interface ISocketService {

   boolean sendMessage(String message);
   void startSocket();
   void stopSocket();

}
