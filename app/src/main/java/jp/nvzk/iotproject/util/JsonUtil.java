package jp.nvzk.iotproject.util;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import jp.nvzk.iotproject.model.ResponseData;

/**
 * Created by menteadmin on 2015/10/15.
 */
public class JsonUtil {

    public static ResponseData getResponse(String rootStr){
        String data = "";
        try {
            JSONObject rootObject = new JSONObject(rootStr);
            data = rootObject.toString();
        } catch (JSONException e) {
            //...
        }
        Gson gson = new Gson();
        return gson.fromJson(data, ResponseData.class);
    }

}
