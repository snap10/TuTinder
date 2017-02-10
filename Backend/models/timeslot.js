/**
 * Created by Snap10 on 16.06.16.
 */
var mongoose = require('mongoose');
var Schema = mongoose.Schema;


var TimeSlotSchema = new Schema({
        day: Number,
        from: String,
        to: String
    },
    {
        timestamps: true
    });

module.exports = mongoose.model('TimeSlots', TimeSlotSchema);

