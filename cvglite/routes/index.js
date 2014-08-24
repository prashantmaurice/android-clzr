var express = require('express');
var cvgadaptor = require("./stack");


var apioutput = require("./apiout");

var router = express.Router();

/* Process Control Module*/
var adaptor = new cvgadaptor.StreamControl();

/* ------------*/


function _makeJobDataFromRequest( req ){
    return req.body;
}

/* */

router.get('/', function( req, res ){
    res.writeHead(200,{"Content-Type":"text/plain"});
    res.write("Hello!");
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
	output = new apioutput.BaseOutput();
	//debugger;
	output.setBody({"data":outputMsg});
	output.outBody( res );
	res.end();
	//debugger;
    });

    //debugger;
    adaptor.doJob( jobObject );
    //debugger;
});

module.exports = router;
