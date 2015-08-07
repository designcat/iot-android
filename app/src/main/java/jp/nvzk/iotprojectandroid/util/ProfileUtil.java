package jp.nvzk.iotprojectandroid.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
}
