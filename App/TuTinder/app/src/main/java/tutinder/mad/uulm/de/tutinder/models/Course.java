package tutinder.mad.uulm.de.tutinder.models;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * Created by Snap10 on 29.04.16.
 */
public class Course implements CustomListitem {

    private String _id;
    private String name;
    private String term;
    private String lecturer;
    private String description;
    private Faculty faculty;
    private Date created;
    private String cloudinarypicturepath;
    private String picturepath;
    private String thumbnailpath;
    private String instituteid;
    private String facultyid;
    private String[] enrolledusers;
    private User[] enrolleduserobjects;
    //IF Timeslots for FUll course Object
    private String[] timeslots;
    private String[] chosentimeslots;
    private Timeslot[] timeslotobjects;
    // Timeslots for Users internal Courseobject
    private Timeslot[] chosentimeslotobjects;
    private int maxgroupsize;


    /**
     * Creates a new Course-Object.
     *
     * @param name
     * @param term
     * @param faculty
     * @param lecturer
     * @param description
     * @param created
     * @param cloudinarypicturepath
     * @param picturepath
     * @param enrolledusers
     * @param timeslots
     * @param chosentimeslots
     * @param maxgroupsize
     */

    public Course(String _id, String name, String term, Faculty faculty, String lecturer, String description, Date created, String cloudinarypicturepath, String picturepath, String[] enrolledusers, String[] timeslots, String[] chosentimeslots, int maxgroupsize) {
        this._id = _id;
        this.name = name;
        this.term = term;
        this.faculty = faculty;
        this.lecturer = lecturer;
        this.description = description;
        this.created = created;
        this.cloudinarypicturepath = cloudinarypicturepath;
        this.picturepath = picturepath;
        this.enrolledusers = enrolledusers;

        this.timeslots = timeslots;
        this.chosentimeslots = chosentimeslots;
        this.maxgroupsize = maxgroupsize;
    }

    public Course(String courseid) {
        this._id = courseid;
    }

    public Course() {

    }

