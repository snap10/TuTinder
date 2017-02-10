var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var Course = require('../models/course');
var User = require('../models/user');
var Faculty = require('../models/faculty');
var Institute = require('../models/institute');
var seneca = require('seneca');
var crypto = require('crypto');
var authController = require('../controllers/auth');


/* GET users listing. */
router.get('/', authController.isAuthenticated, function (req, res) {
    Faculty.find(function (err, faculties) {
        if (err)
            res.send(err);
        res.json(faculties);
    });
});

/* POST should create a New Faculty and respond with the new created ID and Information*/
router.post('/', authController.isAuthenticated, function (req, res) {
    if (req.body.name) {

        var faculty = new Faculty();
        faculty.name = req.body.name;

        // save the course and check for errors
        faculty.save(function (err, faculty) {
            if (err)
                res.send(err);

            res.json(faculty)
        });
    } else {
        res.status(400).send('Please provide name');
    }
});
/* GET institutes listing. */
router.get('/institutes', authController.isAuthenticated, function (req, res) {
    Faculty.find({}, 'institutes', function (err, institutes) {
        if (err)
            res.send(err);
        res.json(institutes);
    });
});

/* GET institutes listing. */
router.get('/:facultyid/institutes', authController.isAuthenticated, function (req, res) {
    if (req.body.name) {

        var institute = new Institute();
        faculty.name = req.body.name;

        // save the course and check for errors
        faculty.findByIdAndUpdate(req.params.facultyid, {$addToSet: {institutes: institute}}, {
            safe: true,
            new: true
        }, function (err, faculty) {
            if (err)
                res.send(err);

            res.json(faculty)
        });
    } else {
        res.status(400).send('Please provide name');
    }
});
/* create institue within facultyid . */
router.post('/:facultyid/institutes', authController.isAuthenticated, function (req, res) {
    if (req.body.name) {

        var institute = new Institute();
        institute.name = req.body.name;

        // save the course and check for errors
        Faculty.findByIdAndUpdate(req.params.facultyid, {$addToSet: {institutes: institute}}, {
            safe: true,
            new: true
        }, function (err, faculty) {
            if (err)
                res.send(err);

            res.json(faculty)
        });
    } else {
        res.status(400).send('Please provide name');
    }
});
/* create institue within facultyid . */
router.delete('/:facultyid/institutes/:instituteid', authController.isAuthenticated, function (req, res) {

    // save the course and check for errors
    Faculty.findByIdAndUpdate(req.params.facultyid, {$pull: {institutes: {_id: req.params.instituteid}}}, {
        safe: true,
        upsert: true,
        new: true
    }, function (err, faculty) {
        if (err)
            res.send(err);

        res.json(faculty)
    });

});


/* GET users listing. */
router.get('/:facultyid', authController.isAuthenticated, function (req, res) {
    Faculty.findById(req.params.facultyid, function (err, faculty) {
        if (err)
            res.send(err);
        res.json(faculty);
    });
});

module.exports = router;
/**
 * Created by Snap10 on 03.05.16.
 */
