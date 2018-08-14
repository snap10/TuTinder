/**
 * Created by Snap10 on 27.04.16.
 */
/**
 * Created by Snap10 on 27.04.16.
 */
var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var User = require('../models/user');
var Group = require('../models/group');
var Rating = require('../models/rating');
var Match = require('../models/match');
var Course = require('../models/course');
var authController = require('../controllers/auth');
var util = require('util');
var gcm = require('node-gcm');


// Set up the sender with you API key
var sender = new gcm.Sender('<TODO insert your own gcm key>');

//SENECA SECTION


/*
 * GET /matching/
 * Finds all matchings of the requesting user
 * Responds with an array.
 */
router.get('/', authController.isAuthenticated, function (req, res) {
    Group.find({users: req.user._id}, function (err, groups) {
        var groupids = []
        if (err) {
            console.log("Error loading groups:" + err.message);
            res.status(500).send("Error loading groups:" + err.message);
        }
        if (groups) {
            for (var i in groups) {
                groupids.push(groups[i]);
            }
        }


        Match.find({$or: [{_userid: req.user._id}, {_groupid: {$in: groupids}}]}).populate('_courseid matches matchedgroups').exec(function (err, matches) {
            if (err) res.status(500).send(err);
            else res.json(matches);
        });
    });
});

/*
 Returns the user only if the User has a Match with the requested user...
 */
router.get('/courses/:courseid/users/:userid', authController.isAuthenticated, function (req, res) {
    Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
        var groupid;
        if (err) {
            res.status(500).send(err.message);
            console.error(err);
            return;
        }
        else if (group) {
            console.log("Group for user found");
            groupid = group._id;
        }
        Match.findOne({
            _courseid: req.params.courseid,
            _userid: req.params.userid,
            $or: [{
                'matches': req.user._id
            }, {
                'matchedgroups': groupid
            }]
        }).populate('_userid').exec(function (err, match) {
            if (err) {
                res.status(500).send(err.message);
                console.error(err);
                return;
            }
            else if (!match) {
                console.log("No Match found, we hide user information");
                res.status(403).send("No Match found, so we hide user information");
            } else {
                console.log("Matchinginformation:" + util.inspect(match, false, null));
                console.log("Match found, we can deliver userinformation");
                res.json(match._userid);
                console.log("User information sent");
            }
        });


    });
})
;

/*
 * GET /matching/courses/<id>
 * Finds all ratings of the given course.
 * Responds with matching object or error.
 */

router.get('/courses/:courseid/ratings', authController.isAuthenticated, function (req, res) {
    Rating.findOne({_courseid: req.params.courseid, _userid: req.user._id}, function (err, rating) {
        if (err) res.status(500).send(err);
        else if (!rating) res.status(404).send('No ratings found.');
        else res.json(rating);
    });
});


