var express = require('express');
app = express();


var http = require('http');
http.createServer(function (req, res) {
	res.writeHead(200, {'Content-Type': 'text/plain'});
	res.writeHead(200, {'Access-Control-Allow-Origin': '*'});
	res.end();
}).listen(8080, "127.0.0.1");
console.log('Server running at http://127.0.0.1:8080');

app.get('/write/:url', function(req, res) {
	console.log(req.params.url);
	res.send("hi");
})

write = function(res,req){
	console.log(req.params.msg);
	var path = "/home/steve/workspace/hackathons/hackumbc17/ingr3/web/new.txt";
	var data = "potatoes.com"

	fs.writeFile(path, data, function(error) {
	if (error) {
		console.error("write error: " + error.message);
	} else {
		console.log("successful write to " + path);
	}
});	
};
