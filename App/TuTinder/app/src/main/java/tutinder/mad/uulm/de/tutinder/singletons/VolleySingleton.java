package tutinder.mad.uulm.de.tutinder.singletons;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.LruBitmapCache;

/**
 * Created by Snap10 on 04.05.16.
 */
public class VolleySingleton {
    public static final String TAG = VolleySingleton.class.getName();
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    /**
     * @URL for Backend Server...
     * TODO insert your own Backend Adress
     */
    private static final String API_ROOT = "http://:192.168.0.100:3000";


    private VolleySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(context));
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public void cancelRequests() {
        mRequestQueue.cancelAll(TAG);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public String getAPIRoot() {
        return API_ROOT;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}