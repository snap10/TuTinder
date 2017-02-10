package tutinder.mad.uulm.de.tutinder.models;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Snap10 on 04.07.16.
 */
public class GroupRequest implements CustomListitem {
    private String _id;
    private Course _courseid;
    private List<GroupRequestMember> members;
    private Date updatedAt;
    private Date createdAt;

    public GroupRequest() {
    }

    public GroupRequest(String _id, Course _courseid, GroupRequestMember member, Date updatedAt, Date createdAt) {
        this._id = _id;
        this._courseid = _courseid;
        this.members = members;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Course get_courseid() {
        return _courseid;
    }

    public void set_courseid(Course _courseid) {
        this._courseid = _courseid;
    }

    public List<GroupRequestMember> getMembers() {
        return members;
    }

    public List<User> getUsers() {
        List<User> tmpList = new ArrayList<>();
        for (GroupRequestMember grpm :
                members) {
            tmpList.add(grpm.get_userid());
        }
        return tmpList;
    }

    /**
     * Returns the number of users that requested the request
     *
     * @return
     */
    public int getAcceptedNumber() {
        int counter = 0;
        for (GroupRequestMember grpm :
                members) {
            if (grpm.getAccept()) counter++;

        }
        return counter;
    }

    public void setMembers(List<GroupRequestMember> members) {
        this.members = members;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public String getTitle() {

        return "Grouprequest//TODO insert Context to CustomListItem";
    }

    @Override
    public String getSubtitle() {
        return _courseid.getName();
    }

    @Override
    public String getThumbnailpath() {
        return _courseid.getThumbnailpath();
    }

    @Override
    public Types getType() {
        return Types.GroupRequest;
    }

    public User getRequestor(String myuserid) throws Resources.NotFoundException {
        GroupRequestMember member = findCurrentUserAsMember(myuserid);
        if (member.get_requestorid() != null) {
            return member.get_requestorid();
        } else {
            throw new Resources.NotFoundException("Requestor was not found for user");

        }
    }

    public boolean isAccepted(String myuserid) throws Resources.NotFoundException {
        for (GroupRequestMember member : members) {
            if (member.get_userid().get_id().equals(myuserid)) return member.getAccept();
        }
        throw new Resources.NotFoundException("Requestor was not found for user");

    }

    /**
     * Checks if the provided userid is the inital creator of the request
     *
     * @param myuserid
     * @return
     */
    public boolean isInitiator(String myuserid) throws Resources.NotFoundException {
        GroupRequestMember tmp = findCurrentUserAsMember(myuserid);
        if (tmp.get_requestorid()!=null&&tmp.get_userid()!=null&&tmp.get_userid().get_id().equals(tmp.get_requestorid().get_id())) return true;
        else return false;

    }

    public GroupRequestMember findCurrentUserAsMember(String myuserid) throws Resources.NotFoundException {
        for (GroupRequestMember grpm :
                members) {
            if (grpm.get_userid().get_id().equals(myuserid)) return grpm;
        }
        throw new Resources.NotFoundException("User was not found in Members");
    }
}
