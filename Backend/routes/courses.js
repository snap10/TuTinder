var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var Course = require('../models/course');
var User = require('../models/user');
var Faculty = require('../models/faculty');
var Institute = require('../models/institute');
var Group = require('../models/group');
var Match = require('../models/match');
var GroupRequest = require('../models/group_request');
var crypto = require('crypto');
var authController = require('../controllers/auth');
var url = require('url');
var util = require('util');
var mkdirp = require('mkdirp');
var formidable = require('formidable');
//var imageProcessor = require('lwip');
var cloudinary = require('cloudinary');
var gcm = require('node-gcm');

// Set up the sender with you API key
var sender = new gcm.Sender('<TODO insert your own gcm key>');

cloudinary.config({
    //TODO insert your own credentials
    cloud_name: 'xxxx',
    api_key: 'xxx',
    api_secret: 'xxxxx'
});
var sendGCM = function (receivingusers, initiatinguser, courseid, grouprequestid, notificationtext, title, action, coursename, groupid, ismember) {
    console.log(initiatinguser);
    var message = new gcm.Message({
        priority: "high",
        data: {
            userid: initiatinguser._id || "userid not provided",
            username: initiatinguser.name || "Name not provided",
            usermatrikelnr: initiatinguser.matrikelnr || "Matrikelnr not provided",
            userpicture: initiatinguser.profilepicturepath || "no picturepath povided",
            courseid: courseid || "Courseid not provided",
            coursename: coursename || "Courseid not provided",
            action: action || "action not provided",
            grouprequestid: grouprequestid,
            text: notificationtext,
            groupid: groupid,
            //states that the user is member in the gropu(important for groupactivity in android)
            ismember: ismember,
        }


    });
    var gcmtokens = []
    receivingusers.forEach(function (user) {
        if (user.gcmtoken) gcmtokens.push(user.gcmtoken);
    });
    if (gcmtokens && gcmtokens.length > 0) {
        sender.send(message, {registrationTokens: gcmtokens}, function (err, response) {
            if (err) console.error("Error Sending GCM" + err);
            else console.log("Sent GCM Message" + util.inspect(response, false, null));
        });
    } else {
        console.log("Could not send GCM because no tokens were provided...");
    }
}
/*
 * GET /courses
 * Responds with array of all course objects.
 */
router.get('/', authController.isAuthenticated, function (req, res) {
    Course.find(function (err, courses) {
        if (err) res.status(500).send(err);
        else res.json(courses);
    });
});


/*
 * GET /courses/find?q=<string>
 * Finds all courses with the given string in their name.
 * Search is case-insensitive.
 * Responds with array of course objects.
 */
router.get('/find', authController.isAuthenticated, function (req, res) {
    var q = url.parse(req.url, true).query.q;
    Course.find({'name': {'$regex': q, '$options': 'i'}}, function (err, course_array) {
        if (err) res.status(500).send(err);
        else res.status(200).send(course_array);
    });
});

/*
 * DELETE /courses/find?q=<string>
 * Finds and deletes all courses with the given string in  their name.
 * Response is either empty or an error
 * //TODO from Ferdi: Does that funciton make sense ?
 */
router.delete('/find', authController.isAuthenticated, function (req, res) {
    var q = url.parse(req.url, true).query.q;
    Course.find({'name': {'$regex': q, '$options': 'i'}}, function (err, course_array) {
        if (err) res.status(500).send(err);
        else {
            for (var i in course_array) {
                course_array[i].remove(function (err) {
                    if (err) res.status(500).send(err);
                    else res.send('');
                });
            }
        }
    });
});

/*
 * GET /courses/faculties
 * Finds all distinct faculties.
 * Responds with array of faculty IDs or error.
 */
router.get('/faculties', authController.isAuthenticated, function (req, res) {
    Course.find().distinct('faculty._id', function (err, faculties) {
        if (err) res.status(500).send(err);
        else res.json(faculties);
    });
});

/*
 * GET /courses/faculties/<id>
 * Finds courses within given faculty.
 * Responds with array of course objects or error.
 */
router.get('/faculties/:facultyid', authController.isAuthenticated, function (req, res) {
    Course.find({'faculty._id': req.params.facultyid}, function (err, courses) {
        if (err) res.status(500).send(err);
        else res.json(courses);
    });
});

