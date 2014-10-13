var express = require('express');
var cvgadaptor = require("./stack");


var apioutput = require("./apiout");

var router = express.Router();


var db = require('./logger');
/* Process Control Module*/
var adaptor = new cvgadaptor.StreamControl();

/* ------------*/


function _makeJobDataFromRequest( req ){
    return req.body;
}

/* */


router.use('/', function( req, res, next )){
    res.set('Access-Control-Allow-Origin:','*');
    next();
});
router.get('/', function( req, res ){
    res.writeHead(200,{"Content-Type":"application/json"});
    res.write( JSON.stringify({ "version":1.0 }) );
    res.end();
});

router.post('/classify', function(req, res) {
    

    var immediate = new apioutput.BaseOutput();
    
    //try{
	var jobDetails = _makeJobDataFromRequest( req );
    //}catch(err){
//	immediate.setBody( immediate, err );
  //  }
    
    immediate.setHead( {"code":200, "Content-Type":"application/json"} );

    //immediate.outHead( res );
    var jobObject = new cvgadaptor.ClassifierInputObject( jobDetails, function( outputMsg ){
	output = new apioutput.ClassifierDataOutput();
	debugger;
	//output.setBody({"data":outputMsg});
	output.setData( outputMsg );
	output.outBody( res );
	res.end();

	db.putJob( jobObject , function(){ console.log('logged.') });
	//debugger;
    }, function(err){
	output = new apioutput.ErrorOutput();
	output.setMessage( "Error loading link." );
	output.outBody( res );
	res.end();
    });

    //debugger;
    adaptor.doJob( jobObject );
    //debugger;
});


router.get('/', function( err, req, res, next ){
    output = new apioutput.ErrorOutput();
    output.setMessage( err.message );
    output.outBody( res );
    res.end();
});

module.exports = router;
