package tutinder.mad.uulm.de.tutinder.models;

import java.util.List;

/**
 * Created by Snap10 on 04.05.16.
 */
public class Institute {

    private String _id;
    private String name;
    private List<Course> courses;


    public Institute(String _id, String name, List<Course> courses) {
        this._id = _id;
        this.name = name;
        this.courses = courses;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
