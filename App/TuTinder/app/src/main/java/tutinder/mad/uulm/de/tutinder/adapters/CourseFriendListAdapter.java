package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.security.InvalidParameterException;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.FriendActivity;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * RecyclerAdapter for Friend Items.
 *
 * @author 1uk4s
 * @author snap10
 */
public class CourseFriendListAdapter extends RecyclerView.Adapter<CourseFriendListAdapter.FriendViewHolder> {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private List<User> itemList;

    private OnListItemInteractListener listener;


    /**
     * Default Constructor.
     *
     * @param context
     * @param friendList
     * @param listener
     */
    public CourseFriendListAdapter(Context context, List<User> friendList, OnListItemInteractListener listener) {
        this.mContext = context;
        this.mTutinder = Tutinder.getInstance();
        this.mVolley = VolleySingleton.getInstance(context);
        this.itemList = friendList;
        this.listener = listener;
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_inner, parent, false);
        return new FriendViewHolder(itemView);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.bind(itemList.get(position), position);
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Updates the itemList of the Adapter.
     *
     * @param friendList
     */
    public void setItemList(List<User> friendList) {
        this.itemList.clear();
        this.itemList.addAll(friendList);
        notifyDataSetChanged();
    }


    /**
     * ViewHolder of CourseFriendListAdapter.
     */
    public class FriendViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootView;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ProgressBar progressBar;
        private ImageButton btnAction;
        private View vSeparator;

        private User userItem;


        /**
         * Default Constructor.
         *
         * @param itemView
         */
        public FriendViewHolder(View itemView) {
            super(itemView);

            rootView = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);

            progressBar = (ProgressBar) itemView.findViewById(R.id.listitem_progressbar);
            vSeparator = (View) itemView.findViewById(R.id.listitem_separator);

            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);

            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);

            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);


        }

        /**
         * Binds a User Object to this ViewHolder Instance.
         *
         * @param friend
         * @param position
         */
        public void bind(User friend, int position) {
            if(position == 0) {
                vSeparator.setVisibility(View.INVISIBLE);
            }

            userItem = friend;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, FriendActivity.class);
                    intent.putExtra("friendid", userItem.get_id());
                    mContext.startActivity(intent);
                }
            });

            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupDialog();
                    return true;
                }
            });

            ivPicture.setImageUrl(userItem.getProfileThumbnailPath(), mVolley.getImageLoader());

            tvTitle.setText(userItem.getName());
            tvSubtitle.setText(userItem.getDescription());

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupDialog();
                }
            });
        }

        /**
         * Shows a PopupMenu at this ViewHolder Instances Position.
         */
        private void showPopupDialog() {
            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_course_friend_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_send_group_request:
                            Bundle bundleRequest = new Bundle();
                            bundleRequest.putString("userid", userItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_send_group_request), bundleRequest, FriendViewHolder.this);
                            return true;
                        case R.id.action_show_profile:
                            Bundle bundleProfile = new Bundle();
                            bundleProfile.putString("friendid", userItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_show_profile), bundleProfile);
                            return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }


        /**
         * Shows the ListItems loading ProgressDialog.
         */
        public void showProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setEnabled(true);
            }
        }

        /**
         * Hides the ListItems loading ProgressDialog if its showing.
         */
        public void hideProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                progressBar.setEnabled(false);
            }
        }

    }
}
