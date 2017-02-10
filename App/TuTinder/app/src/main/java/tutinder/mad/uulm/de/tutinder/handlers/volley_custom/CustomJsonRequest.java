package tutinder.mad.uulm.de.tutinder.handlers.volley_custom;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CustomJsonRequest extends JsonRequest<String> {

    private final Map<String, String> headers;
    private final Response.Listener<String> listener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url     URL of the request to make
     * @param headers Map of request headers
     */
    public CustomJsonRequest(int method, String url, String body, Map<String, String> headers,
                             Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url,body,listener, errorListener);

        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? createAuthHeader(headers.get("matrikelnr"), headers.get("password")) : super.getHeaders();
    }

    private Map<String, String> createAuthHeader(String matrikelnr, String password) {
        Map<String, String> headerMap = new HashMap<String, String>();

        String credentials = matrikelnr + ":" + password;
        String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + encodedCredentials);

        return headerMap;
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    json,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

}