/*
 * GET /courses/terms
 * Finds all distinct terms.
 * Responds with array of term names or error.
 */
router.get('/terms', authController.isAuthenticated, function (req, res) {
    Course.find().distinct('term', function (err, terms) {
        if (err) res.status(500).send(err);
        else res.json(terms);
    });
});

/*
 * GET /courses/terms/<name>
 * Finds courses within given term.
 * Responds with array of course objects or error.
 */
router.get('/terms/:termname', authController.isAuthenticated, function (req, res) {
    Course.find({'term': req.params.termname}, function (err, courses) {
        if (err) res.status(500).send(err);
        else res.json(courses);
    });
});

/*
 * POST /courses
 * Creates a new course.
 * Request body must contain keys
 * a) "name"
 * b) "term"
 * c) "instituteid"
 * c) "facultyid"
 * and can contain keys
 * d) "lecturer"
 * e) "picturepath"
 * f) "maxgroupsize"
 * g) "password"
 * Responds with full course object or error.
 */
router.post('/', authController.isAuthenticated, function (req, res) {
    authController.isAdmin(req.user, function (err, isadmin) {
        if (err) res.status(401).send(err.message);
        else {
            console.log("Is admin, can perform operation");
            console.log(util.inspect(req.body, false, null));

            if (req.body.name && req.body.term && req.body.instituteid && req.body.facultyid) {
                Faculty.findById(req.body.facultyid, function (err, faculty) {
                    if (err) {
                        console.log("Faculty_notFound")
                        res.status(400).send('Faculty not found.');
                        // ignore mongoose err because uninformative
                    } else {
                        var course = new Course();
                        course.name = req.body.name;
                        course.term = req.body.term;
                        course.timeslots = req.body.timeslots;
                        course.maxgroupsize = req.body.maxgroupsize;
                        var institute = faculty.institutes.id(req.body.instituteid);
                        if (!institute) {
                            console.log("Institue not Found");
                            res.status(400).send('Institute not found.');
                            return;
                        }
                        course.faculty = {
                            _id: faculty._id,
                            name: faculty.name,
                            institute: {
                                _id: institute._id,
                                name: institute.name,

                            }
                        };
                        course.lecturer = req.body.lecturer;
                        course.picturepath = req.body.picturepath;

                        if (req.body.password)
                            course.hashedpassword = crypto.createHash('sha1').update(req.body.password).digest('hex');
                        console.log(course);
                        course.save(function (err, ncourse) {
                            if (err) {
                                console.log("error on saving course" + err.message);
                                res.status(500).send(err.message);
                                return;
                            }
                            Faculty.findOneAndUpdate({
                                _id: req.body.facultyid,
                                'institutes._id': req.body.instituteid
                            }, {
                                $push: {
                                    'institutes.$.courses': {
                                        _id: ncourse._id,
                                        name: ncourse.name
                                    }
                                }
                            }, {new: true}, function (err, faculty) {
                                if (err) console.log("error on updating Faculty");
                            });

                            console.log(ncourse);
                            res.json(ncourse);
                        });
                    }
                });
            } else {
                console.log("Please provide name, term, insituteid and facultyid")
                res.status(400).send('Please provide name, term, instituteid and facultyid to create course');
            }
        }
    });
});

/**
 * Get all requests for the requesting user
 */
router.get("/grouprequests", authController.isAuthenticated, function (req, res) {
    GroupRequest.find({'members._userid': req.user._id}).populate('_courseid members._userid members._requestorid').exec(function (err, requests) {
        if (err) {
            console.log("error finding grouprequests" + err.message);
            res.status(500).send("error finding grouprequests" + err.message);
        } else if (!requests) {
            console.log("error finding grouprequests" + err.message);
            res.status(404).send("error finding grouprequests" + err.message);
        } else {
            res.json(requests);
        }
    });
});

/**
 * Get all requests for the requesting user in the specific course
 */
router.get("/grouprequests/:requestid", authController.isAuthenticated, function (req, res) {
    GroupRequest.findById(req.params.requestid).populate('_courseid members._userid members._requestorid').exec(function (err, requests) {
        if (err) {
            console.log("error finding grouprequests" + err.message);
            res.status(500).send("error finding grouprequests" + err.message);
        } else if (!requests) {
            console.log("error finding grouprequests" + err.message);
            res.status(404).send("error finding grouprequests" + err.message);
        } else {
            res.json(requests);
        }
    });
});