router.delete('/courses/:courseid/ratings', authController.isAuthenticated, function (req, res) {
    if (!(req.user)) {
        res.status(400).send('');
        return;
    }
    Rating.findOne({
        _courseid: req.params.courseid,
        _userid: req.user._id
    }, function (err, rating) {
        if (err) {
            res.status(500).send(err);
            return;
        }
        if (!rating) {
            console.log("Rating not found...");
            res.status(404).send('Rating not found.');
            return;
        }
        //Check if user is already in a Group, then we don't reset ratings
        Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
            if (err) res.status(500).send(err);
            if (group) {
                console.log("Group for user found, skip resetting groups");
                res.status(400).send("User is already in a Group, cannot reset ratings");
            } else {
                // Remove all ratings that are no match
                var resCode = 200, resMessage = 'Resetted ratings';

                Match.findOne({
                    _courseid: req.params.courseid,
                    _userid: req.user._id
                }, function (err, match) {
                    if (err) {
                        resCode = 500;
                        resMessage = err;
                        return
                    }
                    if (match) {
                        //Users
                        rating.ratings = rating.ratings.filter(function (object) {
                            var match = match.matches.indexOf(object._rateduserid) > 0;
                            if (!match) console.log("Resetting ratings of " + _rateduserid);
                            return !match
                        });
                        rating.rated = rating.rated.filter(function (_rateduserid) {
                            var match = match.matches.indexOf(_rateduserid) > 0;
                            if (!match) console.log("Resetting rated of " + _rateduserid);
                            return !match
                        });
                        rating.groupratings = rating.groupratings.filter(function (object) {
                            var match = match.matchedgroups.indexOf(object._ratedgroupid) > 0;
                            if (!match) console.log("Resetting grouprating of " + _ratedgroupid);
                            return !match
                        });
                        rating.ratedgroups = rating.ratedgroups.filter(function (_ratedgroupid) {
                            var match = match.matchedgroups.indexOf(_ratedgroupid) > 0;
                            if (!match) console.log("Resetting grouprating of " + _ratedgroupid);
                            return !match
                        });
                        rating.markModified('ratings');
                        rating.markModified('rated');
                        rating.markModified('ratedgroups');
                        rating.markModified('groupratings');
                        rating.save();
                        res.status(resCode).send(resMessage);

                    } else {
                        Rating.remove({_id: rating._id}, function (err, rating) {
                            if (err) res.status(500).send("Error while deleting Rating")
                            res.status(resCode).send(resMessage);

                        });
                    }

                });


            }

        });

    })
    ;
})
;

/*
 * DELETE /matching/courses/<id>/groups/<id of requested group>
 * Deletes the rating that the current user gave for the requested group.
 * Request must contain currently logged in user.
 * Responds with 200 OK if rating could be undone.
 * Ratings cannot be undone if users alrady matched.
 * If rating is like or disklike is not important here.
 */
router.delete('/courses/:courseid/ratings/groups/:groupid', authController.isAuthenticated, function (req, res) {
    if (!(req.user)) {
        res.status(400).send('');
        return;
    }
    // Get matching object of requested user
    Match.findOne({
        _courseid: req.params.courseid,
        _groupid: req.params.groupid,
        'matches': req.user._id // Select match with current user
    }, 'matches.$', function (err, match) {
        if (err) {
            res.status(500).send(err);
            return;
        }
        // If match exists send error and return
        if (match) {
            res.status(400).send('You already matched. This cannot be undone.');
            return;
        }
        // Get rating object of current user
        Rating.findOne({
            _courseid: req.params.courseid,
            _userid: req.user._id
        }, function (err, rating) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            if (!rating) {
                res.status(404).send('Rating not found.');
                return;
            }
            // Search rating for the requested user
            var resCode = 404, resMessage = 'Rating not found.';
            for (var i in rating.ratings) {
                if (rating.groupratings[i]._ratedgroupid != req.params.groupid) continue;
                rating.groupratings.splice(i, 1); // Remove array element
                rating.markModified('ratings');
                rating.save();
                resCode = 200;
                resMessage = '';
            }
            if (resCode == 200) {
                resCode = 500;
                resMessage = 'Rating arrays unconsistent!';
            }
            for (var i in rating.ratedgroups) {
                if (rating.ratedgroups[i] != req.params.groupid) continue;
                rating.ratedgroups.splice(i, 1);
                rating.markModified('rated');
                rating.save();
                resCode = 200;
                resMessage = 'Rating was rested sucessfully';
            }
            res.status(resCode).send(resMessage);
        });
    });
});
/*
 * DELETE /matching/courses/<id>/users/<id of requested user>
 * Deletes the rating that the current user gave for the requested user.
 * Request must contain currently logged in user.
 * Responds with 200 OK if rating could be undone.
 * Ratings cannot be undone if users alrady matched.
 * If rating is like or disklike is not important here.
 */
