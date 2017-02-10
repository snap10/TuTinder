/**
 * Created by Snap10 on 27.04.16.
 */
var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var User = require('../models/user');
var Course = require('../models/course');
var FriendRequest = require('../models/friend_request');
var GroupRequest = require('../models/group_request');
var Group = require('../models/group');
var seneca = require('seneca');
var authController = require('../controllers/auth');
var util = require('util');
var fs = require('fs');
var mkdirp = require('mkdirp');
var formidable = require('formidable');
var cloudinary = require('cloudinary');
var gcm = require('node-gcm');


// Set up the sender with you API key
var sender = new gcm.Sender('<TODO insert your own gcm key>');


cloudinary.config({
    //TODO insert own credentials
    cloud_name: 'xxx',
    api_key: 'xxxx',
    api_secret: 'xxxx'
});



var sendGCM = function (receivingusers, initiatinguser, courseid, groupid, notificationtext, title, action, coursename) {
    console.log(receivingusers);
    var message = new gcm.Message({
        priority: "high",
        data: {
            userid: initiatinguser._id || "userid not provided",
            username: initiatinguser.name || "Name not provided",
            usermatrikelnr: initiatinguser.matrikelnr || "Matrikelnr not provided",
            userpicture: initiatinguser.profilepicturepath || "no picturepath povided",
            courseid: courseid || "Courseid not provided",
            coursename: coursename || "Coursename not provided",
            action: action || "action not provided",
            groupid: groupid || "groupid is not provided",
            text: notificationtext || "text is not provided"
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


/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Userinformation...
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */

/* GET users listing. */
router.get('/', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.user._id}, function (err, user) {
        if (err) {
            console.log(err);
            res.send('Database error');
        } else res.json(user);
    });
});


/* Put users information. */
router.put('/', authController.isAuthenticated, function (req, res) {

    User.findById(req.user._id, function (err, user) {
        if (user) {
            // Update each attribute with any possible attribute that may have been submitted in the body of the request
            // If that attribute isn't in the request body, default back to whatever it was before.

            user.name = req.body.name || user.name;
            user.email = req.body.email || user.email;
            user.phone = req.body.phone || user.phone;
            user.description = req.body.description || user.description;
            user.studycourse = req.body.studycourse || user.studycourse;
            user.personalitytags = req.body.personalitytags || user.personalitytags;
            user.interesttags = req.body.interesttags || user.interesttags;
            user.profilepicturepath = req.body.profilepicturepath || user.profilepicturepath;
            user.profilethumbnailpath = req.body.profilethumbnailpath || user.profilethumbnailpath;
            if (req.body.newPassword) {
                console.log("updating password");
                user.hashedpassword = req.body.newPassword
            }
            console.log(user);
            user.save({new: true}, function (err, user) {
                if (err) {
                    console.log("Error while putting new user:" + err);
                    res.send(err);
                }
                else res.json(user);
            });
        } else {
            console.log(err);
        }
    });
});


/* Put gcmtoken to user information. */
router.put('/gcmtoken/:token', authController.isAuthenticated, function (req, res) {
    User.update({gcmtoken: req.params.token}, {$set: {gcmtoken: null}}, {multi: true}, function (err, numAffected) {
        if (err) console.log("Error, while trying to delete old GCMTokens:" + err);
        else console.log("#GCMTokens deleted:" + numAffected.n);
        User.findByIdAndUpdate(req.user._id, {gcmtoken: req.params.token}, function (err, user) {
            if (err) {
                console.log("Update GCMToken Error:" + err);
                res.status(500).send("Error updating GCM Token in Database");
            } else {
                res.send("GCM Token updated");

            }
        });
    });

});
/* Put gcmtoken to user information. */
router.delete('/gcmtoken', authController.isAuthenticated, function (req, res) {
    User.findByIdAndUpdate(req.user._id, {gcmtoken: null}, function (err, user) {
        if (err) {
            console.log("Update GCMToken Error:" + err);
            res.status(500).send("Error updating GCM Token in Database");
        } else {
            res.send("GCM Token deleted");
        }
    });
});


/*Delete the user*/
router.delete('/', authController.isAuthenticated, function (req, res) {
    //TODO test that findandremove
    User.findByIdAndRemove(req.user._id, function (err, user) {
        if (err) {
            console.log(err);
            res.send('Database error');
        } else res.json('Removed user' + user.id);
    });
});

router.get('/login', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.user._id}, function (err, user) {
        if (err) {
            console.log(err);
            res.send('Database error');
        } else res.json(user);
    });
});
/* POST should create a New Users and respond with the new created ID and Information*/
router.post('/register', function (req, res) {

    if (!(req.body.matrikelnr && req.body.password)) {
        console.log('Someone tried to register without matrikelnr and Password' + req);
        res.status(400).send('Error please provide matrikelnr and password');
    } else {
        //Check if User is already in Database
        console.log(req.body.matrikelnr);
        User.findOne({matrikelnr: req.body.matrikelnr}, function (err, user) {
            console.log(user);
            if (user) {
                res.status(409).send('Error user is already present');
            } else {
                console.log(req.body);
                var user = new User();      // create a new instance of the Bear model
                user.name = req.body.name;  // set the bears name (comes from the request)
                user.matrikelnr = req.body.matrikelnr;
                user.hashedpassword = req.body.password;
                user.studycourse = req.body.studycourse;
                user.phone = req.body.phone;
                user.description = req.body.description;
                user.email = req.body.email;
                user.personalitytags = req.body.personalitytags;
                user.interesttags = req.body.interesttags;
                user.securitygroup = 0;
                //TODO handle possible other fields...
                // save the and check for errors
                user.save(function (err, user) {
                    if (err) {
                        res.send(err);
                    }
                    else res.json(user);
                });
            }
        });
    }
});

