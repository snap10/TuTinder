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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupChatActivity;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * RecyclerAdapter for Group Items.
 *
 * @author 1uk4s
 * @author snap10
 */
public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private List<Group> itemList;

    private OnListItemInteractListener listener;

    /**
     * Default Constructor.
     * @param context
     * @param groupList
     * @param listener
     */
    public GroupListAdapter(Context context, List<Group> groupList, OnListItemInteractListener listener) {
        this.mContext = context;
        this.mTutinder = Tutinder.getInstance();
        this.mVolley = VolleySingleton.getInstance(this.mContext);
        this.itemList = groupList;
        this.listener = listener;
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_default, parent, false);
        return new GroupViewHolder(itemView);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
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
     * Updates the groupList of the Adapter.
     * @param groupList
     */
    public void setItemList(List<Group> groupList) {
        this.itemList = groupList;
        notifyDataSetChanged();
    }


    /**
     * ViewHolder of GroupListAdapter.
     *
     * @author 1uk4s
     * @author snap10
     */
    public class GroupViewHolder extends RecyclerView.ViewHolder {

        private final int LOADING_ARRAY_POSITION_USERS = 0;
        private final int LOADING_ARRAY_POSITION_COURSE = 1;
        private boolean[] loadingArray = {false, false};

        private RelativeLayout rootView;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ImageButton btnAction;

        private Group groupItem;


        /**
         * Default Constructor.
         * @param itemView
         */
        public GroupViewHolder(View itemView) {
            super(itemView);

            rootView = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
            btnAction.setImageResource(R.drawable.ic_chat_black_24dp);
        }

        /**
         * Binds a Group Object to this ViewHolder Instance.
         * @param group
         */
        public void bind(final Group group) {
            groupItem = group;

            this.itemView.setVisibility(View.INVISIBLE);

            groupItem.getUsers(mContext, mVolley, mTutinder, new Group.OnUsersLoadedListener() {
                @Override
                public void onUsersComplete(List<User> users) {
                    String usernames = "";
                    for (int i = 0; i < users.size(); i++) {
                        if(i > 0) {
                            usernames += ", " + users.get(i).getName() ;
                        } else {
                            usernames += users.get(i).getName() ;
                        }
                    }
                    tvSubtitle.setText(usernames);

                    loadingArray[LOADING_ARRAY_POSITION_USERS] = true;
                    showItem();
                }
            });
            groupItem.getCourse(mVolley, mTutinder, new Group.OnCourseLoadedListener() {
                @Override
                public void onCourseLoaded(Course course) {
                    tvTitle.setText(course.getName());
                    ivPicture.setImageUrl(course.getThumbnailpath(), mVolley.getImageLoader());

                    loadingArray[LOADING_ARRAY_POSITION_COURSE] = true;
                    showItem();
                }
            });

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GroupActivity.class);
                    intent.putExtra("groupid", group.get_id());
                    intent.putExtra("ismember", true);
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

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GroupChatActivity.class);
                    intent.putExtra("groupid", group.get_id());
                    mContext.startActivity(intent);
                }
            });
        }

        /**
         * Shows a PopupMenu at this ViewHolder Instances Position.
         */
        public void showPopupMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_group_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.action_show_group:
                            Bundle argsGroup = new Bundle();
                            argsGroup.putString("groupid", groupItem.get_id());
                            argsGroup.putBoolean("ismember", true);
                            listener.onListItemInteract(mContext.getString(R.string.action_show_group), argsGroup);
                            return true;
                        case R.id.action_send_messages:
                            Bundle argsMessages = new Bundle();
                            argsMessages.putString("groupid", groupItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_send_messages), argsMessages);
                            return true;
                        case R.id.action_leave_group:
                            Bundle argsLeave = new Bundle();
                            argsLeave.putString("groupid", groupItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_leave_group), argsLeave);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        /**
         * Shows item if all necessary data is available.
         */
        public void showItem(){
            for(boolean isLoaded : loadingArray) {
                if(!isLoaded) {
                    return;
                }
            }
            this.itemView.setVisibility(View.VISIBLE);
        }
    }

}
