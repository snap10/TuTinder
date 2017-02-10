package tutinder.mad.uulm.de.tutinder.models;

import java.util.List;

/**
 * Created by Snap10 on 04.05.16.
 */
public class Faculty {

    private String _id;
    private String name;
    //If Faculty is in Course
    private Institute institute;
    //If General Faculty a List is provided...
    private List<Institute> institutes;


    public Faculty(String _id, String name, Institute institute, List<Institute> institutes) {
        this._id = _id;
        this.name = name;
        this.institutes = institutes;
        this.institute = institute;
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

    public Institute getInstitute() {
        return institute;
    }

    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public List<Institute> getInstitutes() {
        return institutes;
    }

    public void setInstitutes(List<Institute> institutes) {
        this.institutes = institutes;
    }

    public int getCourseCount() {
        int courseCount = 0;
        for (Institute i :
                institutes) {
            courseCount += i.getCourses().size();
        }
        return courseCount;
    }
}

