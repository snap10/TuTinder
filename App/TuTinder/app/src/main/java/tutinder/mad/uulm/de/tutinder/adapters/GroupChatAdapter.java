package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.exceptions.UsersNotLoadedException;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.Message;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * Created by Lukas on 02.07.2016.
 */
public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MessageViewHolder> {

    // inflates message layout (alignment right)
    private final int MESSAGE_SELF = 0;
    private final int MESSAGE_SELF_NO_INFO = 1; // username and thumbnail are hidden
    private final int MESSAGE_SELF_DATE_CHANGE = 2; // show additional cardView, which shows the date
    private final int MESSAGE_SELF_NO_INFO_DATE_CHANGE = 3; // username and thumbnail are hidden & show additional cardView, which shows the date

    // inflates message layout (alignment left)
    private final int MESSAGE_OTHERS = 10;
    private final int MESSAGE_OTHERS_NO_INFO = 11; // username and thumbnail are hidden
    private final int MESSAGE_OTHERS_DATE_CHANGE = 12; // show additional cardView, which shows the date
    private final int MESSAGE_OTHERS_NO_INFO_DATE_CHANGE = 13; // username and thumbnail are hidden & show additional cardView, which shows the date

    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;

    private Group group;
    private List<Message> messages;

    public GroupChatAdapter(Context context, Group group) {
        this.context = context;
        this.helper = Tutinder.getInstance();
        this.volleySingleton = VolleySingleton.getInstance(this.context);
        this.group = group;
        this.messages = Arrays.asList(group.getMessages());
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isAuthor = false;
        boolean isMultible = false;
        boolean isDateChange = false;

        View itemView;
        // layout (alignment right)
        if(viewType == MESSAGE_SELF || viewType == MESSAGE_SELF_NO_INFO || viewType == MESSAGE_SELF_DATE_CHANGE || viewType == MESSAGE_SELF_NO_INFO_DATE_CHANGE) {
            isAuthor = true;
            itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.recycler_item_message_self, parent, false);

            if(viewType == MESSAGE_SELF_NO_INFO_DATE_CHANGE) {
                isMultible= true;
                isDateChange = true;
            } else {
                if(viewType == MESSAGE_SELF_NO_INFO) {
                    isMultible= true;
                }
                if(viewType == MESSAGE_SELF_DATE_CHANGE) {
                    isDateChange = true;
                }
            }
        }
        // layout (alignment left)
        else if(viewType == MESSAGE_OTHERS || viewType == MESSAGE_OTHERS_NO_INFO || viewType == MESSAGE_OTHERS_DATE_CHANGE ||viewType == MESSAGE_OTHERS_NO_INFO_DATE_CHANGE) {
            itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.recycler_item_message_others, parent, false);

            if(viewType == MESSAGE_OTHERS_NO_INFO_DATE_CHANGE) {
                isMultible= true;
                isDateChange = true;
            } else {
                if(viewType == MESSAGE_OTHERS_NO_INFO) {
                    isMultible= true;
                }
                if(viewType == MESSAGE_OTHERS_DATE_CHANGE) {
                    isDateChange = true;
                }
            }
        }
        // default: layout (alignment left)
        else {
            itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.recycler_item_message_others, parent, false);
        }

        return new MessageViewHolder(itemView, isAuthor, isMultible, isDateChange);
    }

    @Override
    public int getItemViewType(int position) {
        boolean isMultible = false;
        boolean isDateChange = false;

        // show date on first item
        if(position == 0) {
            isDateChange = true;
        }

        if(position > 0) {
            // check if user sent multiple messages
            if(messages.get(position).getUser().equals(messages.get(position-1).getUser())) {
                isMultible = true;
            }
            // check if day changed
            Calendar calCurrent = Calendar.getInstance();
            calCurrent.setTime(messages.get(position).getCreated());
            Calendar calPrevious = Calendar.getInstance();
            calPrevious.setTime(messages.get(position-1).getCreated());
            if(calCurrent.get(Calendar.DAY_OF_YEAR) != calPrevious.get(Calendar.DAY_OF_YEAR)){
                isDateChange = true;
            }
        }

        // check if message if from logged in user
        // return value based on decisions
        if(messages.get(position).getUser().equals(helper.getLoggedInUser().get_id())) {
            if(isMultible && isDateChange) {
                return MESSAGE_SELF_NO_INFO_DATE_CHANGE;
            } else if(!isMultible && isDateChange) {
                return MESSAGE_SELF_DATE_CHANGE;
            } else if(isMultible && !isDateChange){
                return MESSAGE_SELF_NO_INFO;
            } else {
                return MESSAGE_SELF;
            }
        } else {
            if(isMultible && isDateChange) {
                return MESSAGE_OTHERS_NO_INFO_DATE_CHANGE;
            } else if(!isMultible && isDateChange) {
                return MESSAGE_OTHERS_DATE_CHANGE;
            } else if(isMultible && !isDateChange){
                return MESSAGE_OTHERS_NO_INFO;
            } else {
                return MESSAGE_OTHERS;
            }
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> message) {
        this.messages = message;
        notifyDataSetChanged();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private CardView cardMessage;
        private TextView tvUserName, tvMessage, tvTime, tvDate;
        private CircleNetworkImageView ivThumbnail;

        private boolean isAuthor;
        private boolean isMultible;
        private boolean isDateChange;

        public MessageViewHolder(View itemView, boolean isAuthor, boolean isMultible, boolean isDateChange) {
            super(itemView);

            this.isAuthor = isAuthor;
            this.isMultible = isMultible;
            this.isDateChange = isDateChange;

            cardMessage = (CardView) itemView.findViewById(R.id.card_message);
            tvUserName = (TextView) itemView.findViewById(R.id.tv_username);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            ivThumbnail = (CircleNetworkImageView) itemView.findViewById(R.id.iv_thumbnail);
        }

        public void bind(final Message message) {
            // popup on longclick
            cardMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("message", tvMessage.getText().toString());
                    clipboardManager.setPrimaryClip(clip);

                    Toast.makeText(context, R.string.toast_message_copy, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            // show message
            tvMessage.setText(message.getText());

            // show message created (hours and minutes)
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
            String time = formatTime.format(message.getCreated());
            tvTime.setText(time);

            // hide username if user is author of message
            if(isAuthor) {
                tvUserName.setVisibility(View.GONE);
            }

            // hide username and thumbnail if user sent multible messages
            if(isMultible) {
                tvUserName.setVisibility(View.GONE);
                ivThumbnail.setVisibility(View.INVISIBLE);
            } else {
                ivThumbnail.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
                ivThumbnail.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            }

            // show date if day has changed
            if(isDateChange) {
                SimpleDateFormat formatDate = new SimpleDateFormat("EEEE, dd. MMM yyyy");
                String date = formatDate.format(message.getCreated());
                tvDate.setText(date);
                tvDate.setVisibility(View.VISIBLE);
            }

            // show username and thumbnail
            try {
                tvUserName.setText(group.getUserById(message.getUser()).getName());
                ivThumbnail.setImageUrl(group.getUserById(message.getUser()).getProfileThumbnailPath(), volleySingleton.getImageLoader());
            } catch (UsersNotLoadedException e) {
                group.getUsers(context, volleySingleton, helper, new Group.OnUsersLoadedListener() {
                    @Override
                    public void onUsersComplete(List<User> users) {
                        if(users != null) {
                            for(User user : users) {
                                if(user.get_id().equals(message.getUser())) {
                                    tvUserName.setText(user.getName());
                                    ivThumbnail.setImageUrl(user.getProfileThumbnailPath() , volleySingleton.getImageLoader());
                                    break;
                                }
                            }
                        }
                    }
                });
                e.printStackTrace();
            }
        }
    }
}