router.delete('/courses/:courseid/ratings/users/:userid', authController.isAuthenticated, function (req, res) {
    if (!(req.user)) {
        res.status(400).send('');
        return;
    }
    // Get matching object of requested user
    Match.findOne({
        _courseid: req.params.courseid,
        _userid: req.params.userid,
        'matches': req.user._id // Select match with current user
    }, 'matches.$', function (err, match) {
        if (err) {
            res.status(500).send(err);
            return;
        }
        // If match exists send error and return
        if (match) {
            res.status(400).send('You already matched. This cannot be undone.');
            return;
        }
        // Get rating object of current user
        Rating.findOne({
            _courseid: req.params.courseid,
            _userid: req.user._id
        }, function (err, rating) {
            if (err) {
                res.status(500).send(err);
                return;
            }
            if (!rating) {
                res.status(404).send('Rating not found.');
                return;
            }

            // Search rating for the requested user
            var resCode = 404, resMessage = 'Rating not found.';
            for (var i in rating.ratings) {
                if (rating.ratings[i]._rateduserid != req.params.userid) continue;
                rating.ratings.splice(i, 1); // Remove array element
                rating.markModified('ratings');
                rating.save();
                resCode = 200;
                resMessage = '';

            }
            if (resCode == 200) {
                resCode = 500;
                resMessage = 'Rating arrays unconsistent!';
            }
            for (var i in rating.rated) {
                if (rating.rated[i] != req.params.userid) continue;
                rating.rated.splice(i, 1);
                rating.markModified('rated');
                rating.save();
                resCode = 200;
                resMessage = 'Rating was rested sucessfully';
            }
            res.status(resCode).send(resMessage);
        });
    });
});


/*
 * GET /matching/courses/<id>/groups
 *
 */
router.get('/courses/:courseid/ratings/groups', authController.isAuthenticated, function (req, res) {
    console.log(new Date().getTime());
    //Check if user is already in a Group, then we don't send other groups to match
    Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
        if (err) res.status(500).send(err);
        if (group) {
            console.log("Group for user found, skip sending groups");
            res.status(423).send("User is already in a Group, cannot request other groups");
        } else {
            Rating.findOne({
                _courseid: req.params.courseid,
                _userid: req.user._id
            }, 'ratedgroups', function (err, idlist) {
                if (err) res.status(500).send(err.message);
                else {
                    Course.findOne({_id: req.params.courseid}, {maxgroupsize: 1}, function (err, course) {
                        if (err) res.status(500).send("error retrieving course");
                        else if (course) {
                            //Get groups for course...
                            Group.find({course: req.params.courseid}, function (err, groups) {
                                    if (err) {
                                        console.log("Error collection groupinformation:" + err.message);
                                        res.status(500).send("Error collection groupinformation:" + err.message);
                                    } else if (!groups) {
                                        console.log("Error collection groupinformation:" + err.message);
                                        res.status(404).send("Error collection groupinformation:" + err.message);
                                    }
                                    if (groups) {
                                        if (course.maxgroupsize) {
                                            console.log("Groupsize before filtering:" + groups.length);
                                            groups = groups.filter(function (group) {
                                                //Filter out groups that are full...
                                                return (group.users.length < course.maxgroupsize);
                                            });
                                            console.log("Groupsize after checking maxgroupsize:" + groups.length);
                                        }
                                        if (idlist && idlist.ratedgroups) {
                                            groups = groups.filter(function (x) {
                                                return (idlist.ratedgroups.indexOf(x._id) < 0);
                                            });
                                        }
                                        console.log("Groupsize after filtering:" + groups.length);
                                        res.json(groups);
                                    }
                                }
                            );
                        } else {
                            console.log("Error collection courseinformation:" + err.message);
                            res.status(404).send("Error collection course:" + err.message);
                        }
                    });
                }
            });
        }
    });
});

/*
 * GET /matching/courses/<id>/users
 *
 */
