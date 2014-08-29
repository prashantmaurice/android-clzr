
var aws = require('aws-sdk');
var settings = require('./settings');
aws.config.update({'region':settings.DYNAMODB.region});
var db = new aws.DynamoDB();

exports.putJob = function( job, callback){

    // Build params function.
    var id = job.getHash();
    var details = job.getDetails();

    var params = {
	Item: { /* required */
	    /* anotherKey: ... */
	},
	TableName: settings.DYNAMODB.tables.job, /* required */
    };

    params.Item['id'] = { 'S': id };

    var keys = Object.keys(details);
    for( var i = 0 ;i < keys.length ;i++ ){
	params.Item[keys[i]] = {'S' : details[keys[i]]+""};
    }

    db.putItem(params,function(err, data){
	debugger;
	callback();
    });
}
