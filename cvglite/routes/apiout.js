// A class of output serializers.


exports.BaseOutput = function(){
    this.body = {};

    this.head = {};

    this._mixObj = function( new_obj, obj ){
	var keys = Object.keys( new_obj );
	for( var i = 0 ; i < keys.length; i++ ){
	    obj[ keys[i] ] = new_obj[keys[i]];
	}
    }

    this.setHead = function( new_obj ){
	this._mixObj( new_obj , this.head );	
    }

    this.setBody = function( new_obj ){
	this._mixObj( new_obj , this.body );
    }

    this.outBody = function( res ){
	res.write( JSON.stringify(this.body) );
    }

    this.outHead = function( res ){
	var code = this.head["code"];
	delete this.head["code"];
	res.writeHead( code, this.head ); 
    }

}