router.get('/courses/:courseid/ratings/users', authController.isAuthenticated, function (req, res) {
    var filters = [];
    //Filtersections ?personalitythreshold=integer [0 -10]
    try {
        filters.personalitythreshold = req.query.personalitythreshold;
        filters.equaltimeslots = req.query.timeslots;
        console.log("Filters=" + util.inspect(filters, false, null));
    } catch (e) {
        console.log(e);
    }
    Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
        if (err) res.status(500).send(err);
        if (group) {
            console.log("Group for user found, take groupratings");
            Rating.findOne({
                _courseid: req.params.courseid,
                _groupid: group._id
            }, 'rated', function (err, useridlist) {
                Course.findById({_id: req.params.courseid}, 'enrolledusers', function (err, course) {
                    if (err) res.status(500).send(err);
                    else if (!course) res.status(404).send('No enrolled users.');
                    else {
                        Group.find({course: req.params.courseid}, function (err, groups) {
                            if (err) res.status(500).send(err);
                            else {
                                console.log("groups:" + groups.length);
                                var usersingroups = []
                                var diff = [];
                                for (var i in groups) {
                                    console.log("Users in Group: " + groups[i].users);
                                    usersingroups = usersingroups.concat(groups[i].users);
                                }
                                console.log("users already in groups:" + usersingroups);

                                console.log("Start Filtering out Users, currentsize:" + course.enrolledusers.length);
                                diff = course.enrolledusers.filter(function (userid) {
                                    var equal = !(String(userid) === String(req.user._id));
                                    return equal;
                                });
                                console.log("Filtered out requested user._id:" + req.user._id);
                                console.log("Current size:" + diff.length);
                                if (useridlist && useridlist.rated) {
                                    diff = diff.filter(function (userid) {
                                        return (useridlist.rated.indexOf(userid) < 0);
                                    });
                                    console.log("Filtered out already rated users, currentsize:" + diff.length);
                                }
                                if (usersingroups) {

                                    diff = diff.filter(function (userid) {
                                        //check if the user is in a group
                                        for(var i in usersingroups){
                                            if(String(userid)===String(usersingroups[i])) return false;
                                        }
                                        return true;
                                    });
                                    console.log("Filtered out users already in a group, currentsize:" + diff.length);
                                }

                                console.log("Requesting User Objects for userids in List...:");
                                User.find({_id: {$in: diff}}, function (err, users) {
                                    if (err) console.log("Error collection Userinformation:" + err.message);
                                    else {
                                        var finalArray = matchingAlgorithm(req.user, filters, users, course);
                                        res.json(finalArray);
                                        console.log("Sent the UsersArray to the Requester...");
                                    }

                                });

                            }
                        });
                    }

                });
            });
        } else {
            console.log("No Group for user found, take userRatings");
            Rating.findOne({
                _courseid: req.params.courseid,
                _userid: req.user._id
            }, 'rated', function (err, useridlist) {
                Course.findById({_id: req.params.courseid}, 'enrolledusers', function (err, course) {
                    if (err) res.status(500).send(err);
                    else if (!course) res.status(404).send('No enrolled users.');
                    else {
                        Group.find({course: req.params.courseid}, function (err, groups) {
                            if (err) res.status(500).send(err);
                            else {
                                console.log("Groups:" + groups.length);
                                var usersingroups = []
                                var diff = [];
                                for (var i in groups) {
                                    console.log("Users in Group: " + groups[i].users);
                                    usersingroups = usersingroups.concat(groups[i].users);
                                }
                                console.log("users already in groups:" + usersingroups);

                                console.log("Start Filtering out Users, currentsize:" + course.enrolledusers.length);
                                diff = course.enrolledusers.filter(function (userid) {
                                    var equal = !(String(userid) === String(req.user._id));
                                    return equal;
                                });
                                console.log("Filtered out requested user._id:" + req.user._id);
                                console.log("Current size:" + diff.length);
                                if (useridlist && useridlist.rated) {
                                    diff = diff.filter(function (userid) {
                                        return (useridlist.rated.indexOf(userid) < 0);
                                    });
                                    console.log("Filtered out already rated users, currentsize:" + diff.length);
                                }
                                if (usersingroups) {
                                    diff = diff.filter(function (userid) {
                                        //check if the user is in a group
                                        for(var i in usersingroups){
                                            if(String(userid)===String(usersingroups[i])) return false;
                                        }
                                        return true;
                                    });
                                    console.log("Filtered out users already in a group, currentsize:" + diff.length);
                                }

                                console.log("Requesting User Objects for userids in List...:");
                                User.find({_id: {$in: diff}}, function (err, users) {
                                    if (err) console.log("Error collection Userinformation:" + err.message);
                                    else {
                                        var finalArray = matchingAlgorithm(req.user, filters, users, course);
                                        res.json(finalArray);
                                        console.log("Sent the UsersArray to the Requester...");
                                    }

                                });

                            }
                        });
                    }

                });
            });
        }
    });

});

