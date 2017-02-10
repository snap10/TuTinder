var express = require('express');
var logger = require('morgan');
var bodyParser = require('body-parser');
var mongoose = require('mongoose');
var users = require('./routes/users');
var courses = require('./routes/courses');
var faculties = require('./routes/faculties');
var matching = require('./routes/matching');
var variables = require('./routes/variables');
var user = require('./routes/user');
var passport = require('passport');


var app = express();

app.use(express.static(__dirname + '/public'));
//MongoDB
mongoose.connect('<TODO your own mongo db connection>');

// uncomment after placing your favicon in /public

app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));


// Use the passport package in our application
app.use(passport.initialize());


app.use('/users', users);
app.use('/user', user);
app.use('/courses', courses);
app.use('/matching', matching);
app.use('/faculties', faculties);
app.use('/variables', variables);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function (err, req, res) {
        console.log("Error occured:" + err.message);
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function (err, req, res) {
    console.log("Error occured:" + err.message);
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});


module.exports = app;