    /*
        Getters and Setters
     */

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
        return getTerm();
    }

    public String getThumbnailpath() {
        if (cloudinarypicturepath != null) {
            String tmppath = cloudinarypicturepath;
            int divider = tmppath.lastIndexOf("upload/") + 7;
            String firstPiece = tmppath.substring(0, divider);
            String lastPiece = tmppath.substring(divider);
            String imageConversion = "w_100,h_100,c_thumb/";
            thumbnailpath = firstPiece + imageConversion + lastPiece;
            return thumbnailpath;
        } else {
            return null;
        }

    }

    @Override
    public Types getType() {
        return Types.COURSE;
    }

    public void setThumbnailpath(String thumbnailpath) {
        this.thumbnailpath = thumbnailpath;
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

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }


    public String getLecturer() {
        return lecturer;
    }

    public void setLecturer(String lecturer) {
        this.lecturer = lecturer;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getPicturepath(String apiroot) {
        if (picturepath == null) {
            return cloudinarypicturepath;
        } else {
            return picturepath;

        }
    }

    public void setPicturepath(String picturepath) {
        this.picturepath = picturepath;
    }


    public String[] getEnrolledusers() {
        if (enrolledusers == null && enrolleduserobjects != null) {
            enrolledusers = new String[enrolleduserobjects.length];
            for (int i = 0; i < enrolleduserobjects.length; i++) {
                enrolledusers[i] = enrolleduserobjects[i].get_id();
            }
        }
        return enrolledusers;
    }

    public User[] getEnrolledusers(boolean asUsers) {
        return enrolleduserobjects;
    }

    public void setEnrolledusers(String[] enrolledusers) {
        this.enrolledusers = enrolledusers;
    }

    public void setEnrolledusers(User[] enrolledusers) {
        this.enrolleduserobjects = enrolledusers;
    }


    public int getMaxgroupsize() {
        return maxgroupsize;
    }

    public void setMaxgroupsize(int maxgroupsize) {
        this.maxgroupsize = maxgroupsize;
    }

    public String getCloudinarypicturepath() {
        return cloudinarypicturepath;
    }

    public void setCloudinarypicturepath(String cloudinarypicturepath) {
        this.cloudinarypicturepath = cloudinarypicturepath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(String[] timeslots) {
        this.timeslots = timeslots;
    }

    public String[] getChosentimeslots() {
        return chosentimeslots;
    }

    public void setChosentimeslots(String[] chosentimeslots) {
        this.chosentimeslots = chosentimeslots;
    }

    public void addTimeSlot(Timeslot timeslot) {
        if (timeslotobjects != null) {
            timeslotobjects = Arrays.copyOf(timeslotobjects, timeslotobjects.length + 1);
            timeslotobjects[timeslotobjects.length - 1] = timeslot;
        } else {
            timeslotobjects = new Timeslot[]{timeslot};
        }
        String[] tmpslots = new String[timeslotobjects.length];
        for (int i = 0; i < timeslotobjects.length; i++) {
            tmpslots[i] = timeslotobjects[i].get_id();
        }
        setTimeslots(tmpslots);

    }

    /**
     * Listener can return NULL if no Timeslots there to get from Server...
     *
     * @param volleySingleton
     * @param helper
     * @param context
     * @param listener
     */
    public void getTimeslotObjects(VolleySingleton volleySingleton, Tutinder helper, final Context context, final CourseTimeslotListener listener) {
        if (timeslotobjects != null && timeslots != null && timeslotobjects.length >= timeslots.length) {
            listener.onTimeslotObjectsLoaded(timeslotobjects);
        } else {
            if (timeslots != null && timeslots.length > 0) {
                String querystring = getQuerystringForTimeslotIds(timeslots);
                GsonRequest<Timeslot[]> tsRequest = new GsonRequest<>(Request.Method.GET, volleySingleton.getAPIRoot() + "/variables/timeslots" + querystring, null, Timeslot[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Timeslot[]>() {
                    @Override
                    public void onResponse(Timeslot[] response) {
                        if (response != null) {
                            timeslotobjects = response;
                        }
                        listener.onTimeslotObjectsLoaded(timeslotobjects);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "TODO Error getting Timeslots", Toast.LENGTH_LONG).show();
                    }
                });
                volleySingleton.addToRequestQueue(tsRequest);
            } else {
                listener.onTimeslotObjectsLoaded(null);
            }
        }
    }

    /**
     * Builds a Querystring for the provided Timeslotids
     *
     * @param timeslots
     * @return
     */
    private String getQuerystringForTimeslotIds(String[] timeslots) {
        if (timeslots.length > 0) {
            //Build querystring to have the timeslots loaded...
            String querystring = "?tsid=";
            for (int i = 0; i < timeslots.length; i++) {
                if (i < timeslots.length - 1)
                    querystring += timeslots[i] + ",";
                else {
                    querystring += timeslots[i];
                }
            }
            return querystring;

        } else {
            return "";
        }
    }

    /**
     * Listener can return NULL if no Timeslots there to get from Server...
     *
     * @param volleySingleton
     * @param helper
     * @param context
     * @param courseid
     * @param listener
     */
    public void getChosenTimeslotObjects(VolleySingleton volleySingleton, Tutinder helper, final Context context, String courseid, final CourseChosenTimeslotListener listener) {
        if (chosentimeslotobjects != null && chosentimeslots != null && chosentimeslotobjects.length >= chosentimeslots.length) {
            listener.onChosenTimeslotObjectsLoaded(chosentimeslotobjects);
        } else {
            if (chosentimeslots != null && chosentimeslots.length > 0) {
                String querystring = getQuerystringForTimeslotIds(chosentimeslots);
                GsonRequest<Timeslot[]> tsRequest = new GsonRequest<>(Request.Method.GET, volleySingleton.getAPIRoot() + "/variables/timeslots" + querystring, null, Timeslot[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Timeslot[]>() {
                    @Override
                    public void onResponse(Timeslot[] response) {
                        if (response != null) {
                            chosentimeslotobjects = response;
                        }
                        listener.onChosenTimeslotObjectsLoaded(chosentimeslotobjects);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "TODO Error getting ChosenTimeslots", Toast.LENGTH_LONG).show();
                    }
                });
                volleySingleton.addToRequestQueue(tsRequest);
            } else {
                listener.onChosenTimeslotObjectsLoaded(null);
            }
        }
    }


    public String getFacultyid() {
        return facultyid;
    }

    public void setFacultyid(String facultyid) {
        this.facultyid = facultyid;
    }

    public String getInstituteid() {
        return instituteid;
    }

    public void setInstituteid(String instituteid) {
        this.instituteid = instituteid;
    }

    public void removeTimeslot(String id) {
        //Remove from idlist
        List<String> temp = Arrays.asList(timeslots);
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).equals(id)) {
                temp.remove(i);
            }
        }
        timeslots = temp.toArray(new String[temp.size()]);
        //RemoveFrom Objects
        List<Timeslot> tmpObj = Arrays.asList(timeslotobjects);
        for (int i = 0; i < tmpObj.size(); i++) {
            if (tmpObj.get(i).get_id().equals(id)) {
                tmpObj.remove(i);
            }
        }
        timeslotobjects = tmpObj.toArray(new Timeslot[tmpObj.size()]);
    }


    public interface CourseTimeslotListener {
        /**
         * Can be Null if there are no Timeslots to get from the Server
         *
         * @param timelostobjects
         */
        void onTimeslotObjectsLoaded(Timeslot[] timelostobjects);
    }


    public interface CourseChosenTimeslotListener {
        /**
         * Can be Null if there are no Timeslots to get from the server...
         *
         * @param chosentimeslotobjects
         */
        void onChosenTimeslotObjectsLoaded(Timeslot[] chosentimeslotobjects);
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof Course) {
            Course c2 = (Course) o;
            return this.get_id().equals(c2.get_id());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return get_id().hashCode();
    }
}