var matchingAlgorithm = function (requestinguser, filters, usersarray, course) {
    var userTags = requestinguser.personalitytags;

    var threshold = filters.personalitythreshold;
    var equaltimeslots = filters.equaltimeslots;
    console.log("threshold " + threshold);
    console.log("Timeslots " + equaltimeslots);
    //Check users for Threshold
    console.log("Start filtering by Matching Algorithm:");

    var diffarray = usersarray;

    if (threshold && threshold > 0 && usersarray && userTags) {
        console.log("PersonalityThreshold set to:" + threshold + ", start filtering Personality");
        var usersarraysizebefore = usersarray.length;
        var count = 0;

        diffarray = usersarray.filter(function (rateuser) {
            count += 1;
            if (rateuser) {
                if (rateuser.personalitytags) {
                    console.log("Filtering user:" + rateuser.name);
                    overlapingtags = rateuser.personalitytags.filter(function (x) {
                        //return true if userTag contains same TAG
                        return !(userTags.indexOf(x) < 0)
                    });
                } else {
                    console.log("Rateuser has no tags" + rateuser);
                    overlapingtags = []
                }
                rateuser.personalityfactor = overlapingtags.length;
                console.log("Threshold:" + threshold)
                console.log("PersonalityMatchingFactor:" + overlapingtags.length);
                //Remove user from List if does not match requested threshold
                if (threshold > rateuser.personalityfactor) {
                    console.log("filtered out user:" + rateuser.name);
                    return false
                } else {
                    console.log("keep user");
                    return true;
                }
            } else {
                console.log("No Rateuser:" + rateuser + ", filtering out...");
                return false;
            }
        });
        console.log("Filtered out " + (usersarraysizebefore - diffarray.length) + " because of PersonalityThreshold");
    }
    //If timeslotfilteropting is selected we check for overlapping timeslots...
    if (filters.equaltimeslots == 1 && diffarray) {
        console.log("Timeslotfilter was set");
        var chosentimeslots = requestinguser.courses.id(course._id).chosentimeslots;
        console.log("MyChosenTimeslots:" + chosentimeslots);
        var courseslots = course.timeslots;
        var diffarraysizebefore = diffarray.length;
        if (chosentimeslots) {
            console.log("chosentimesots are found...");
            diffarray = diffarray.filter(function (rateuser) {
                console.log(rateuser.name);
                var rateuserslots = [];
                for (var i in rateuser.courses) {
                    if (String(rateuser.courses[i]._id) === String(course._id)) {
                        rateuserslots = rateuser.courses[i].chosentimeslots;
                    }
                }
                if (rateuserslots) {
                    console.log("UsersChosenTimeslots" + rateuserslots);
                    //Matrixcheck to find overlapping timeslots...
                    for (var i in rateuserslots) {
                        console.log(rateuserslots[i]);
                        if (!(chosentimeslots.indexOf(rateuserslots[i]) < 0)) {
                            return true;
                        }
                    }
                }
                //Remove user from array, because no overlapping slots were found
                return false;
            });
        }
        console.log("Filtered out " + (diffarraysizebefore - diffarray.length) + " because of missing timeslotoverlap");
    }
    diffarray.sort(function (a, b) {
        return b.personalityfactor - a.personalityfactor
    });
    console.log("Count:" + count);


    console.log("Sending #users:" + diffarray.length);

    return diffarray;

}


