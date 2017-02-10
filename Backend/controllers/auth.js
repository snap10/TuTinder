/**
 * Created by Snap10 on 25.04.16.
 */

// Load required packages
var passport = require('passport');
var BasicStrategy = require('passport-http').BasicStrategy;
var User = require('../models/user');

//Username is fixed and should be a matrikelnr
passport.use(new BasicStrategy(
    function (username, password, callback) {
        console.log('Start authentication');
        if (isNaN(username))
            return callback('Please provide a valid Number for matrikulation number');
        User.findOne({matrikelnr: username}, function (err, user) {
            if (err) {
                console.log(err);
                return callback(err);
            }

            // No user found with that username
            if (!user) {
                console.log('No User with that Username!');
                return callback(null, false);
            }

            // Make sure the password is correct
            user.verifyPassword(password, function (err, isMatch) {
                if (err) {
                    console.log('Password verification error');
                    return callback(err);
                }

                // Password did not match
                if (!isMatch) {
                    console.log('Passwords did not match');
                    return callback(null, false);
                }

                // Success
                return callback(null, user);
            });
        });
    }
));

var isAdmin = function (user, cb) {
    if (user.securitygroup == 1) return cb(null, true);
    else {
        var err = new Error('Not Admin');
        err.status=401;
        return cb(err, false);
    }
}

exports.isAdmin = isAdmin;
exports.isAuthenticated = passport.authenticate('basic', {session: false});
