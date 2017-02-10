package tutinder.mad.uulm.de.tutinder.models;

import java.util.Date;
import java.util.List;

/**
 * Created by Snap10 on 11.06.16.
 */
public class Matches {
    String _id;
    String _userid;
    String _groupid;
    Course _courseid;
    List<User> matches;
    List<Group> matchedgroups;
    private Date updatedAt;
    private Date createdAt;

    public Matches() {

    }

    public Matches(String _id, String _userid, String _groupid, Course _courseid, List<User> matches, List<Group> matchedgroups) {
        this._id = _id;
        this._userid = _userid;
        this._groupid = _groupid;
        this._courseid = _courseid;
        this.matchedgroups = matchedgroups;
        this.matches = matches;

    }

    public String get_groupid() {
        return _groupid;
    }

    public void set_groupid(String _groupid) {
        this._groupid = _groupid;
    }

    public List<Group> getMatchedgroups() {
        return matchedgroups;
    }

    public void setMatchedgroups(List<Group> matchedgroups) {
        this.matchedgroups = matchedgroups;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_userid() {
        return _userid;
    }

    public void set_userid(String _userid) {
        this._userid = _userid;
    }

    public Course get_courseid() {
        return _courseid;
    }

    public void set_courseid(Course _courseid) {
        this._courseid = _courseid;
    }

    public List<User> getMatches() {
        return matches;
    }

    public void setMatches(List<User> matches) {
        this.matches = matches;
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
}
