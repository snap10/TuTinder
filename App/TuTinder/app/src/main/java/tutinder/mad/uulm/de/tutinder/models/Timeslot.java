package tutinder.mad.uulm.de.tutinder.models;

import android.content.Context;

import tutinder.mad.uulm.de.tutinder.R;

/**
 * Created by Snap10 on 17.06.16.
 */
public class Timeslot {
    String _id;
    int day;
    String from;
    String to;

    public Timeslot() {
    }

    public Timeslot(String _id, int day, String from, String to) {
        this._id=_id;
        this.day = day;
        this.from = from;
        this.to = to;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFormattedTimeslot(Context context) {
        String timeslot = "";
        switch(day) {
            case 0:
                timeslot += context.getString(R.string.monday);
                break;
            case 1:
                timeslot += context.getString(R.string.tuesday);
                break;
            case 2:
                timeslot += context.getString(R.string.wednesday);
                break;
            case 3:
                timeslot += context.getString(R.string.thursday);
                break;
            case 4:
                timeslot += context.getString(R.string.friday);
                break;
            case 5:
                timeslot += context.getString(R.string.saturday);
                break;
            case 6:
                timeslot += context.getString(R.string.sunday);
                break;
        }

        timeslot += ": " + from + " - " + to;
        return timeslot;
    }
}
