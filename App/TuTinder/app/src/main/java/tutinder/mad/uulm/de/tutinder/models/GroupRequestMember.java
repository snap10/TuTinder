package tutinder.mad.uulm.de.tutinder.models;

/**
 * Created by Snap10 on 04.07.16.
 */
public class GroupRequestMember {
    private String _id;
    private User _userid;
    private User _requestorid;
    private Boolean accept;

    public GroupRequestMember() {
    }

    public GroupRequestMember(String _id, User _userid, User requestorid, Boolean accept) {
        this._id = _id;
        this._userid = _userid;
        _requestorid = requestorid;
        this.accept = accept;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public User get_userid() {
        return _userid;
    }

    public void set_userid(User _userid) {
        this._userid = _userid;
    }

    public Boolean getAccept() {
        return accept;
    }

    public void setAccept(Boolean accept) {
        this.accept = accept;
    }

    public User get_requestorid() {
        return _requestorid;
    }

    public void set_requestorid(User _requestorid) {
        this._requestorid = _requestorid;
    }
}
