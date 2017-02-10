/**
 * Created by Snap10 on 07.06.16.
 */


var mongoose = require('mongoose');
var Schema = mongoose.Schema;


var PersonalityTagsSchema = new Schema({
        tags: [{
            tag: [
                {
                    lang: String,
                    value: String
                }
            ]
        }]
    },
    {
        timestamps: true
    });

module.exports = mongoose.model('PersonalityTags', PersonalityTagsSchema);

