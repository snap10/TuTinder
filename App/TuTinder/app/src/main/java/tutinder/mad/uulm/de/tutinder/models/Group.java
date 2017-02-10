package tutinder.mad.uulm.de.tutinder.models;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.exceptions.UsersNotLoadedException;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * Created by Snap10 on 29.04.16.
 */
public class Group implements CustomListitem {

    private String _id;
    private String[] users;
    private List<User> userObjects;
    private String course;
    private Course courseobject;
    private Message[] messages;
    private String grouprequest;
    private Date updatedAt;
    private Date createdAt;

    public Group(String _id, String[] users, String course, Message[] messages, String grouprequest) {
        this._id = _id;
        this.users = users;
        this.course = course;
        this.messages = messages;
        this.grouprequest = grouprequest;
        this.userObjects = new ArrayList<>();
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Message[] getMessages() {
        return messages;
    }

    public List<Message> getMessagesList() {
        List<Message> messages = new ArrayList<Message>();
        for(Message msg : this.messages) {
            messages.add(msg);
        }
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public String getGrouprequest() {
        return grouprequest;
    }

    public void setGrouprequest(String grouprequest) {
        this.grouprequest = grouprequest;
    }

    /**
     * Returns the user with the provided id if the user is in the userobjects array and therefore must be groupmember...
     *
     * @param userid
     * @return
     * @see Group.OnUsersLoadedListener before you can get the User you must have called getUsers at least once and have the users returned by the listener...
     */
    public User getUserById(String userid) throws UsersNotLoadedException {
        if (userObjects != null && userObjects.size() == users.length) {
            for (User user :
                    userObjects) {
                if (user.get_id().equals(userid)) return user;
            }
        }
        throw new UsersNotLoadedException("You have to load the users via getUsers before calling this method...");
    }

    public void getUsers(final Context context, VolleySingleton volleySingleton, Tutinder helper, final OnUsersLoadedListener listener) {
        if (userObjects != null && userObjects.size() == users.length) {
            listener.onUsersComplete(userObjects);
            return;
        }

        //Build querystring to have the timeslots loaded...
        String querystring = "?uid=";
        for (int i = 0; i < users.length; i++) {
            if (i < users.length - 1)
                querystring += users[i] + ",";
            else {
                querystring += users[i];
            }
        }
        GsonRequest<User[]> tsRequest = new GsonRequest<>(Request.Method.GET, volleySingleton.getAPIRoot() + "/users" + querystring, null, User[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User[]>() {
            @Override
            public void onResponse(User[] response) {
                if (response != null) {
                    userObjects = Arrays.asList(response);
                }
                listener.onUsersComplete(userObjects);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "TODO Error getting Users", Toast.LENGTH_LONG).show();
            }
        });
        volleySingleton.addToRequestQueue(tsRequest);


    }

    public void getCourse(VolleySingleton volleySingleton, Tutinder helper, final OnCourseLoadedListener listener) {
        if (courseobject != null) {
            listener.onCourseLoaded(courseobject);
            return;
        }

        GsonRequest<Course> courseRequest = new GsonRequest<>(Request.Method.GET, volleySingleton.getAPIRoot() + "/courses/" + course, null, Course.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course>() {
            @Override
            public void onResponse(Course response) {
                courseobject = response;
                listener.onCourseLoaded(courseobject);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        volleySingleton.addToRequestQueue(courseRequest);
    }

    @Override
    public String getId() {
        return get_id();
    }

    @Override
    public String getTitle() {
        if (courseobject == null) return "";
        return courseobject.getName();
    }

    @Override
    public String getSubtitle() {
        if (userObjects == null) return "";
        String usernames = "";
        for (int i = 0; i < userObjects.size(); i++) {
            usernames += userObjects.get(i).getName() + ", ";
        }
        return usernames.substring(0, usernames.length() - 2);
    }

    @Override
    public String getThumbnailpath() {
        if (courseobject != null) {
            return courseobject.getThumbnailpath();
        }
        return null;
    }

    @Override
    public Types getType() {
        return Types.GROUP;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public interface OnUsersLoadedListener {

        public void onUsersComplete(List<User> users);
    }

    public interface OnCourseLoadedListener {
        public void onCourseLoaded(Course course);
    }
}




