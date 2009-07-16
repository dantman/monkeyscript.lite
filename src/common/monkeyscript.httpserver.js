
print("Starting up...");

var port = 80;
var host = 'localhost';

var ss = new java.net.ServerSocket(port);
while(true) {
	var client = ss.accept();
	try {
		var inputStream = client.getInputStream();
		var reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
		
		try {
			var httpLine = reader.readLine();
			if( !httpLine )
				throw "Invalid HTTP request";
			var [ httpMethod, httpLocation, httpVersion ] = String(httpLine).match(/(.+)\s+(/.*)\s+HTTP/(\d+\.\d+)/);
			httpMethod = httpMethod.toUpperCase();
			httpVersion = parseFloat(httpVersion);
			if ( !/^\w+$/.test(httpMethod) )
				throw "Invalid HTTP request";
			if ( httpVersion !== 1.0 && httpVersion !== 1.1 )
				throw {
					name: "HTTPError",
					status: 505,
					message: "HTTP Version " + httpVersion + " not supported";
				};
			
			var httpHeaders = {};
			while ((line = reader.readLine()) != null) {
				line = String(line); // Cast Java String to JavaScript String
				if ( line == "" )
					break; // We've hit the \n\n that signals header end
				var [ headerName, /*:*/, headerValue ] = line.partition(':');
				if ( !/^[a-z][-_a-z0-9]*/i.test(headerName) || /[-_]$/.test(headerName) )
					continue;
				headerValue = headerValue.trimLeft();
				headerName = headerName.toUpperCase();
				if ( headerName in httpHeaders )
					httpHeaders[headerName] += "\n" + headerValue;
				else
					httpHeaders[headerName] = headerValue;
			}
			
			// ToDo POST Body
			
			var [ httpPath, /*?*/, httpQuery ] = httpLocation.partition('?');
			
			var jsgi = {};
			jsgi["REQUEST_METHOD"]    = httpMethod;
			jsgi["SCRIPT_NAME"]       = "";
			jsgi["PATH_INFO"]         = httpPath || "";
			jsgi["QUERY_STRING"]      = httpQuery || "";
			jsgi["SERVER_NAME"]       = host;
			jsgi["SERVER_PORT"]       = port.toString(10);
			if( 'Host' in httpHeaders )
				jsgi["HTTP_HOST"] = httpHeaders['Host'];
			for ( var headerName in httpHeaders )
				jsgi["HTTP_" + httpHeader.toUpperCase()] = httpHeaders[headerName];
			delete jsgi["HTTP_CONTENT_TYPE"];
			delete jsgi["HTTP_CONTENT_LENGTH"];
			jsgi["jsgi.version"]      = [0, 1];
			jsgi["jsgi.url_scheme"]   = 'http'; // Right now we don't do https
			jsgi["jsgi.input"]        = 
			jsgi["jsgi.errors"]       = 
			jsgi["jsgi.multithread"]  = false;
			jsgi["jsgi.multiprocess"] = false;
			jsgi["jsgi.run_once"]     = false;
		
			// Response handling code here
			var [ resStatus, resHeaders, resBody ] = ...;
			resStatus = parseInt(resStatus, 10);
			
			if ( resStatus < 100 )
				throw {
					name: "HTTPError",
					status: 500,
					message: "Web app returned a status code below 100 (" + resStatus + ")";
				};
			
			if ( ( resStatus >= 100 && resStatus < 200 ) || resStatus == 204 || resStatus == 304 ) {
				// We're ignoring bad things here. By the JSGI spec are we supposed to throw instead?
				delete resHeaders['Content-Length'];
				delete resHeaders['Content-Type'];
			} else {
				if ( !resHeaders['Content-Type'] )
					throw {
						name: "HTTPError",
						status: 500,
						message: "App returned a " + resStatus + " status code but did not include a Content-Type.";
					};
			}
			
			if ( !resBody.forEach )
				throw {
					name: "HTTPError",
					status: 500,
					message: "Body returned by web app does not support .forEach";
				};
			
            var outputStream = client.getOutputStream();
			var writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(inputStream));
			
			"HTTP/v.v ### ..."
			
			resBody.forEach(function(piece) {
				if ( !piece.toByteString )
					throw {
						name: "HTTPError",
						status: 500,
						message: "Body piece returned by web app does not support .toByteString";
					};
				
				
				
			});
			if( resBody.close )
				resBody.close();
			
			
			outputStream.close();
			
		} catch ( e if e.name === "HTTPError" ) {
			
		}		
	} catch ( e ) {
		print( e );
	}
}

