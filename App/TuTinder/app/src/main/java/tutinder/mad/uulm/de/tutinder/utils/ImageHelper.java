package tutinder.mad.uulm.de.tutinder.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by Lukas on 08.07.2016.
 */
public class ImageHelper {

    /**
     *  Crates a ImageUrl with specified widht and height.
     * @param imageUrl
     * @param width
     * @param height
     * @return
     */
    public static String getThumbnailFrom(String imageUrl, int width, int height) {
        String tmppath = imageUrl;
        int divider = tmppath.lastIndexOf("upload/") + 7;
        String firstPiece = tmppath.substring(0, divider);
        String lastPiece = tmppath.substring(divider);
        String imageConversion = "w_" + width + ",h_" + height + ",c_thumb/";

        return (firstPiece + imageConversion + lastPiece);
    }

    /**
     * Creates a circle Bitmap.
     * @param bitmap
     * @return
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
