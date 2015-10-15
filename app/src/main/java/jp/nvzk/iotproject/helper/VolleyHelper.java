package jp.nvzk.iotproject.helper;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by yurina on 2014/11/26.
 */
public class VolleyHelper {
    public static final Object lock = new Object();
    public static RequestQueue requestQueue;
    public static ImageLoader imageLoader;

    public static RequestQueue getRequestQueue(final Context context){
        synchronized (lock){
            if(requestQueue == null){
                requestQueue = Volley.newRequestQueue(context);
            }
            return requestQueue;
        }
    }

    public static void cancelRequest(Object tag){
        if(requestQueue != null){
            requestQueue.cancelAll(tag);
        }
    }

    public interface RequestFilter{
        public boolean apply(Request<?> request);
    }

}
