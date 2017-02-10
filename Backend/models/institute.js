/**
 * Created by Snap10 on 03.05.16.
 */
/**
 * Created by Snap10 on 20.04.16.
 */

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html

var InstituteSchema = new Schema({
    name: {type: String},
    courses: [{
        _id: Schema.Types.ObjectId,
        name: String
    }]},{
    timestamps: true
});
module.exports = mongoose.model('Institute', InstituteSchema);