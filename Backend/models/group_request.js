/**
 * Created by Snap10 on 03.05.16.
 */

var mongoose = require('mongoose');
var User = require('./user');
var Course = require('./course');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html

var GroupRequestSchema = new Schema({
        _courseid: {type: Schema.Types.ObjectId, ref: 'Course'},
        members: [{
            _userid: {type: Schema.Types.ObjectId, ref: 'User'},
            _requestorid: {type: Schema.Types.ObjectId, ref: 'User'},
            accept: Boolean
        }]
    },
    {
        timestamps: true
    });

module.exports = mongoose.model('GroupRequest', GroupRequestSchema);
/**
 * Created by Snap10 on 23.05.16.
 */
