package tutinder.mad.uulm.de.tutinder.adapters.customItems;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.CustomListitem;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.User;

/**
 * Created by Snap10 on 11.06.16.
 */
public class MatchParentListItem implements ParentListItem {
    private String groupid;
    private Course course;
    private List<User> matchedusers;
    private List<Group> matchedgroups;


    public MatchParentListItem(Course course, List<User> userList, List<Group> matchedgroups) {
        this.course = course;
        this.matchedusers = userList;
        this.matchedgroups = matchedgroups;
    }

    /**
     * @param course
     * @param groupid
     * @param matches
     * @param matchedgroups
     */
    public MatchParentListItem(Course course, String groupid, List<User> matches, List<Group> matchedgroups) {

        this.course = course;
        this.groupid = groupid;
        this.matchedusers = matches;
        this.matchedgroups = matchedgroups;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<User> getMatchedusers() {
        return matchedusers;
    }

    public void setMatchedusers(List<User> matchedusers) {
        this.matchedusers = matchedusers;
    }

    @Override
    public List<CustomListitem> getChildItemList() {
        List<CustomListitem> returnlist = new ArrayList<>();
        returnlist.addAll(matchedusers);
        returnlist.addAll(matchedgroups);
        return returnlist;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public List<Group> getMatchedgroups() {
        return matchedgroups;
    }

    public void setMatchedgroups(List<Group> matchedgroups) {
        this.matchedgroups = matchedgroups;
    }

}
