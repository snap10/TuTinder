/**
 * Created by Snap10 on 09.05.16.
 */

var Institute = require('./institute');
var Faculty = require('./faculty');
var Group = require('./group');
var User = require('./user');
var Course = require('./course');

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html


var RatingSchema = new Schema({
    _id: Schema.Types.ObjectId,
    _userid: Schema.Types.ObjectId,
    _groupid:Schema.Types.ObjectId,
    _courseid: {type: Schema.Types.ObjectId, ref: 'Course'},
    ratings: [{
        _id: false,
        _rateduserid: {type: Schema.Types.ObjectId, ref: 'User'},
        like: Boolean
    }],
    rated: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],
    ratedgroups:[{
        type: Schema.Types.ObjectId,
        ref: 'Group'
    }],
    groupratings:[{
        _id: false,
        _ratedgroupid: {type: Schema.Types.ObjectId, ref: 'Group'},
        like: Boolean
    }]


}, {timestamps: true});


module.exports = mongoose.model('Rating', RatingSchema);