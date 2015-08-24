package jp.nvzk.iotproject.util;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import jp.nvzk.iotproject.App;


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
        sEditor.putString("ID", id).commit();
    }

    public static String getUserName(){
        return sSetting.getString("NAME", "");
    }

    public static void setUserName(String name){
        sEditor.putString("NAME", name).commit();
    }

    public static BluetoothDevice getBluetoothDeviceLeft(){
        String string = sSetting.getString("DEVICE_LEFT", "");
        Gson gson = new Gson();
        return gson.fromJson(string, BluetoothDevice.class);
    }

    public static boolean haveDeviceLeft(){
        String string = sSetting.getString("DEVICE_LEFT", "");
        if(string.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    public static boolean haveDeviceRight(){
        String string = sSetting.getString("DEVICE_RIGHT", "");
        if(string.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    public static void setBluetoothDeviceLeft(BluetoothDevice device){
        Gson gson = new Gson();
        sEditor.putString("DEVICE_LEFT", gson.toJson(device)).commit();
    }

    public static BluetoothDevice getBluetoothDeviceRight(){
        String string = sSetting.getString("DEVICE_RIGHT", "");
        Gson gson = new Gson();
        return gson.fromJson(string, BluetoothDevice.class);
    }

    public static void setBluetoothDeviceRight(BluetoothDevice device){
        Gson gson = new Gson();
        sEditor.putString("DEVICE_RIGHT", gson.toJson(device)).commit();
    }
}
