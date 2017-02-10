/**
 * Created by Snap10 on 20.04.16.
 */

var Institute = require('./institute');
var Faculty = require('./faculty');
var Group = require('./group');
var TimeSlots = require('./timeslot');
var User = require('./user');

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html


var CourseSchema = new Schema({
    name: String,
    term: String,
    lecturer: String,
    description:String,
    faculty: {
        _id: Schema.Types.ObjectId,
        name: String,
        institute: {_id: Schema.Types.ObjectId, name: String},
    },
    //select:false prevents to retrieve the password on User.find
    //Must selected explicitly in the query see http://stackoverflow.com/a/12096922 for more information.
    hashedpassword: {type: String, select: false},
    created: {type: Date, default: Date.now},
    cloudinarypicturepath: String,
    picturepath: String,
    thumbnailpath: String,
    timeslots:[{type:Schema.Types.ObjectId,ref:'TimeSlots'}],
    enrolledusers: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],

    groups:[{type:Schema.Types.ObjectId,ref:'Group'}],
    maxgroupsize: Number
}, {
    timestamps: true

});


module.exports = mongoose.model('Course', CourseSchema);