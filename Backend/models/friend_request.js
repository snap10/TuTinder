/**
 * Created by Snap10 on 03.05.16.
 */


var mongoose = require('mongoose');
var Schema = mongoose.Schema;

//TODO possibliy use refs to enable population. See: http://mongoosejs.com/docs/populate.html

var FriendRequestSchema = new Schema({
        _userid: Schema.Types.ObjectId,
        nonce: String
    },
    {
        timestamps: true
    });

module.exports = mongoose.model('FriendRequest', FriendRequestSchema);
/**
 * Created by Snap10 on 23.05.16.
 */
