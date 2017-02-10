/**
 * Created by Snap10 on 11.06.16.
 */

var User = require('./user');
var Course = require('./course');

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html


var MatchSchema = new Schema({
    _id: Schema.Types.ObjectId,
    _userid: {type:Schema.Types.ObjectId, ref:'User'},
    _groupid:{type:Schema.Types.ObjectId,ref:'Group'},
    _courseid: {type: Schema.Types.ObjectId, ref: 'Course'},
    matches: [{type: Schema.Types.ObjectId, ref: 'User'}],
    matchedgroups: [{type: Schema.Types.ObjectId, ref: 'Group'}],


}, {timestamps: true});


module.exports = mongoose.model('Match', MatchSchema);