/*
 * POST matching/courses/<id>/users/<id>
 * Rate a user.
 */
router.post('/courses/:courseid/ratings/users/:userid/rating', authController.isAuthenticated, function (req, res) {
    //Check if user is already in a Group, then we don't send other groups to match
    function rateAsGroup(group) {
        var match = false;
        if (req.body.like != undefined) {
            Rating.findOne({
                _courseid: req.params.courseid,
                _userid: req.params.userid,
                'groupratings._ratedgroupid': group._id
            }, {'groupratings.$': 1}, function (err, ratings) {
                if (err) {
                    console.log("Error finding ratedGroups Document:" + err);
                    res.status(500).send(err.message);
                    return;
                } else if (ratings) {
                    console.log(ratings);
                    if (ratings.groupratings[0].like && req.body.like) {
                        //Hey there we have a Match
                        console.log("WOhaaa we found a Match and update the other group...");
                        match = true;
                        Match.findOneAndUpdate({
                                _courseid: req.params.courseid,
                                _userid: req.params.userid
                            }, {$addToSet: {matchedgroups: group._id}},
                            {
                                upsert: true, new: true
                            }).populate('_userid _courseid').exec(function (err, matchobj) {
                            if (err)
                                console.log("Bad, updating otherusers match failed..." + err);
                            if (matchobj) {
                                var user = matchobj._userid;
                                console.log("Update of otheruser successfull:" + matchobj._id);

                                console.log("User to notify:" + match._userid);
                                var message = new gcm.Message({
                                    priority: "high",
                                    data: {
                                        matchedid: group._id,
                                        matchedname: "Group in " + matchobj._courseid.name,
                                        matchedmatrikelnr: -1,
                                        courseid: req.params.courseid,
                                        matchedpicture: matchobj._courseid.picturepath,
                                        action: "tutinder.mad.uulm.de.tutinder.intent.action.new_match_with_group"
                                    }

                                });
                                // Now the sender can be used to send messages
                                if (user.gcmtoken) {
                                    var regTokens = [user.gcmtoken];
                                    console.log(regTokens);
                                    sender.send(message, {registrationTokens: regTokens}, function (err, response) {
                                        if (err) console.error("Error Sending GCM" + err);
                                        else    console.log("Sent GCM Message" + util.inspect(response, false, null));
                                    });
                                }

                            }
                        });
                        Match.findOneAndUpdate({
                                _courseid: req.params.courseid,
                                _groupid: group._id
                            }, {$addToSet: {matches: req.params.userid}},
                            {
                                upsert: true, new: true
                            }).exec(function (err, mymatch) {
                            if (err) console.log("Bad, updating groups matchobject failed..." + err);
                        });
                    } else {
                        console.log("To bad, otheruser doesn't like the requestingUser...");
                    }
                }
                Rating.findOneAndUpdate({
                    _courseid: req.params.courseid,
                    _groupid: group._id
                }, {
                    $addToSet: {
                        ratings: {_rateduserid: req.params.userid, like: req.body.like},
                        rated: req.params.userid
                    }
                }, {new: true, upsert: true}, function (err, rating) {
                    if (err)
                        console.log("Error updating requesting User:" + err);
                    else {
                        console.log("Saved Rating");
                        var response = {
                            type: "user",
                            _groupid: group._id,
                            _courseid: req.params.courseid,
                            _rateduserid: req.params.userid,
                            like: req.body.like,
                            match: match
                        }
                        res.json(response);
                    }

                });

            });

        } else {
            res.status(400).send("Please provide a like attribute...!");
        }

    }

    function rateAsUser(req, res) {
        var match = false;
        if (req.body.like != undefined) {
            Rating.findOne({
                _courseid: req.params.courseid,
                _userid: req.params.userid,
                'ratings._rateduserid': req.user._id
            }, {'ratings.$': 1}, function (err, ratings) {
                if (err) {
                    console.log("Error finding ratedusers Document:" + err);
                    res.status(500).send(err.message);
                    return;
                }
                else if (ratings) {
                    console.log(ratings);
                    if (ratings.ratings[0].like && req.body.like) {
                        //Hey there we have a Match
                        console.log("WOhaaa we found a Match and update the other user...");
                        match = true;
                        Match.findOneAndUpdate({
                                _courseid: req.params.courseid,
                                _userid: req.params.userid
                            }, {$addToSet: {matches: req.user._id}},
                            {
                                upsert: true, new: true
                            }).populate('_userid _courseid').exec(function (err, matchobj) {
                            if (err)
                                console.log("Bad, updating otherusers match failed..." + err);
                            if (matchobj) {
                                var user = matchobj._userid;
                                console.log("Update of otheruser successfull:" + matchobj._id);

                                console.log("User to notify:" + match._userid);
                                var message = new gcm.Message({
                                    priority: "high",
                                    data: {
                                        matchedid: req.user._id,
                                        matchedname: req.user.name,
                                        matchedmatrikelnr: req.user.matrikelnr,
                                        courseid: req.params.courseid,
                                        matchedpicture: req.user.profilepicturepath,
                                        action: "tutinder.mad.uulm.de.tutinder.intent.action.new_match"
                                    }

                                });
                                // Now the sender can be used to send messages
                                if (user.gcmtoken) {
                                    var regTokens = [user.gcmtoken];
                                    console.log(regTokens);
                                    sender.send(message, {registrationTokens: regTokens}, function (err, response) {
                                        if (err) console.error("Error Sending GCM" + err);
                                        else    console.log("Sent GCM Message" + util.inspect(response, false, null));
                                    });
                                }

                            }
                        });
                        Match.findOneAndUpdate({
                                _courseid: req.params.courseid,
                                _userid: req.user._id
                            }, {$addToSet: {matches: req.params.userid}},
                            {
                                upsert: true, new: true
                            }).exec(function (err, mymatch) {
                            if (err) console.log("Bad, updating requestingusers match failed..." + err);
                        });
                    } else {
                        console.log("To bad, otheruser doesn't like the requestingUser...");
                    }
                }
                Rating.findOneAndUpdate({
                    _courseid: req.params.courseid,
                    _userid: req.user._id
                }, {
                    $addToSet: {
                        ratings: {_rateduserid: req.params.userid, like: req.body.like},
                        rated: req.params.userid
                    }
                }, {new: true, upsert: true}, function (err, rating) {
                    if (err)
                        console.log("Error updating requesting User:" + err);
                    else {
                        console.log("Saved Rating");
                        var response = {
                            type: "user",
                            _userid: req.user._id,
                            _courseid: req.params.courseid,
                            _rateduserid: req.params.userid,
                            like: req.body.like,
                            match: match
                        }
                        res.json(response);
                    }

                });
            });
        } else {
            res.status(400).send("Please provide a like attribute...!");
        }
    }

    Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
        if (err) res.status(500).send(err);
        if (group) {
            console.log("Rating is counted as grouprating");
            rateAsGroup(group);
        } else {
            console.log("Rating is counted as userrating");
            rateAsUser(req, res);
        }
    });


});