/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Pictures...
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */


//TODO router.put('/profilepicture) ... to have the profilepicture updated individually of the userdetails

router.post('/userpicture', authController.isAuthenticated, function (req, res) {
    var staticPath = 'images/users/' + req.user._id + '/';
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
                    User.findByIdAndUpdate(req.user.id, {
                        $addToSet: {
                            cloudinarypicturepaths: result.url
                        }
                    }, {new: true}, function (err, user) {
                        if (err) res.status(500).send(err);
                        else {

                            res.status(200).send(user);
                        }
                    });
                }, {
                    public_id: "users/" + req.user._id + "/" + file.name.substring(0, file.name.lastIndexOf('.')),
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

router.delete('/userpicture/:picturepath', authController.isAuthenticated, function (req, res) {
    var originalpath = decodeURI(req.params.picturepath);
    var path = originalpath.substr(originalpath.indexOf('/users/') + 1);
    var public_id = path.substring(0, path.lastIndexOf('.'));
    console.log(path);
    console.log(public_id);

    cloudinary.uploader.destroy(public_id, function (result) {
        console.log(result)
        if (result) {
            User.findOneAndUpdate({_id: req.user._id}, {$pull: {cloudinarypicturepaths: originalpath}}, {new: true}, function (err) {
                    if (err) {
                        console.log("Error deleting path on DB: " + err);
                        res.send(err);
                    } else {
                        res.send("Picture deleted!");
                        //Check if Picture was set as ProfilePicture
                        User.findOneAndUpdate({
                            _id: req.user._id,
                            profilepicturepath: originalpath
                        }, {
                            $unset: {profilepicturepath: "", profilethumbnailpath: ""}

                        }, {new: true}, function (err, user) {
                            if (err) {
                                console.log("Error deleting profilepicturepath on DB: " + err);
                            } else if (!user) {
                                console.log("Path was not a ProfilePicturePath");
                            } else {
                                console.log("ProfilePicturepath was deleted, too");
                                //Check if Picture was set as ProfilePicture

                            }

                        });
                    }
                },
                {invalidate: true});
        }
    });
});


/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Courses...
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */


/**
 * Provide the CourseIDs and Name
 */
router.get('/courses', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.user._id}).select('courses').exec(function (err, courselist) {
        if (err) {
            console.log(err);
            res.status(500).send(err.message);
        }
        else
            res.json(courselist);
    });
});


