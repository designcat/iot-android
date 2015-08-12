package jp.nvzk.iotprojectandroid;

import android.app.Application;

import com.github.nkzawa.socketio.client.Socket;

import jp.nvzk.iotprojectandroid.util.SocketUtil;

public class App extends Application {
    public static App context;
    public static Socket socket;

    @Override
    public void onCreate(){
        super.onCreate();
        context = (App)getApplicationContext();

        SocketUtil.initSocket();
    }
}
