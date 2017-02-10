package tutinder.mad.uulm.de.tutinder.models;

import java.util.Date;

/**
 * Created by Lukas on 27.06.2016.
 */
public class Message {
    private String _id;
    private String text;
    private String user;
    private Date created;

    public Message(String _id, String text, String user, Date created) {
        this._id = _id;
        this.text = text;
        this.user = user;
        this.created = created;
    }

    public Message(String text, String user, Date created) {
        this.text = text;
        this.user = user;
        this.created = created;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
