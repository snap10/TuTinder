var mongoose = require('mongoose');
var User = require('./user');
var Course = require('./course');
var GroupRequest = require('./group_request');
var Schema = mongoose.Schema;

var GroupSchema = new Schema({
        users: [{type: Schema.Types.ObjectId, ref: 'User'}],
        course: {type: Schema.Types.ObjectId, ref: 'Course'},
        messages: [{
            text: String,
            user: {type: Schema.Types.ObjectId, ref: 'User'},
            created: {type: Date, default: Date.now}
        }],
        grouprequest: {type: Schema.Types.ObjectId, ref: 'GroupRequest'}
    },
    {timestamps: true});

GroupSchema.methods.addUser = function (user) {
    var is_success = true;
    if (!user._id) is_success = false;
    if (is_success) this.users.push(user._id);
    return is_success;
}

module.exports = mongoose.model('Group', GroupSchema);