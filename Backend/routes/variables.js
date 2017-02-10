/**
 * Created by Snap10 on 07.06.16.
 */
var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var PersonalityTags = require('../models/personality_tags');
var TimeSlots = require('../models/timeslot');
var authController = require('../controllers/auth');


/* GET all available Timeslots.*/

router.get('/timeslots', function (req, res) {
    if (req.query.tsid) {
        console.log(req.query.tsid);
        var ids = req.query.tsid.split(",");
        TimeSlots.find({_id: {$in: ids}}).exec(function (err, timeslots) {
            if (err) {
                console.log("Error while Reading Slots from Database: " + err);
                res.status(500).send('Error while Reading Tags from Database');
                return;
            }
            res.json(timeslots);
        });
    } else {
        TimeSlots.find().exec(function (err, timeslots) {
            if (err) {
                console.log("Error while Reading Slots from Database: " + err);
                res.status(500).send('Error while Reading Tags from Database');
                return;
            }
            res.json(timeslots);
        });

    }

});

/* Create TimeSlot. */
router.post('/timeslots', authController.isAuthenticated, function (req, res, next) {
    if (authController.isAdmin(req.user, function (err, isadmin) {
            if (err) {
                res.status(401).send(err.msg);
            } else {
                console.log(req.body.day);
                console.log(req.body.from);
                console.log(req.body.to);

                var timeslot = req.body;
                if (timeslot.day && timeslot.from && timeslot.to) {
                    TimeSlots.findOneAndUpdate({day: timeslot.day, from: timeslot.from, to: timeslot.to}, {}, {
                        upsert: true,
                        new: true
                    }, function (err, slot) {
                        if (err) {
                            console.log(err);
                            console.log(err.message);
                            next(err);
                            return;
                        } else if (!slot) {
                            console.log("Slot could not be found or created");
                            res.status(500).send("Slot could not be created");
                            return;
                        } else {
                            console.log("Timeslot created:" + slot);
                            res.json(slot);
                        }
                    });
                }

            }
        }));
});


/* GET tags listing.
 * ?lang=DE or EN optional provides only languagespecific */
router.get('/tags/personality', function (req, res) {
    //TODO find some complex aggregate function to extract specific language
    /* if (req.query.lang) {
     var lang = req.query.lang.toUpperCase();
     PersonalityTags.aggregate({[]})
     PersonalityTags.find({},'tags.$',function (err, tags) {
     if (!tags) {
     console.log("Language for Tags was not found: " + lang);
     res.status(404).send('Language not found.');
     return;
     }
     res.json(tags);
     });
     } else {*/
    PersonalityTags.find().exec(function (err, tags) {
        if (err) {
            console.log("Error while Reading Tags from Database: " + err);
            res.status(500).send('Error while Reading Tags from Database');
            return;
        }
        res.json(tags);
    });

});

/* GET tags listing.
 * ?lang=DE or EN optional provides only languagespecific */
router.get('/tags/personality/:tagId', function (req, res) {
    //TODO find some complex aggregate function to extract specific language
    /* if (req.query.lang) {
     var lang = req.query.lang.toUpperCase();
     PersonalityTags.aggregate({[]})
     PersonalityTags.find({},'tags.$',function (err, tags) {
     if (!tags) {
     console.log("Language for Tags was not found: " + lang);
     res.status(404).send('Language not found.');
     return;
     }
     res.json(tags);
     });
     } else {*/
    PersonalityTags.find({'tags._id': req.params.tagId}, {'tags.$': 1}).exec(function (err, tags) {
        if (err) {
            console.log("Error while Reading Tags from Database: " + err);
            res.status(500).send('Error while Reading Tags from Database');
            return;
        }

        res.json(tags);

    });

})
;

/* Create Tag. */
router.post('/tags/personality', authController.isAuthenticated, function (req, res) {
    console.log(req.body.tag);
    if (req.body.tag) {
        var tag = req.body.tag;
        if (tag.tags && tag.tags.length == 2) {
            perstag = new PersonalityTags(tag);
            perstag.save({upsert: true, new: true}, function (err, tag) {
                if (!tag) {
                    console.log("Error while saving Tag in DB: " + req.body.tag);
                    res.status(500).send('Error while saving Tag in DB');
                    return;
                }
                res.json(tag);
            });

        } else {
            res.status(400).send("Please provide valid Tag information in DE and EN");
        }
    } else {
        res.status(400).send("Please provide valid Tag information in DE and EN");
    }

});

module.exports = router;
/**
 * Created by Snap10 on 03.05.16.
 */