/**
 * Get all requests for the requesting user in the specific course
 */
router.get("/:courseid/grouprequests/", authController.isAuthenticated, function (req, res) {
    GroupRequest.find({
        _courseid: req.params.courseid,
        members: {$elemMatch: {_userid: req.user._id}}
    }).populate('_courseid members._userid members._requestor').exec(function (err, requests) {
        if (err) {
            console.log("error finding grouprequests" + err.message);
            res.status(500).send("error finding grouprequests" + err.message);
        } else if (!requests) {
            console.log("error finding grouprequests" + err.message);
            res.status(404).send("error finding grouprequests" + err.message);
        } else {
            res.json(requests);
        }
    });
});


/*
 * GET /courses/<id>
 * Responds with course object or error.
 */
router.get('/:courseid', authController.isAuthenticated, function (req, res) {
    Course.findOne({_id: req.params.courseid}, function (err, course) {
        if (err) res.status(500).send(err);
        else if (!course) res.status(404).send('Course not found.' + req.params.courseid);
        else res.json(course);
    });
});

/*
 * GET /courses/<id>/maxgroupsize
 * Responds with "maxgroupsize":"integer" or error.
 */
router.get('/:courseid/maxgroupsize', authController.isAuthenticated, function (req, res) {
    Course.findOne({_id: req.params.courseid}, function (err, course) {
        if (err) res.status(500).send(err);
        else if (!course) res.status(404).send('Course not found.' + req.params.courseid);
        else if (course.maxgroupsize) {
            res.json({maxgroupsize: course.maxgroupsize});
        } else {
            res.status(204).send("No maxgroupsize provided for course");
        }
    });
});


/*
 * GET /courses/<id>/groupquantity
 * Responds with "maxgroupsize":"integer" or error.
 */
router.get('/:courseid/groupquantity', authController.isAuthenticated, function (req, res) {
    Group.count({course: req.params.courseid}, function (err, quantity) {
        if (err) res.status(500).send(err);
        else if (quantity) {
            res.json({groupquantity: quantity});
        } else {
            quantity = 0;
            res.json({groupquantity: quantity});
        }
    });
});


/*
 * PUT /courses/<id>
 * Updates an existing course object.
 * Request body can contain any key defined in course schema.
 * Response is either empty or an error.
 */
router.put('/:courseid', authController.isAuthenticated, function (req, res) {
    authController.isAdmin(req.user, function (err, isadmin) {
        if (err) res.status(401).send(err.message);
        else {
            Course.findOne({_id: req.params.courseid}, function (err, course) {
                if (err) res.status(500).send(err);
                else if (!course) res.status(404).send('Course not found.');
                else {
                    for (var key in req.body) course[key] = req.body[key];
                    course.save(function (err, course, numAffected) {
                        if (err) res.status(500).send(err);
                        else res.status(200).send(course);
                    });
                }
            });
        }
    });
});

/*
 * DELETE /courses/<id>
 * Deletes an existing course object.
 * Response is either empty or an error.
 */
router.delete('/:courseid', authController.isAuthenticated, function (req, res) {
    Course.findOne({_id: req.params.courseid}, function (err, course) {
        if (err) res.status(500).send(err);
        else if (!course) res.status(404).send('Course not found.');
        else {
            course.remove(function (err) {
                if (err) res.status(500).send(err);
                else res.status(200).send('');
            });
        }
    });
});