/**
 Enroll into the course with the provided ID
 */

router.post('/courses/:courseid/enroll', authController.isAuthenticated, function (req, res) {
    // find by document id and update
    Course.findOne({_id: req.params.courseid, 'enrolledusers': req.user._id}, function (err, course) {
        if (course) {
            console.log("course" + course);
            res.status(409).send("Error, User is already in Course");
        }
        else {
            console.log("User not in Course yet");
            Course.findByIdAndUpdate({_id: req.params.courseid}, {$addToSet: {enrolledusers: req.user._id}}, {
                    safe: true,
                    new: true
                }, function (err, course) {
                    if (err)
                        console.log(err)
                    else {
                        User.findById(req.user._id, function (err, user) {
                            if (err)
                                console.log(err);
                            else {
                                user.courses.push({
                                    _id: course._id,
                                    name: course.name,
                                    term: course.term,
                                    faculty: {
                                        _id: course.faculty._id,
                                        name: course.faculty.name,
                                        institute: {
                                            _id: course.faculty.institute._id,
                                            name: course.faculty.institute.name,
                                        }
                                    },
                                    cloudinarypicturepath: course.cloudinarypicturepath,
                                    picturepath: course.picturepath,
                                    thumbnailpath: course.thumbnailpath
                                });
                                user.save({new: true}, function (err, user) {
                                    if (err) {
                                        console.log("Error while updating user with new Courseinformation:" + err);
                                        res.status(500).send(err);
                                    }
                                    else res.json(user);
                                });
                            }
                        });
                    }
                }
            );
        }
    });

});


/**
 * Withdraw from the Course with the provided ID
 */
router.post('/courses/:courseid/leave', authController.isAuthenticated, function (req, res) {
    // find by document id and update
    Course.findByIdAndUpdate(
        req.params.courseid,
        {
            $pull: {
                enrolledusers: req.user._id
            }
        },
        {safe: true, upsert: true, new: true},
        function (err, course) {
            if (err)
                console.log(err)
            else {
                User.findByIdAndUpdate(req.user._id, {$pull: {courses: {_id: req.params.courseid}}}, {new: true}, function (err, user) {
                    res.json(user);
                })
            }
        }
    );


});

/**
 * @returns the Timeslots for the provided courseid of that user...
 */
router.get('/courses/:courseid/timeslots', authController.isAuthenticated, function (req, res) {
    console.log(req.params.courseid);
    User.findOne({
        _id: req.user._id,
        courses: {$elemMatch: {_id: req.params.courseid}},
    }, {'courses.$.chosentimeslots': 1}).populate('courses.chosentimeslots').exec(function (err, userWithTimeslots) {
        if (err) {
            console.error(err.message);
            res.status(500).send(err);
        }
        else if (!userWithTimeslots) res.status(404).send('Course not found.');
        else {
            if (userWithTimeslots.courses[0].chosentimeslots) {
                res.json(userWithTimeslots.courses[0].chosentimeslots);
                console.log("Timeslots have been sent:" + userWithTimeslots.courses[0].chosentimeslots);
            } else {
                res.status(204).send("No Timeslots have been set for the course");
            }
        }
    });
});

/**
 * Sets the timeslots for the provided Courseid in the userobject...
 * @requires application/json timeslots:["id1","id2","id..."]
 * @returns Timeslots as Objects.
 */
