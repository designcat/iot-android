package jp.nvzk.iotproject.util;

import com.loopj.android.http.RequestParams;

import jp.nvzk.iotproject.Const;

/**
 * Created by menteadmin on 2015/10/15.
 */
public class UrlUtil {

    public static String ALREADY_NAME = "name_already_used";
    public static String NAME_REQUIRED = "name_required";
    public static String NAME_INVALIDE = "name_invalid";
    public static String PASSWORD_INVALID = "password_invalid";

    public static String getSignInUrl(){
        return Const.ROOT_URL + "users/";
    }

    /**
     * コメントをポストするパラム
     * @return
     */
    public static RequestParams getSignInParams(String id, String name, String pass) {
        RequestParams params = new RequestParams();
        params.put("name", id);
        params.put("password", pass);
        params.put("nickname", name);

        return params;
    }

    public static String getErrorMessage(String msg){
        if(msg.equals(ALREADY_NAME)){
            return "同じユーザー名のユーザーが既に登録されています";
        }
        else if(msg.equals(NAME_REQUIRED)){
            return "ユーザー名が入力されていません";
        }
        else if(msg.equals(NAME_INVALIDE)){
            return "ユーザ名にアルファベット小文字、数字、_以外の文字が使用されています";
        }
        else if(msg.equals(PASSWORD_INVALID)){
            return "パスワードが入力されていません";
        }
        else {
            return "データが不正です";
        }
    }
}
