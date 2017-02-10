package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.FriendActivity;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * RecyclerAdapter for Friend Items.
 *
 * @author 1uk4s
 * @author snap10
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {

    private Context mContext;
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
    public FriendListAdapter(Context context, List<User> friendList, OnListItemInteractListener listener) {
        this.mContext = context;
        this.mVolley = VolleySingleton.getInstance(mContext);
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
                .inflate(R.layout.recycler_item_default, parent, false);
        return new FriendViewHolder(itemView);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.bind(itemList.get(position));
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
     * @param userList
     */
    public void setItemList(List<User> userList) {
        this.itemList = userList;
        notifyDataSetChanged();
    }


    /**
     * ViewHolder of FriendListAdapter
     *
     * @author 1uk4s
     * @author snap10
     */
    public class FriendViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootView;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ImageButton btnAction;

        private User friendItem;


        /**
         * Default Constructor.
         *
         * @param itemView
         */
        public FriendViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);

            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);

            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu();
                }
            });

            rootView = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, FriendActivity.class);
                    intent.putExtra("friendid", friendItem.get_id());
                    mContext.startActivity(intent);
                }
            });
            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu();
                    return true;
                }
            });
        }

        /**
         * Binds a User Object to this ViewHolder Instance.
         *
         * @param friend
         */
        public void bind(User friend) {
            friendItem = friend;

            tvTitle.setText(friend.getName());
            tvSubtitle.setText(friend.getStudycourse());
            ivPicture.setImageUrl(friend.getProfilepicturepath(), mVolley.getImageLoader());
        }

        /**
         * Shows a PopupMenu at this ViewHolder Instances Position.
         */
        private void showPopupMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_friend_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.action_show_profile:
                            Bundle argsProfile = new Bundle();
                            argsProfile.putString("friendid", friendItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_show_profile), argsProfile);
                            return true;
                        case R.id.action_remove_friend:
                            Bundle argsRemove = new Bundle();
                            argsRemove.putString("friendid", friendItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_remove_friend), argsRemove);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }
    }
}
