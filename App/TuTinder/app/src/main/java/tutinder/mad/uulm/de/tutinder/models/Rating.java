package tutinder.mad.uulm.de.tutinder.models;

import java.util.Date;

/**
 * Created by Snap10 on 11.06.16.
 */
public class Rating {
    User _rateduserid;
    boolean like;
    boolean match;
    private Date updatedAt;
    private Date createdAt;

    public Rating() {
    }

    public Rating(User _rateduserid, boolean like, boolean match) {
        this._rateduserid = _rateduserid;
        this.like = like;
        this.match = match;
    }

    public User get_rateduserid() {
        return _rateduserid;
    }

    public void set_rateduserid(User _rateduserid) {
        this._rateduserid = _rateduserid;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
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