router.post('/:courseid/uploadpicturetocloud', authController.isAuthenticated, function (req, res) {
    var staticPath = 'images/users/' + req.params.courseid + '/';
    var path = __dirname + '/../public/' + staticPath;
    mkdirp(path + "fullsize/", function (err) {
        if (err) res.status(500).send(err);
        else {

            var form = new formidable.IncomingForm();
            form.on('fileBegin', function (name, file) {
                console.log('Starting upload process for ' + file.name);
                file.name = new Date().getTime() + "_" + file.name;
                file.path = path + "fullsize/" + file.name;
            });
            form.on('file', function (name, file) {
                console.log("Starting Uploading to Cloudinary...")
                cloudinary.uploader.upload(file.path, function (result) {
                    console.log(util.inspect(result, false, null));
                    var cloudinaryPath = result.url;
                    console.log(util.inspect(cloudinaryPath, false, null));
                    Course.findByIdAndUpdate(req.params.courseid, {
                        cloudinarypicturepath: result.url
                    }, {new: true}, function (err, course) {
                        if (err) res.status(500).send(err);
                        else {

                            res.status(200).send(course);
                        }
                    });
                    User.update({courses: {$elemMatch: {_id: req.params.courseid}}}, {'courses.$.cloudinarypicturepath': result.url}, {multi: true}, function (err, users) {
                        if (err) {
                            console.error("error" + err.message);
                        } else {
                            console.log(users);
                        }
                    });

                }, {
                    public_id: "courses/" + req.params.courseid + "/" + file.name.substring(0, file.name.lastIndexOf('.')),
                    use_filename: true,
                    width: 1500,
                    height: 1500,
                    crop: "limit"
                });
            });
            // start formidable
            form.parse(req);
        }
    });
});

router.get('/:courseid/timeslots', authController.isAuthenticated, function (req, res) {
    Course.findOne({_id: req.params.courseid}, {'timeslots': 1}).populate('timeslots').exec(function (err, course) {
        if (err) res.status(500).send(err);
        else if (!course) res.status(404).send('Course not found.');
        else {
            if (course.timeslots) {
                res.json(course.timeslots);
                console.log("Timeslots have been sent:" + course.timeslots);
            } else {
                res.status(204).send("No Timeslots have been set for the course");
            }
        }
    });
});


router.delete('/:courseid/timeslots/:timeslotid', authController.isAuthenticated, function (req, res) {
    Course.findOneAndUpdate({_id: req.params.courseid}, {$pull: {timeslots: req.params.timeslotid}}).populate('timeslots').exec(function (err, course) {
        if (err) res.status(500).send(err);
        else if (!course) res.status(404).send('Course not found.');
        else {
            if (course.timeslots) {
                res.json(course.timeslots);
                console.log("Timeslots have been sent:" + course.timeslots);
            } else {
                res.status(204).send("No Timeslots have been set for the course");
            }
        }
    });
});


/**
 * @Author Snap10
 * accept or deny a request with given requestid
 */
