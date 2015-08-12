package jp.nvzk.iotprojectandroid.util;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Created by user on 15/08/09.
 */
public class SocketUtil {

    private static Socket socket;

    public static void initSocket(){
        try {
            socket = IO.socket("http://localhost");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket(){
        return socket;
    }

}
