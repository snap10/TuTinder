package tutinder.mad.uulm.de.tutinder.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

public class QrCodeActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    public static final String QR_PREFIX = "TuTinder:ID:";
    public static final String QR_DELIMITER = ":";
    private final int ACCENT = 0xFF2196F3;
    private final int BLACK = 0xFF000000;
    private final int WHITE = 0xFFFFFFFF;
    private final int TRANSPARENT = 0x00FFFFFF;

    private Context context;
    private Tutinder helper = Tutinder.getInstance();
    private Boolean nonceSent = false;
    private Toolbar toolbar;

    UUID nonce;
    private ImageView ivQrCode;
    private VolleySingleton volleySingleton;
    private int size;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
         /*
            Context
         */
        context = getApplicationContext();
        volleySingleton = VolleySingleton.getInstance(context);
        /*
            Toolbar
         */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get view elements
         */
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        ivQrCode = (ImageView) findViewById(R.id.iv_qrcode);

        nonce = UUID.randomUUID();
        saveNonceOnServer(nonce);
    }

    /**
     * Makes an API call to load a course with the given courseId.
     *
     * @param nonce
     */
    private void saveNonceOnServer(final UUID nonce) {
        showProgressDialog(getString(R.string.dialog_message_loading));
        final String URL = volleySingleton.getAPIRoot() + "/user/friends/friendrequest";
        try {
            JSONObject body = new JSONObject().put("nonce", nonce);

            CustomJsonRequest requestCourse = new CustomJsonRequest(Request.Method.POST, URL, body.toString(), helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    nonceSent = true;
                    hideProgressDialog();
                    showQrCode(helper.getLoggedInUser().get_id(), nonce, size);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null) {
                        Toast.makeText(context, error.networkResponse.statusCode + " " + getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                    } else {
                        //TODO Remove error.getMessage() in Production
                        Toast.makeText(context, getString(R.string.strangeError) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    hideProgressDialog();
                    finish();
                }
            });
            volleySingleton.addToRequestQueue(requestCourse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressDialog(String msg) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }

    }

    /**
     * Hides the activities loading ProgressDialog if its showing.
     */
    private void hideProgressDialog() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(false);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // get ImageView width
        size = ivQrCode.getWidth();
        if (nonceSent) {
            showQrCode(helper.getLoggedInUser().get_id(), nonce, size);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showQrCode(String request, UUID nonce, int size) {
        Bitmap qrcode = null;
        try {
            qrcode = encodeAsBitmap(request, nonce, size);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_generating_qr_code, Toast.LENGTH_LONG).show();
            onBackPressed();
            return;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_generating_qr_code, Toast.LENGTH_LONG).show();
            onBackPressed();
            return;
        }
        ivQrCode.setImageBitmap(qrcode);
    }

    public Bitmap encodeAsBitmap(String str, UUID nonce, int size) throws WriterException, IllegalArgumentException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix result;
        String code = QR_PREFIX + str + QR_DELIMITER + nonce;
        result = new MultiFormatWriter().encode(code, BarcodeFormat.QR_CODE, size, size, hints);

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : TRANSPARENT;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
