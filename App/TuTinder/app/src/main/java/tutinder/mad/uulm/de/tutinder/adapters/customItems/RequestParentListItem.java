package tutinder.mad.uulm.de.tutinder.adapters.customItems;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.GroupRequest;
import tutinder.mad.uulm.de.tutinder.models.User;

/**
 * Created by Snap10 on 11.06.16.
 */
public class RequestParentListItem implements ParentListItem {

    private Course course;
    private List<GroupRequest> groupRequests;


    public RequestParentListItem(Course course, List<GroupRequest> groupRequests) {
        this.course = course;
        this.groupRequests = groupRequests;
    }

    /**
     * @param course
     * @param groupid
     * @param matches
     * @param groupRequests
     */
    public RequestParentListItem(Course course, String groupid, List<User> matches, List<GroupRequest> groupRequests) {

        this.course = course;
        this.groupRequests = groupRequests;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public List<GroupRequest> getChildItemList() {

        return groupRequests;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public List<GroupRequest> getGroupRequests() {
        return groupRequests;
    }

    public void setGroupRequests(List<GroupRequest> groupRequests) {
        this.groupRequests = groupRequests;
    }

    public void addRequest(GroupRequest groupRequest) {
        if (groupRequests != null) {
            groupRequests.add(groupRequest);
        } else {
            groupRequests = new ArrayList<>();
            groupRequests.add(groupRequest);
        }
    }
}
