package jp.nvzk.iotprojectandroid.util;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import jp.nvzk.iotprojectandroid.App;


/**
 * Created by menteadmin on 2015/08/05.
 */
public class ProfileUtil {
    private ProfileUtil(){}

    private final static String FIRST = "FIRST";

    private static SharedPreferences sSetting = PreferenceManager.getDefaultSharedPreferences(App.context);
    private static SharedPreferences.Editor sEditor = sSetting.edit();

    /**
     * 初回起動かどうかを返す
     * @return
     */
    public static boolean getFirstStartFlag(){
        return sSetting.getBoolean(FIRST, true);
    }

    /**
     * 初回起動済みにフラグをセット
     * @param flag
     */
    public static void setFirstStartFlag(boolean flag){
        sEditor.putBoolean(FIRST, flag).commit();
    }

    public static String getUserId(){
        return sSetting.getString("ID", "");
    }

    public static void setUserId(String id){
        sEditor.putString("ID", id);
    }

    public static BluetoothDevice getBluetoothDevice(){
        String string = sSetting.getString("DEVICE", "");
        Gson gson = new Gson();
        return gson.fromJson(string, BluetoothDevice.class);
    }

    public static void setBluetoothDevice(BluetoothDevice device){
        Gson gson = new Gson();
        sEditor.putString("DEVICE", gson.toJson(device));
    }
}
