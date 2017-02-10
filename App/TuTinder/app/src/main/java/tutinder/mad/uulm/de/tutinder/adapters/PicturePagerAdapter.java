package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.NetworkImageView;

import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * Created by Snap10 on 23.05.16.
 */
public class PicturePagerAdapter extends PagerAdapter {

    private final String[] picturePaths;
    private final Context context;
    private final int defaultPictureId;

    public PicturePagerAdapter(Context context, String[] picturePaths, int defaultPictureId) {
        this.context = context;
        this.picturePaths = picturePaths;
        this.defaultPictureId = defaultPictureId;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {


        NetworkImageView networkImageView = new NetworkImageView(context);
        ViewPager.LayoutParams params = new ViewPager.LayoutParams();


        params.height = ViewPager.LayoutParams.MATCH_PARENT;
        params.width = ViewPager.LayoutParams.MATCH_PARENT;

        networkImageView.setLayoutParams(params);
        networkImageView.setScaleType(NetworkImageView.ScaleType.CENTER_CROP);
        networkImageView.setMinimumHeight(100);
        networkImageView.setImageUrl(picturePaths[position], VolleySingleton.getInstance(context).getImageLoader());
        networkImageView.setDefaultImageResId(defaultPictureId);
        networkImageView.setErrorImageResId(defaultPictureId);
        container.addView(networkImageView);
        return networkImageView;

    }

    @Override
    public int getCount() {
        return picturePaths.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
