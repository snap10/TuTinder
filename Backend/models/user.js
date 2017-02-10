/**
 * Created by Snap10 on 20.04.16.
 */

var mongoose = require('mongoose');
var bcrypt = require('bcrypt-nodejs');

var Schema = mongoose.Schema;
//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html

var UserSchema = new Schema({
        matrikelnr: Number,
        //select:false prevents to retrieve the password on User.find
        //Must selected explicitly in the query see http://stackoverflow.com/a/12096922 for more information.
        hashedpassword: String,
        securitygroup: Number,
        gcmtoken: String,
        name: String,
        phone: Number,
        studycourse: String,
        email: String,
        description: String,
        cloudinarypicturepaths: [String],
        personalitytags: [
            {
                type: Schema.Types.ObjectId
            }
        ],
        interesttags: [String],
        profilepicturepath: String,
        profilethumbnailpath: String,
        //Coursename is replicated
        groups: [{
            _id: Schema.Types.ObjectId,
            coursename: String,
            term: String
        }],
        courses: [{
            _id: Schema.Types.ObjectId,
            name: String,
            term: String,
            faculty: {
                _id: Schema.Types.ObjectId,
                name: String,
                institute: {_id: Schema.Types.ObjectId, name: String},
            },
            cloudinarypicturepath: String,
            chosentimeslots: [{type: Schema.Types.ObjectId, ref: 'TimeSlots'}]
        }],
        friends: [{
            type: Schema.Types.ObjectId,
            ref: 'User'
        }]
    },
    {
        timestamps: true
    });

// Execute before each user.save() call
UserSchema.pre('save', function (callback) {
    var user = this;
    console.log("PreSavingoperation is done");
    // Break out if the password hasn't changed
    if (!user.isModified('hashedpassword' || this.isNew)) return callback();

    // Password changed so we need to hash it
    console.log("Password changed, creating new Hash...");
    bcrypt.genSalt(5, function (err, salt) {
        if (err) return callback(err);

        bcrypt.hash(user.hashedpassword, salt, null, function (err, hash) {
            if (err) return callback(err);
            user.hashedpassword = hash;
            callback();
        });
    });
});


UserSchema.methods.verifyPassword = function (password, callback) {
    bcrypt.compare(password, this.hashedpassword, function (err, isMatch) {
        console.log("log: " + this.hashedpassword);
        if (err) return callback(err);
        callback(null, isMatch);
    });
};


module.exports = mongoose.model('User', UserSchema);