router.put('/courses/:courseid/timeslots', authController.isAuthenticated, function (req, res) {

    if (req.body.timeslots) {
        console.log("Timeslots provided, check if valid");
        console.log(req.body.timeslots);
        Course.findOne({_id: req.params.courseid}, function (err, course) {
            if (err) {
                console.error(err.message);
                res.status(500).send(err);
            } else if (!course) res.status(404).send('Course not found.');
            else {
                var timeslotsLengthProvided = req.body.timeslots.length;
                var timeslots = req.body.timeslots.filter(function (ts) {
                    return ((course.timeslots.indexOf(ts)) >= 0);
                });
                console.log("Filtered out " + (timeslotsLengthProvided - timeslots.length) + " wrong Timeslots");
                console.log(timeslots);
                User.findOneAndUpdate({
                    _id: req.user._id,
                    courses: {$elemMatch: {_id: req.params.courseid}},
                }, {$set: {'courses.$.chosentimeslots': timeslots}}, {
                    select: 'courses',
                    new: true,
                    upsert: true
                }).exec(function (err, userWithTimeslots) {
                    if (err) {
                        console.error(err.message);
                        res.status(500).send(err);
                    }
                    else if (!userWithTimeslots) res.status(404).send('Course not found.');
                    else {
                        //Search for course because course.$ does not work with findAndUPdate...
                        for (var i in userWithTimeslots.courses) {
                            if (String(userWithTimeslots.courses[i]._id) === String(req.params.courseid)) {
                                //Return object if greater 0
                                if (userWithTimeslots.courses[i].chosentimeslots.length > 0) {
                                    res.json(userWithTimeslots.courses[i].chosentimeslots);
                                } else {
                                    res.status(204).send("No Timeslots have been set for the course");
                                }
                                console.log("Timeslots have been sent:" + userWithTimeslots.courses[i].chosentimeslots);
                                break;
                            }
                        }
                    }

                });

            }
        });
    } else {
        console.log("No Timeslots provided, can't put them");
        res.status(400).send("No Timeslots provided, can't put them");
    }

});


/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Groups...
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */

/**
 * Provide the GroupIds and Coursenames Information
 */
router.get('/groups', authController.isAuthenticated, function (req, res) {
    Group.find({users: req.user._id}, function (err, groups) {
        if (err) res.status(500).send(err);
        else {
            console.log("send groups to user: " + req.user._id);
            res.json(groups);
        }
    });
});

/**
 * Provide the GroupIds and Coursenames Information
 */
router.get('/groups/:groupid', authController.isAuthenticated, function (req, res) {
    Group.findOne({_id: req.params.groupid}, function (err, group) {
        if (err) res.status(500).send(err);
        else {
            console.log("send groups to user: " + req.user._id);
            res.json(group);
        }
    });
});


/**
 * Provide the GroupIds and Coursenames Information
 */
router.get('/groups/:groupid/messages', authController.isAuthenticated, function (req, res) {
    Group.findOne({_id: req.params.groupid}, function (err, group) {
        if (err) res.status(500).send(err);
        else if (!group) {
            console.log("send groups to user: " + req.user._id);
            res.status(404).send("NO group found");
        } else {
            console.log("send groups to user: " + req.user._id);
            res.json(group.messages);
        }
    });
});

/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Messageing
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */

/**
 * get all messages within the group...
 */
router.get('/groups/:groupid/messages', authController.isAuthenticated, function (req, res) {
    Group.findOne({_id: req.params.groupid, users: req.user._id}).exec(function (err, group) {
        if (err) res.status(500).send(err);
        else {
            console.log("send messages to user " + req.user._id);
            res.json(group.messages);
        }
    });
});
/**
 * post a new message
 * Minimum:
 * { "message":{
 *      "text":"testhere",
 *      }
 * }
 *
 * optional: created
 */
