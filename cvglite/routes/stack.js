

var engine = require("engine.io");
var net = require("net");
var settings = require("./settings");
var fs = require('fs');

var request = require('request');

var DualQueue = function(){

    this.current = [];
    this.queued = [];

    this.objects = {};
    

    this.push = function( object ){
	var hash = object.getHash();
	this.queued.push( hash );
	this.objects[hash] = object;
    }

    this.removeFront = function(){
	return this.objects[queued.pop()];
    }

    this.shift = function(){
	var popped = this.queued.shift();
	if(popped == undefined)
	    return null;
	this.current.push(popped);
	return this.objects[popped];
    }

    this.pop = function(){
	var popped = this.current.shift();
	return this.objects[popped];
    }
    
    this.currSize = function(){
	return this.current.length;
    }

    this.queueSize = function(){
	return this.queued.length;
    }
}

function download(uri, filename, callback){
    request.head(uri, function(err, res, body){
	console.log('content-type:', res.headers['content-type']);
	console.log('content-length:', res.headers['content-length']);
	var stream = fs.createWriteStream(filename);	
	request(uri).pipe(stream).on('close', function(){
	    stream.end();
	    callback();
	});
    });
};
exports.ClassifierInputObject = function( data, callback ){

    this.data = data;
    this.callback = callback;
    
    this.hash = new Date().getTime();

    this.prepare = function(callback){
	var http = require('http');
	var fs = require('fs');


	download( this.data.url ,  __dirname + "/temp/" + this.getHash() + '.png' , function(){
	    console.log('done');
	    callback();
	});

	/*var file = fs.createWriteStream( __dirname + "/temp/" + this.getHash() );
	var request = http.get(this.data.url, function(response) {
	    response.pipe(file);
	});*/
    }

    this.getInputString = function(){
	return __dirname + "/temp/" + this.getHash() +'.png'
    }

    this.respond = function( data ){
	callback(data);
    }
    
    this.clean = function(){
	//fs.unlink( __dirname + "/temp/" + this.getHash() );
    }
    
    this.getHash = function(){
	return this.hash;
    }
}

exports.StreamControl = function( conn_details ){
    this.conn_details = conn_details;
    this.queue = new DualQueue();

    this.doJob = function( job ){
	var self = this;
	job.prepare(function(){
	    self.queue.push( job );
	    self.batchCall();
	});
    }

    this.batchCall = function(){
	console.log("In batch call.")
	// check if a process is already underway.
	if(this.queue.currSize())
	    return;

	var raw_input = "";
	
		
	for( var i = 0 ; i < settings.MAX_BATCH_LENGTH ; i++ ){
	    var obj = this.queue.shift();
	    console.log("Shifted: Curr size: "+this.queue.currSize());
	    if( obj == null )
		break;
	    
	    raw_input += obj.getInputString();
	    if( this.queue.queueSize() == 0 )
		raw_input+=" "
	}
	
	if( !i )
	    return;
	
	var socket = new net.Socket();

	var self = this;
	socket.connect( settings.CLASSIFIER_SERVICE.port, settings.CLASSIFIER_SERVICE.host, function(){
	    console.log( "Writing: "+raw_input );
	    socket.write(raw_input);
	    console.log("opened");
	    socket.on("data", function( msg ){
		console.log('Data:'+msg);
	        self.onFinish( msg.toString() );
		socket.destroy();
	    });
	    socket.on("close", function(){
		console.log("closed");
	    });
	});

    }
    this.onFinish = function( out ){
	var len = this.queue.currSize();
	console.log("Num in curr: "+ len);
	for( var i=0; i<len; i++){
	    console.log("Responding");
	    var obj = this.queue.pop();
	    obj.clean();
	    obj.respond( out );
	    console.log("Responded");
	}
	this.batchCall();
    }
}

