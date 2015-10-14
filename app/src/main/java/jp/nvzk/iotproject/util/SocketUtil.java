package jp.nvzk.iotproject.util;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

import jp.nvzk.iotproject.Const;

/**
 * Created by user on 15/08/09.
 */
public class SocketUtil {

    private static Socket socket;
    private static Socket roomSocket;

    public static void initSocket(){
        try {
            socket = IO.socket(Const.SOCKET_URL);
            roomSocket = IO.socket(Const.SOCKET_URL_ROOM);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket(){
        return socket;
    }

    public static Socket getRoomSocket(){
        return roomSocket;
    }

}
