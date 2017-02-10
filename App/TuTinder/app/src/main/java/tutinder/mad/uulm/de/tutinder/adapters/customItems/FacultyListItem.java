package tutinder.mad.uulm.de.tutinder.adapters.customItems;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Faculty;

/**
 * Created by Snap10 on 04.05.16.
 */
public class FacultyListItem implements ParentListItem {
    Faculty faculty;
    private List<Course> courseList;

    public FacultyListItem(Faculty faculty,List<Course> courseList) {
        this.faculty=faculty;
        this.courseList = courseList;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }

    @Override
    public List<Course> getChildItemList() {

        return courseList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
