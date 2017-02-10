package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * Created by Lukas on 30.06.2016.
 */
public class ThumbnailListAdapter extends RecyclerView.Adapter<ThumbnailListAdapter.ThumbnailViewHolder> {

    Context context;
    VolleySingleton volleySingleton;

    List<String> thumbnailPathList;

    public ThumbnailListAdapter(Context context, List<String> thumbnailPathList) {
        this.context = context;
        this.volleySingleton = VolleySingleton.getInstance(this.context);
        this.thumbnailPathList = thumbnailPathList;
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_thumbnail, parent, false);
        return new ThumbnailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        holder.bind(thumbnailPathList.get(position));
    }

    @Override
    public int getItemCount() {
        return thumbnailPathList.size();
    }

    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        private CircleNetworkImageView ivThumbnail;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = (CircleNetworkImageView) itemView.findViewById(R.id.iv_thumbnail);
        }

        public void bind(String thumbnailPath) {
            ivThumbnail.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_accent_50dp);
            ivThumbnail.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_accent_50dp);
            ivThumbnail.setImageUrl(thumbnailPath, volleySingleton.getImageLoader());
        }
    }
}
