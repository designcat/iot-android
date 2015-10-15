package jp.nvzk.iotproject;

/**
 * Created by menteadmin on 2015/07/31.
 */
public class Const {

    //BLESerial サービスUUID
    //public static final String UUID_BLESERIAL_SERVICE = "569a1101-b87F-490c-92cb-11ba5ea5167c";
    public static final String UUID_BLESERIAL_SERVICE = "bd011f22-7d3c-0db6-e441-55873d44ef40";
    //　BLESerial　受信UUID （Notify)
    //public static final String UUID_BLESERIAL_RX = "569a2000-b87F-490c-92cb-11ba5ea5167c";
    public static final String UUID_BLESERIAL_RX = "2a750d7d-bd9a-928f-b744-7d5a70cef1f9";
    // BLESerial 送信UUID （write without response)
    //public static final String UUID_BLESERIAL_TX = "569a2001-b87F-490c-92cb-11ba5ea5167c";
    public static final String UUID_BLESERIAL_TX = "0503b819-c75b-ba9b-3641-6a7f338dd9bd";
    //　キャラクタリスティック設定UUID
    //public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String ROOT_URL = "http://petapeta-staging.rot1024.com/api/v1/";

    public static final String SOCKET_URL = "http://petaxpeta.herokuapp.com/";
    public static final String SOCKET_URL_ROOM = "http://petaxpeta.herokuapp.com/";

    public class KEY{
        public static final String DEVICE = "DEVICE";
        public static final String MEMBERS = "MEMBERS";
        public static final String ROOM = "ROOM";
        public static final String MESSAGE = "MESSAGE";
    }
}
