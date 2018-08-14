var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var User = require('../models/user');
var authController = require('../controllers/auth');
var fs = require('fs');
var personalitytags = require('../personalitytags.json');
var Course = require('../models/course');




/* GET users listing. */
router.get('/', authController.isAuthenticated, function (req, res) {

    //return only users in requested ids list
    if (req.query.uid) {
        console.log(req.query.uid);
        var ids = req.query.uid.split(",");
        User.find({_id: {$in: ids}}).exec(function (err, users) {
            if (err) {
                console.log("Error while Reading Users from Database: " + err);
                res.status(500).send('Error while Reading Users from Database');
                return;
            }
            res.json(users);
        });
    } else {
        //return all users
        User.find(function (err, users) {
            if (err) {
                console.log("Error while Reading Users from Database: " + err);
                res.status(500).send('Error while Reading Users from Database');
                return;
            }
            res.json(users);
        });

    }
});

/**
 * @Deprecated
 * GET users/tags/<language>
 * Looks for personality tags in the given language.
 * Language is a short name like 'en'/'EN' or 'de'/'DE' and case-insensitive.
 * Personality tags are stored in a json file and loaded like a node module.
 * Note that course names can also be tags.
 * Response contains
 * a) personality tags (as list of tupels) under 'personality'
 * b) course names (as list) under 'interests'.
 */
router.get('/tags/:language', function (req, res) {
    var tags = {};
    tags.personality = personalitytags[req.params.language.toUpperCase()];
    if (!tags.personality) {
        res.status(404).send('Language not found.');
        return;
    }
    Course.find({}, 'name', function (err, courses) {
        if (err) res.status(500).send(err);
        else {
            tags.interests = [];
            for (var i in courses) tags.interests.push(courses[i].name);
            res.status(200).json(tags);
        }
    });
});

/**
 * Provide the Users Information
 */
router.get('/:id', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.params.id}, function (err, user) {
        if (err) {
            res.send(err);
            console.log(err);
            return;
        }
        res.json(user);
    });
});

/**
 * Provide the Users Information
 */
router.put('/:id/password', authController.isAuthenticated, function (req, res) {
    if (authController.isAdmin(req.user, function (err, isadmin) {
            if (err) res.status(401).send("Not an Admin");
            else {
                if (req.body.password) {
                    User.findOne({_id: req.params.id}, function (err, user) {
                        if (err) {
                            res.send(err);
                            console.log(err);
                            return;
                        } else if (user) {
                            user.hashedpassword = req.body.password;
                            user.save({new: true}, function (err, user) {
                                if (err) {
                                    console.log("Error while putting new password:" + err);
                                    res.send(err);
                                }
                                else res.json(user);
                            });
                        } else {
                            console.log("Error while putting new password:" + err);
                            res.status(404).send("User not found");
                        }
                    });
                }
            }
        }));
});


/**
 * Provide the CourseIDs and Name
 */
router.get('/:id/courses', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.params.id}).select('courses').exec(function (err, courselist) {
        if (err)
            res.send(err);
        res.json(courselist);
    });
});

/**
 * Provide the GroupIds and Coursenames Information
 */
router.get('/:id/groups', authController.isAuthenticated, function (req, res) {
    User.findOne({_id: req.params.id}).select('groups').exec(function (err, grouplist) {
        if (err)
            res.send(err);
        res.json(grouplist);
    });
});

module.exports = router;