router.post('/groups/:groupid/messages', authController.isAuthenticated, function (req, res) {
    if (req.body.message) {
        var message = req.body.message;
        message.user = req.user._id;
        Group.findOneAndUpdate({
            _id: req.params.groupid,
            users: req.user._id
        }, {$push: {messages: message}}, {new: true}).populate('course users').exec(function (err, group) {
            if (err) {
                console.error("Error posting message" + err.message);
                res.status(500).send("Error posting message" + err.message);
            } else if (!group) {
                console.error("Error posting message" + err.message);
                res.status(500).send("Error posting message" + err.message);
            } else {
                res.json(group.messages);
                var receivingusers = group.users.filter(function (user) {
                    if (String(user._id) === String(req.user._id)) return false;
                    else return true;
                });
                sendGCM(receivingusers, req.user, group.course._id, group._id, message.text,
                    "Message: " + req.user.name,
                    'tutinder.mad.uulm.de.tutinder.intent.action.new_message', group.course.name);
            }
        });
    } else {
        res.status(400).send("Please provide valid message document in body");
    }
});


/**
 * Provide the GroupIds and Coursenames Information
 */
router.get('/grouprequests', authController.isAuthenticated, function (req, res) {
    GroupRequest.find({users: {$elemMatch: {_userid: req.user._id}}}).exec(function (err, grouprequests) {
        if (err) res.status(500).send(err);
        else {
            res.json(grouprequests);
            console.log("send grouprequests to user: " + req.user._id);
        }
    });
});

/**
 * Withdraw from the Group with the provided ID
 */
router.delete('/groups/:groupid/', authController.isAuthenticated, function (req, res) {
    Group.findOneAndUpdate({
        _id: req.params.groupid,
        users: req.user._id
    }, {$pull: {users: req.user._id}}, {new: true}).populate("course users").exec(function (err, group) {
        if (err) {
            console.error(err);
            res.status(500).send(err);
            return;
        }
        else {
            //TODO Test if GroupRequest is properly updated
            GroupRequest.findOneAndUpdate({
                _id: group.grouprequest,
                members: {$elemMatch: {_userid: req.user._id}}
            }, {members: {$pull: {_userid: req.user_id}}}, function (err, grouprequest) {
                if (err) {
                    console.error(err);
                    res.status(500).send(err);
                    return;
                }
                //Send the groupobject to have user verified he is no longer in it
                console.log("User " + req.user._id + "left group" + req.params.groupid);
                res.send("Left Group!");
                var receivingusers = group.users.filter(function (user) {
                    if (String(user._id) === String(req.user._id)) return false;
                    else return true;
                });
                sendGCM(receivingusers, req.user, group.course._id, group._id, null,
                    "Message: " + req.user.name,
                    'tutinder.mad.uulm.de.tutinder.intent.action.member_left', group.course.name);
            });

        }
    })
});
/**
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * Friends...
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 * ****************************************************************************************************************
 */

/**
 * Get friends of the User
 */

router.get('/friends', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.user._id}, 'friends').populate('friends').exec(function (err, users) {
        if (err) {
            console.log(err);
            res.send('Database error');
        } else {
            console.log("Result:" + users);
            res.json(users.friends);

        }
    });
});
/**
 * Get particular Friend of the User
 */

router.get('/friends/:friendid', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.params.friendid}).exec(function (err, friend) {
        if (err) {
            console.log(err);
            res.status(500).send('Database error');
        } else {
            console.log("Result:" + friend);
            if (friend) {
                if (friend.friends.indexOf(req.user._id) > -1) {
                    //In the array!
                    console.log("Friend found, delivering information");
                    res.json(friend);
                } else {
                    //Not in the array
                    console.log("Forbidden to get UserDetails, because you are no friends")
                    res.status(403).send("Forbidden to get UserDetails, because you are no friends");
                }
            } else {
                console.log(400 + "- No User found with id:" + req.params.friendid);
                res.status(400).send("No User found with that id");
            }

        }
    });
});

/* Create to user information. */
router.post('/friends/friendrequest/', authController.isAuthenticated, function (req, res) {
    console.log(req.body.nonce);
    if (req.body.nonce) {
        FriendRequest.findOneAndUpdate({_userid: req.user._id}, {nonce: req.body.nonce}, {
            upsert: true,
            new: true
        }, (function (err, request) {
            if (err) {
                console.log("error on save friendrequest" + err);
                res.status(500).send("Error Creating Friendrequest");
            } else {
                res.send("Request successfully created");
            }
        }));

    } else {
        res.status(400).send("Please provide a nonce parameter");
    }
});
/**
 * Add another user as a Friend
 */
