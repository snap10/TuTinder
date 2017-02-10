package tutinder.mad.uulm.de.tutinder.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.configs.SecurityGroups;
import tutinder.mad.uulm.de.tutinder.utils.ImageHelper;

/**
 * Created by Snap10 on 29.04.16.
 */
public class User implements CustomListitem {

    private String _id;
    private int matrikelnr;
    private int securitygroup;
    private String name;
    private String password;
    private String phone;
    private String studycourse;
    private String email;
    private String profilepicturepath;
    private String profilethumbnailpath;
    private String[] cloudinarypicturepaths;
    private String[] cloudinarythumbnailpaths;
    private String description;
    private String[] personalitytags;
    private List<String> enrolledCourseIds;


    private String[] friends;
    private List<Group> groups;
    private List<Course> courses;
    private String newPassword;


    public User(String _id, int matrikelnr, int securitygroup, String name, String password, String phone, String studycourse, String email, String profilepicturepath, String[] cloudinarypicturepaths, String description, String[] personalitytags, String[] friends, List<Group> groups, List<Course> courses) {

        this._id = _id;
        this.matrikelnr = matrikelnr;
        this.securitygroup = securitygroup;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.studycourse = studycourse;
        this.email = email;
        this.profilepicturepath = profilepicturepath;
        this.cloudinarypicturepaths = cloudinarypicturepaths;

        this.description = description;
        this.personalitytags = personalitytags;
        this.friends = friends;
        this.groups = groups;
        this.courses = courses;
    }

    /**
     * Checks if user is already enrolled in the Course with the given ID
     *
     * @param courseId
     * @return
     */
    public Boolean isEnrolledInCourse(String courseId) {
        if (enrolledCourseIds == null) {
            enrolledCourseIds = new ArrayList<>();
            for (Course c : courses) {
                enrolledCourseIds.add(c.get_id());
            }
        }
        if (enrolledCourseIds != null && enrolledCourseIds.contains(courseId)) {
            return true;
        }
        return false;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }


    public void setProfilethumbnailpath(String profilethumbnailpath) {
        this.profilethumbnailpath = profilethumbnailpath;
    }

    public String[] getCloudinarythumbnailpaths() {
        return cloudinarythumbnailpaths;
    }

    public void setCloudinarythumbnailpaths(String[] cloudinarythumbnailpaths) {
        this.cloudinarythumbnailpaths = cloudinarythumbnailpaths;
    }


    public String getObjectId() {
        return _id;
    }

    public void setObjectId(String objectId) {
        this._id = objectId;
    }

    public int getMatrikelnr() {
        return matrikelnr;
    }

    public void setMatrikelnr(int matrikelnr) {
        this.matrikelnr = matrikelnr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudycourse() {
        return studycourse;
    }

    public void setStudycourse(String studycourse) {
        this.studycourse = studycourse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getPersonalitytags() {
        return personalitytags;
    }

    public void setPersonalitytags(String[] personalitytags) {
        this.personalitytags = personalitytags;
    }

    public String[] getFriends() {
        return friends;
    }

    public void setFriends(String[] friends) {
        this.friends = friends;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        enrolledCourseIds = new ArrayList<>();
        for (Course c : courses) {
            enrolledCourseIds.add(c.get_id());
        }
    }

    /**
     * @return
     */
    public Map<String, String> getLoginCredentials() {
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("matrikelnr", matrikelnr + "");
        credentials.put("password", password);
        return credentials;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getProfilepicturepath() {
        if (profilepicturepath == null && cloudinarypicturepaths.length > 0) {
            profilepicturepath = cloudinarypicturepaths[0];
            return cloudinarypicturepaths[0];
        }
        return profilepicturepath;

    }

    public String getProfileThumbnailPath() {
        if (profilethumbnailpath == null && cloudinarypicturepaths.length > 0) {
            return ImageHelper.getThumbnailFrom(cloudinarypicturepaths[0], 100, 100);
        }
        return profilethumbnailpath;
    }

    public String getProfileThumbnailPath(int w, int h) {
        if (profilethumbnailpath == null && cloudinarypicturepaths.length > 0) {
            return ImageHelper.getThumbnailFrom(cloudinarypicturepaths[0], w, h);
        }
        return profilethumbnailpath;

    }

    public void setProfilepicturepath(String profilepicturepath) {
        this.profilepicturepath = profilepicturepath;
        if (this.profilepicturepath != null && this.profilepicturepath.startsWith("http")) {
            int divider = profilepicturepath.lastIndexOf("upload/") + 7;
            String firstPiece = profilepicturepath.substring(0, divider);
            String lastPiece = profilepicturepath.substring(divider);
            String imageConversion = "w_100,h_100,c_thumb/";
            profilethumbnailpath = firstPiece + imageConversion + lastPiece;
        } else {
            profilethumbnailpath = this.profilepicturepath;
        }
    }

    public String[] getCloudinarypicturepaths(boolean asThumbnail) {
        if (asThumbnail) {
            if (cloudinarythumbnailpaths == null || cloudinarythumbnailpaths.length != cloudinarypicturepaths.length) {
                cloudinarythumbnailpaths = cloudinarypicturepaths.clone();
                for (int i = 0; i < cloudinarypicturepaths.length; i++) {
                    String tmppath = cloudinarypicturepaths[i];
                    int divider = tmppath.lastIndexOf("upload/") + 7;
                    String firstPiece = tmppath.substring(0, divider);
                    String lastPiece = tmppath.substring(divider);
                    String imageConversion = "w_100,h_100,c_thumb/";
                    cloudinarythumbnailpaths[i] = firstPiece + imageConversion + lastPiece;
                }
            }
            return cloudinarythumbnailpaths;
        } else {
            return cloudinarypicturepaths;

        }
    }

    public void setCloudinarypicturepaths(String[] cloudinarypicturepaths) {
        this.cloudinarypicturepaths = cloudinarypicturepaths;
    }

    /**
     * Checks if user is enrolled in the course with the given ID
     *
     * @param courseid
     * @return
     */
    public boolean isInCourse(String courseid) {
        for (Course course : courses) {
            if (course.get_id().equals(courseid)) return true;
        }
        return false;
    }

    public int getSecuritygroup() {
        return securitygroup;
    }

    public void setSecuritygroup(int securitygroup) {
        this.securitygroup = securitygroup;
    }

    public boolean isAdmin() {
        return (securitygroup == SecurityGroups.ADMIN);
    }

    @Override
    public String getId() {
        return get_id();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getSubtitle() {
        return getDescription();
    }

    @Override
    public String getThumbnailpath() {
        return getProfileThumbnailPath();
    }

    @Override
    public Types getType() {
        return Types.USER;
    }

    public Course getCourse(String id) {
        for (Course course : courses) {
            if (course.get_id().equals(id)) {
                return course;
            }
        }
        return null;
    }
}