/**
 *
 *
 * @param userids
 * @param req
 */
function notifyusers(userids, req) {
    console.log("Users to notify:" + userids);
    var message = new gcm.Message({
        priority: "high",
        data: {
            matchedid: req.user._id,
            matchedname: req.user.name,
            matchedmatrikelnr: req.user.matrikelnr,
            courseid: req.params.courseid,
            matchedpicture: req.user.profilethumbnailpath,
            action: "tutinder.mad.uulm.de.tutinder.intent.action.new_match"
        }
    });
    User.find({_id: {$in: userids}}, {gcmtoken: 1}, function (err, users) {

        var regTokens = [];
        for (var i in users) {
            if (users[i].gcmtoken) regTokens.push(users[i].gcmtoken)
        }
        console.log(regTokens);
        sender.send(message, {registrationTokens: regTokens}, function (err, response) {
            if (err) console.error("Error Sending GCM" + err);
            else    console.log("Sent GCM Message" + util.inspect(response, false, null));
        });
    });

}
router.post('/courses/:courseid/ratings/groups/:groupid/rating', authController.isAuthenticated, function (req, res) {
    //Check if user is already in a Group, then we don't send other groups to match
    console.log("Incomming Rating for group");
    Group.findOne({course: req.params.courseid, users: req.user._id}, function (err, group) {
        if (err) res.status(500).send(err);
        if (group) {
            console.log("Group for user found, skip sending groups");
            res.status(400).send("User is already in a Group, cannot rate other groups");
        } else {
            var match = false;
            if (req.body.like != undefined) {
                console.log(req.body.like);
                Rating.findOne({
                    _courseid: req.params.courseid,
                    _groupid: req.params.groupid,
                    'ratings._rateduserid': req.user._id
                }, {'ratings.$': 1}, function (err, ratings) {
                    if (err) {
                        console.log("Error finding ratedGroups Document:" + err);
                        res.status(500).send(err.message + ": Error finding ratedgroups document");
                        return;
                    } else if (ratings) {
                        console.log(ratings);
                        if (ratings.ratings[0].like && req.body.like) {
                            //Hey there we have a Match
                            console.log("WOhaaa we found a Match and update the other group...");
                            match = true;
                            Match.findOneAndUpdate({
                                    _courseid: req.params.courseid,
                                    _groupid: req.params.groupid
                                }, {$addToSet: {matches: req.user._id}},
                                {
                                    upsert: true, new: true
                                }).populate('_groupid _courseid').exec(function (err, matchobj) {
                                if (err)
                                    console.log("Bad, updating groupsmatchobject failed..." + err);
                                if (matchobj) {
                                    var group = matchobj._groupid;
                                    console.log("Update of otheruser successfull:" + matchobj._id);
                                    notifyusers(group.users, req);
                                }
                            });
                            Match.findOneAndUpdate({
                                    _courseid: req.params.courseid,
                                    _userid: req.user._id
                                }, {$addToSet: {matchedgroups: req.params.groupid}},
                                {
                                    upsert: true, new: true
                                }).exec(function (err, mymatch) {
                                if (err) console.log("Bad, updating usersmatchobject failed..." + err);
                            });
                        } else {
                            console.log("To bad, otheruser doesn't like the requestingUser...");
                            res.send("Rated suc")
                        }

                    }
                    Rating.findOneAndUpdate({
                        _courseid: req.params.courseid,
                        _userid: req.user._id
                    }, {
                        $addToSet: {
                            groupratings: {_ratedgroupid: req.params.groupid, like: req.body.like},
                            ratedgroups: req.params.groupid
                        }
                    }, {new: true, upsert: true}, function (err, rating) {
                        if (err)
                            console.log("Error updating requesting User:" + err);
                        else {
                            console.log("Saved Rating");
                            var response = {
                                type: "group",
                                _courseid: req.params.courseid,
                                _ratedgroupid: req.params.groupid,
                                like: req.body.like,
                                match: match
                            }
                            res.json(response);
                        }
                    });
                });
            } else {
                res.status(400).send("Please provide a like attribute...!");
            }
        }
    });


});


module.exports = router;
