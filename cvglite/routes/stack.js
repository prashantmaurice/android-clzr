

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

function download(uri, filename, callback, error){
    request.head(uri, function(err, res, body){
	//console.log('content-type:', res.headers['content-type']);
	//console.log('content-length:', res.headers['content-length']);
	var stream = fs.createWriteStream(filename);
	stream.on('error', function(){
	    console.log("Error loading img.");
	    stream.end();
	    error();
	});
	request(uri).on('error', function(){
	    console.log("Error loading img.");
	    stream.end();
	    error();
	}).pipe(stream).on('error', function(){
	    console.log("Error loading img.");
	    stream.end();
	    error();
	}).on('close', function(){
	    stream.end();
	    callback();
	});
    });
};

_nullTerminate = function( str ){
    var out = "";
    for( var i = 0 ;i < str.length; i++ ){
	if( str[i] == '\0' )
	    break;
	out += (str[i]);
    }
    return out;
}

_toJSON = function( str ){
    var obj = {};

    str.split("|").forEach(function( value, index, array ){
        obj[value.split(":")[0].trim()] = parseFloat(value.split(":")[1].trim());
    });
    return obj;
}

exports.EventTimer = function(){
    this._timedeltas = {};
    this._current = {};
    this.getLabel = function( label ){
	return parseFloat(this._timedeltas[label][0]) + parseFloat(this._timedeltas[label][1])/1e9;
    }

    this.start = function( label ){
	var time = process.hrtime();
	this._current[label] = time;
    }

    this.end = function( label ){
        var time = process.hrtime();
	this._timedeltas[label] = [time[0] - this._current[label][0], time[1] - this._current[label][1]];
    }
}
exports.ClassifierInputObject = function( data, callback, error ){
    exports.EventTimer.call( this );

    // input state storage
    this.data = data;
    
    // output state storage
    this.output = '';

    // error state storage
    this.errState = false;

    // err and success callbacks
    this.callback = callback;
    this.error = error;

    // fast access precomputed variable determining the signature of this object uniquely.
    this.hash = new Date().getTime() + "_" + ( Math.random()*1000 );
    this.startTime = new Date().toISOString();

    this.prepare = function(callback){
	var http = require('http');
	var fs = require('fs');

	var self = this;
	this.start('download');
	download( this.data.url ,  __dirname + "/temp/" + this.getHash() , function(){
	    self.end('download');
	    if(!self.errState)
		callback();
	    //console.log('done loading image');
	}, function(){
	    console.log('Error loading image');
	    self.end('download');
	    self.errState = true;
	    self.error();
	});
	/*var file = fs.createWriteStream( __dirname + "/temp/" + this.getHash() );
	var request = http.get(this.data.url, function(response) {
	    response.pipe(file);
	});*/
    }

    this.getInputString = function(){
	return __dirname + "/temp/" + this.getHash()
    }
    
    this.begin = function(){
	this.start('process');
    }

    this.respond = function( data ){
	this.end('process');
	this.output = data;
	callback(data);
    }
    
    this.finish = function(){
	fs.unlink( __dirname + "/temp/" + this.getHash() );
    }
    
    this.getHash = function(){
	return this.hash;
    }

    this.getDetails = function(){
	debugger;
	return { 'output': JSON.stringify( _toJSON( _nullTerminate(this.output) ) ), 'err': this.errState + '', 'url': this.data.url, 'timestamp': this.startTime, 'download_delta': this.getLabel('download'),'process_delta': this.getLabel('process') };
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
	    
	    obj.begin();
	    raw_input += obj.getInputString();
	    if( this.queue.queueSize() != 0 )
		raw_input+=" "
	}
	
	if( !i )
	    return;
	
	var socket = new net.Socket();

	var self = this;
	socket.connect( settings.CLASSIFIER_SERVICE.port, settings.CLASSIFIER_SERVICE.host, function(){
	    console.log( "Writing: "+raw_input );
	    socket.write(raw_input);
	    //console.log("opened");
	    socket.on("data", function( msg ){
		console.log('Data:'+msg);
	        self.onFinish( msg.toString() );
		socket.destroy();
	    });
	    socket.on("close", function(){
		//console.log("closed");
	    });
	});

    }
    this.onFinish = function( out ){
	var len = this.queue.currSize();
	console.log( "Num in curr: "+ len );
	console.log( "Num in queue: "+ this.queue.queueSize() );

	for( var i=0; i<len; i++){
	    console.log("Responding");
	    var obj = this.queue.pop();
	    obj.finish();
	    obj.respond( out );
	    //console.log("Responded");
	}
	this.batchCall();
    }
}