router.put('/grouprequests/:requestid', authController.isAuthenticated, function (req, res) {
    // Removes user from group request
    var doDeny = function () {
        GroupRequest.findOneAndUpdate({_id: req.params.requestid}, {
            $pull: {members: {_userid: req.user._id}}
        }, {new: true}, function (err, request) {
            if (err) {
                console.error(err);
                res.statuts(500).send("error while deny grouprequest");
            } else {
                res.status(200).send('Removed you from the group request.');
                if (request && request.members.length < 2) {
                    request.remove(function (err) {
                        if (err)console.log("error deleting request" + err.message);
                    });
                }
            }
        });
    }
    // Creates group or adds user to group based on number of accepts
    var doAccept = function () {
        var sendResponseGroupIsFull = function () {
            res.status(400).send('Cannot accept group request. Group is full.');
        }
        // Update the group request
        GroupRequest.findOneAndUpdate({
                _id: req.params.requestid,
                members: {$elemMatch: {_userid: req.user._id}} // members array projection
            },
            {$set: {'members.$.accept': true}}, // update accept to true
            {new: true} // return the updated document in the exec callback
        ).exec(function (err, request) {
            if (err) {
                res.status(500).send(err);
                return;
            } else if (!request) {
                res.status(404).send('Group request not found');
                return;
            }
            // Collect all accepting members
            var memberids = [];
            for (var i in request.members) if (request.members[i].accept) memberids.push(request.members[i]._userid);
            // If there is only 1 accept, thats an error
            if (memberids.length == 1) {
                console.log('Error: A user just accepted a group request that has only 1 accept (group request became unconsistent).');
                return;
            }
            // If there are exactly 2 accepts, a new group is created.
            if (memberids.length == 2) {
                Group.findOneAndUpdate({
                        course: request._courseid, // group has reference of the course
                        grouprequest: request._id // group has reference of the grouprequest
                    }, {
                        $push: {users: {$each: memberids}} // push both users to group members array
                    }, {
                        upsert: true, // create group (if non-existing)
                        new: true // return the new document in the exec callback
                    }
                ).populate('users course').exec(function (err, group) {
                        if (err || !group) {
                            console.log('Error: There were 2 group request accepts but group creation failed.');
                            res.status(500).send('Server failed to create your group.\nError: ' + err);
                            return;
                        }
                        sendGCM(
                            group.users,
                            req.user,
                            group.course._id,
                            req.params.requestid,
                            req.user.name + " in " + group.course.name,
                            'Someone formed a new group with you.',
                            'tutinder.mad.uulm.de.tutinder.intent.action.new_groupmember',
                            group.course.name, group._id, true
                        );
                        var groupid = group._id;
                        //Send groupid of group for Intent in Android
                        res.json(groupid);
                    }
                );
                return;
            }
            // If there are more than 2 accepts, a group already exists and the user is added
            if (memberids.length > 2) {
                // Get the course to check the maxgroupsize
                Course.findOne({_id: request._courseid}, function (err, course) {
                    if (err) {
                        res.status(500).send(err);
                        return;
                    } else if (!course) {
                        res.status(404).send('Course not found.');
                        return;
                    } else if (course.maxgroupsize && (course.maxgroupsize <= memberids.length)) {
                        sendResponseGroupIsFull();
                        return;
                    }
                    // Get the group to add the user
                    Group.findOneAndUpdate({
                            course: request._courseid,
                            grouprequest: request._id
                        }, {
                            $addToSet: {users: {$each: memberids}} // adds user only if not present
                        }, {
                            new: true // return the updated document in the exec callback
                        }
                    ).populate('users course').exec(function (err, group) {
                        if (err) {
                            res.status(500).send(err);
                        } else if (!group) {
                            res.status(400).send('Group not found');
                        } else {
                            group.users = group.users.filter(function (user) {
                                return !(String(user._id) === String(req.user._id));
                            })
                            sendGCM(
                                group.users,
                                req.user,
                                group.course._id,
                                req.params.requestid,
                                req.user.name + " in " + group.course.name,
                                'Someone joined your group.',
                                'tutinder.mad.uulm.de.tutinder.intent.action.new_groupmember',
                                group.course.name, group._id, true
                            );
                            res.status(200).send('Joined the group.');
                        }
                    });
                });
            }
        });
    };
    // Tests, if the user has already accepted another request in this course
    var testAlreadyOtherAccept = function (request) {
        GroupRequest.findOne({
            _courseid: request._courseid,
            members: {$elemMatch: {_userid: req.user._id, accept: true}}
        }, function (err, grouprequest) {
            if (err) {
                res.status(500).send(err);
                return;
            } else if (grouprequest && !(String(grouprequest._id) === String(req.params.requestid))) {
                console.log("already accepted a grouprequest in this course");
                res.status(409).send("Already send a grouprequest")
            }
            else doAccept();
        });
    };
    // Tests, if the user is already in a group in this course
    var testGroup = function (request) {
        Group.findOne({course: request._courseid, users: req.user._id}, function (err, group) {
            if (err) {
                res.status(500).send(err);
                return
            } else if (group) {
                res.status(409).send("You are already in a Group in this course, Groupid:" + group._id);
                console.log("already in a group in this course");
            }
            else
                testAlreadyOtherAccept(request);
        });
    };
    // Get the group request that the user wants to answer
    GroupRequest.findOne({
        _id: req.params.requestid
    }, function (err, grouprequest) {
        if (err) {
            res.status(500).send(err);
            return;
        }
        if (!grouprequest) {
            res.status(404).send('Group request not found.');
            return;
        }
        if (!(req.body.accept)) {
            doDeny();
            return;
        }
        testGroup(grouprequest); // starts the test-chain
    });

});


/**
 * Create a GroupRequest
 * @author Snap10
 * Creates a new GropuRequest or updates if another one is already there
 * Checks if user has been requested before and if match or friendship is in place
 *
 */