router.post('/friends/:friendid/nonce/:nonce', authController.isAuthenticated, function (req, res) {
    FriendRequest.findOne({_userid: req.params.friendid}, function (err, request) {
        if (err) {
            console.log("DB Error on finding FriendRequest" + err);
            res.status(500).send("DB Error on finding FriendRequest");
            return;
        }
        if (!request) {
            console.log("No Pending FriendRequest found");
            res.status(400).send("No Pending FriendRequest found");
            return;
        } else {
            if (!(request.nonce == req.params.nonce)) {
                console.log("Wrong Nonce was provided for user" + req.params.friendid + " by " + req.user._id);
                res.status(403).send("Provided Wrong Nonce to add Friend");
                return;
            }
            request.remove(function (err) {
                if (err)  console.log("Error Deleting used PendingFriendrequestObject");
            });
            User.findOne({_id: req.params.friendid}, function (err, friend) {
                if (err) {
                    console.log(err);
                    res.send('Database error');
                } else {
                    User.findByIdAndUpdate(req.user._id,
                        {$addToSet: {friends: friend._id}},
                        {safe: true, upsert: true},
                        function (err, rawResponse) {
                            if (err) {
                                console.log(err)
                            }
                            else {
                                User.findByIdAndUpdate(friend._id, {$addToSet: {friends: req.user._id}}, {
                                    new: true,
                                    safe: true,
                                    upsert: true
                                }, function (err, friend) {
                                    if (err) {
                                        console.log("Error resolving Friend information:" + err)
                                    } else {
                                        console.log("Prepairing Message");
                                        var message = new gcm.Message({
                                            priority: "high",
                                            delayWhileIdle: true,
                                            data: {
                                                friendid: req.user._id,
                                                friendname: req.user.name,
                                                friendpicture: req.user.profilethumbnailpath,
                                                action: "tutinder.mad.uulm.de.tutinder.intent.action.new_friend_request",
                                                message: "You have a new Friendrequest from " + req.user._id.toString()
                                            }
                                            /*
                                             ,
                                             notification: {
                                             title: "Friendrequest",
                                             icon: "ic_tutinder_250dp",
                                             body: "You have a new Friendrequest",
                                             click_action: "tutinder.mad.uulm.de.tutinder.intent.action.notification_new_friend_request",
                                             sound: "content://settings/system/notification_sound"
                                             }
                                             */
                                        });
                                        // Now the sender can be used to send messages
                                        if (friend.gcmtoken) {
                                            var regTokens = [friend.gcmtoken];
                                            sender.send(message, {registrationTokens: regTokens}, function (err, response) {
                                                if (err) console.error("GCM Message could not be sent: " + err);
                                                else    console.log("Sent GCM Message" + util.inspect(response, false, null));
                                            });
                                        } else {
                                            console.log("Error: Could not notify friend because no gcmtoken was defined for him");
                                        }

                                        res.send('Added friend');
                                    }
                                });
                            }
                        });
                }
            });
        }
    });
});


/**
 * Add another user as a Friend
 */
router.delete('/friends/:friendid', authController.isAuthenticated, function (req, res) {

    User.findByIdAndUpdate(req.user._id,
        {$pull: {friends: req.params.friendid}},
        {safe: true},
        function (err, rawResponse) {
            if (err) {
                console.log(err)
            }
            else {
                User.findByIdAndUpdate(req.params.friendid, {$pull: {friends: req.user._id}}, function (err, rawResponse) {
                    if (err) {
                        console.log(err)
                    } else {
                        res.send('Deleted friend');
                    }
                })
            }
        });

    // find by document id and update

});

module.exports = router;
