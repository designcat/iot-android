package jp.nvzk.iotprojectandroid;

/**
 * Created by menteadmin on 2015/07/31.
 */
public class Const {

    //BLESerial サービスUUID
    public static final String UUID_BLESERIAL_SERVICE = "569a1101-b87F-490c-92cb-11ba5ea5167c";
    //　BLESerial　受信UUID （Notify)
    public static final String UUID_BLESERIAL_RX = "569a2000-b87F-490c-92cb-11ba5ea5167c";
    // BLESerial 送信UUID （write without response)
    public static final String UUID_BLESERIAL_TX = "569a2001-b87F-490c-92cb-11ba5ea5167c";
    //　キャラクタリスティック設定UUID
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public class KEY{
        public static final String DEVICE = "DEVICE";
        public static final String MEMBERS = "MEMBERS";
    }
}