router.post('/:courseid/users/:userid/grouprequest', authController.isAuthenticated, function (req, res) {
    var storeAndNotify = function () {
        store(function (err, grouprequest) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            if (grouprequest) {
                notify(grouprequest);
                console.log("sending request");
                res.send(grouprequest);
            }
        });
    }
    var notify = function (request) {
        var receivers = [];
        User.findById(req.params.userid, function (err, user) {
            if (user) {
                receivers.push(user);
                sendGCM(
                    receivers,
                    req.user,
                    req.params.courseid,
                    request._id,
                    req.user.name + " in " + request._courseid.name,
                    "Someone wants you to join a TuTinder group",
                    "tutinder.mad.uulm.de.tutinder.intent.action.new_group_request",
                    request._courseid.name
                );
            }
        });
    };
    var store = function (cb) {
        GroupRequest.findOneAndUpdate({
                _courseid: req.params.courseid,
                members: {$elemMatch: {_userid: req.user._id, accept: true}}
            }, {
                $push: {members: {_userid: req.params.userid, _requestorid: req.user._id, accept: false}}
            },
            {new: true}).populate('_courseid members._userid members._requestorid').exec(function (err, grouprequest) {
            if (err) cb(err);
            else if (!grouprequest) {
                console.log("no grouprequest could be found, we create one");
                grouprequest = new GroupRequest();
                grouprequest._courseid = req.params.courseid;
                // Both users are stored in the request object
                grouprequest.members = [
                    {_userid: req.user._id, _requestorid: req.user._id, accept: true},
                    {_userid: req.params.userid, _requestorid: req.user._id, accept: false}
                ];
                grouprequest.save(function (err) {
                    if (err) cb(err);
                    else {
                        //Query for request to have users populated...
                        GroupRequest.findById(grouprequest._id).populate('_courseid members._userid, members._requestorid').exec(function (err, grouprequest2) {
                            if (err) cb(err);
                            else cb(null, grouprequest2);
                        });
                    }
                });
            } else {
                console.log("Grouprequest:" + grouprequest);
                cb(null, grouprequest);
            }
        });
    };

    var testFriends = function () {
        User.findOne({
            _id: req.params.userid,
            friends: req.user._id
        }, 'friends.$', function (err, user) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            else if (!user) res.status(409).send('Users must have matched or be friends.');
            else {
                storeAndNotify();
            }

        });
    };
    var testMatch = function () {
        Match.findOne({
            _courseid: req.params.courseid,
            _userid: req.user._id,
            matches: req.params.userid
        }, 'matches.$', function (err, match) {
            console.log("Match:" + match);
            if (err) {
                console.error(err);
                res.status(500).send(err);
                return;
            }
            else if (match) {
                storeAndNotify();
            }
            else testFriends();
        });
    };
    //TODO TEST !!!
    // Test if the current user is already in a full group in this course
    var testGroup2 = function() {
        // Find group in this course, that contains current user
        Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            if (!group) {
                // OK: current user is not in a group when creating this request
                testMatch();
                return;
            }
            // Test if group is full
            // - Find course to get maxgroupsize
            Course.findOne({_id: req.params.courseid}, function (err, course) {
                if (err) {
                    res.status(500).send(err);
                    return;
                }
                if (!course) {
                    console.log('Error creating group request: Course not found.');
                    res.status(500).send(err);
                    return;
                }
                if (course.maxgroupsize == group.users.length) {
                    // group is full
                    res.status(412).send('You cannot send a group request, if you already are in a full group.');
                    return;
                } else {
                    // OK: still space in this group, so request can be sent, continue with match test
                    testMatch();
                }
            });
        });
    }
    var testGroup = function () {
        // Test if the course already has a group with the receiving user as member
        Group.findOne({course: req.params.courseid, users: req.params.userid}, function (err, group) {
            if (err) {
                res.status(500).send(err);
                return;
            } else if (group) {
                res.status(409).send('Receiving user must not already be in a group in this course.');
                return;
            } else {
                console.log("No group found, we test matches now)")
                // testMatch();
                testGroup2();

            }
        });
    };
    /**
     * Tests if the user has already been requested by this user...
     */
    var testAlreadyRequested = function () {
        GroupRequest.findOne({
            _courseid: req.params.courseid, $and: [
                {members: {$elemMatch: {_userid: req.user._id, accept: true}}},
                {'members._userid': req.params.userid}
            ]
        }).exec(function (err, request) {
            if (err) res.status(500).send(err);
            else if (request) {
                res.status(409).send("User has already been requested");
                return;
            } else testGroup();
        });
    };
    Course.findOne({
        _id: req.params.courseid,
        enrolledusers: req.user._id,
        enrolledusers: req.params.userid
    }, function (err, course) {
        if (err)res.status(500).send(err);
        else if (!course) res.status(400).send('Both users must be enrolled in this course');
        else testAlreadyRequested();
    });
})
;


