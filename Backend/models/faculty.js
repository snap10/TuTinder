/**
 * Created by Snap10 on 03.05.16.
 */
/**
 * Created by Snap10 on 20.04.16.
 */

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html

var FacultySchema = new Schema({
    name:String,
    institutes:[{
        name:String,
        courses:[{
            _courseid: Schema.Types.ObjectId,
            name: String,
            picturepath:String
        }]
    }]},{
    timestamps: true
});

module.exports = mongoose.model('Faculty',FacultySchema);