/**
 * Join a Grouprequest
 * @author Snap10
 * Places the requesting user in the grouprequest as false with the requestorid set to the requesting users id
 * Some Groupmember has to accept that request.
 * Checks if user is already in a group and whether the group even exists.
 *
 *
 */
//TODO Testing
router.post('/:courseid/groups/:groupid/grouprequest', authController.isAuthenticated, function (req, res) {
    var storeAndNotify = function (group) {
        store(group, function (err, grouprequest) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            if (grouprequest) {
                notify(grouprequest);
                console.log("sending request");
                res.send(grouprequest);
            }
        });
    }
    var notify = function (request) {
        var receivers = [];
        request.members.forEach(function (member) {
            /**
             * Probably not working
             */
            if (!(String(member._userid._id) === String(req.user._id)) && member.accept) receivers.push(member._userid);
        });
        sendGCM(
            receivers,
            req.user,
            req.params.courseid,
            request._id,
            req.user.name + " in " + request._courseid.name,
            "Someone wants you to join a TuTinder group",
            "tutinder.mad.uulm.de.tutinder.intent.action.new_group_request",
            request._courseid.name
        );
    };
    /**
     *
     * @param group
     * @param cb
     */
    var store = function (group, cb) {
        GroupRequest.findOneAndUpdate({
                _id: group.grouprequest
            }, {
                //Push user as accept:false and with same requestorid to mark him as asking from external
                $push: {members: {_userid: req.user._id, _requestorid: req.user._id, accept: false}}
            },
            {new: true}).populate('_courseid members._userid members._requestorid').exec(function (err, grouprequest) {
            if (err) cb(err);
            else if (!grouprequest) {
                console.log("no grouprequest could be found");
                cb("no grouprequest could be found");
            } else {
                console.log("Grouprequest:" + grouprequest);
                cb(null, grouprequest);
            }
        });
    };
    var testMatch = function () {
        Match.findOne({
            _courseid: req.params.courseid,
            _groupid: req.params.groupid,
            matches: req.user._id
        }).populate("_groupid").exec(function (err, match) {
            console.log("Match:" + match);
            if (err) {
                console.error(err);
                res.status(500).send(err);
                return;
            }
            else if (match) {
                storeAndNotify(match._groupid);
            } else {
                console.error("No Match found");
                res.status(403).send("No Match found");
            }

        });
    };
    var testGroup = function () {
        // Test if the course already has a group with the requesting user
        Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
            if (err) {
                res.status(500).send(err);
                return;
            } else if (group) {
                res.status(409).send("Already in a Group in this course");
                return;
            } else {
                console.log("No group found, we test matches now)")
                testMatch();

            }
        });
    };
    /**
     *Tests if the group exists, and whether there it is not full
     */
    var findGroup = function (course) {
        Group.findOne({_id: req.params.groupid}, function (err, group) {
            if (err) {
                console.error(err.message);
                res.status(500).send(err.message);
            } else if (!group) {
                console.error("Group does not exist");
                res.status(404).send("Group does not exist");

            } else {
                if (group.users.length >= course.maxgroupsize) {
                    res.status(409).send("Group is already full");
                } else {
                    testGroup();
                }
            }
        });
    }
    Course.findOne({
        _id: req.params.courseid,
        enrolledusers: req.user._id,
    }, function (err, course) {
        if (err)res.status(500).send(err);
        else if (!course) res.status(400).send('User must be enrolled in the course');
        else findGroup(course);
    });
});


//TODO Route to have a group accept an other requesting user...

router.post('/:courseid/groups/:groupid/user/:userid', authController.isAuthenticated, function (req, res) {
    Group.findOneAndUpdate({_id: req.params.groupid}, {$addToSet: {users: req.params.userid}}, {new: true}, function (err) {
        if (err) res.status(500).send(err);
        else {
            res.json({success: true});
        }
    });
});


module.exports = router;
