var nauthwssocket;
var nauthwssocketerror;
var nauthwsfallbackbusy;
var nauthwssocketdisabled;

function nauthwsinit(serverid, registerid, knownstatus, host, fallbackhost) {
	nauthwssocketerror = true;
	nauthwsfallbackbusy = false;
	nauthwssocketdisabled = false;

	/* test websocket support */
	if ('WebSocket' in window || 'MozWebSocket' in window) {
		window.setInterval(nauthwscheck, 500, serverid, registerid,
				knownstatus, host);
	} else {
		// fallback
		window.setInterval(nauthwsfallbackcheck, 500, serverid, registerid,
				knownstatus, fallbackhost);
	}
}

function nauthwsfallbackcheck(serverid, registerid, knownstatus, host) {
	if (nauthwsfallbackbusy)
		return;

	nauthwsfallbackbusy = true;
	var xhr;
	xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function() {
		if (xhr.readyState == 4 && xhr.status == 200) {
			var newstatus = xhr.responseText;
			if (newstatus == '0' || newstatus == '1') {
				if (knownstatus != parseInt(newstatus)) {
					nauthwsdoupdate();
				}
			}
			nauthwsfallbackbusy = false;
		} else if (xhr.readyState == 4) {
			nauthwsfallbackbusy = false;
		}
	}
	var fullurl = host + "&serverid=" + encodeURIComponent(serverid)
			+ "&nonce=" + encodeURIComponent(registerid);
	xhr.open("GET", fullurl, true);
	xhr.timeout = 10000;

	xhr.send();
}

function nauthwscheck(serverid, registerid, knownstatus, host) {
	if (!nauthwssocketerror)
		return;
	if(nauthwssocketdisabled)
		return;

	nauthwssocketerror = false;
	try {
		if ('WebSocket' in window) {
			nauthwssocket = new WebSocket(host);
		} else if ('MozWebSocket' in window) {
			nauthwssocket = new MozWebSocket(host);
		}
		log('WebSocket - status ' + nauthwssocket.readyState);
		nauthwssocket.onopen = function(msg) {
			log("Connected");
			nauthwssend("REGISTER " + registerid + " " + serverid);
		};
		nauthwssocket.onmessage = function(msg) {
			log("Received: " + msg.data);
			nauthwsreceive(msg.data, knownstatus);
		};
		nauthwssocket.onclose = function(msg) {
			log("Disconnected");
			nauthwssocketerror = true;
		};
	} catch (ex) {
		log(ex);
		nauthwssocketerror = true;
	}
}

function nauthwssend(msg) {
	try {
		nauthwssocket.send(msg);
		log('Sent: ' + msg);
	} catch (ex) {
		log(ex);
	}
}

function nauthwsreceive(msg, knownstatus) {
	if (knownstatus == 1 && msg.indexOf("LOGOUT") == 0) {
		// currently logged in, received logout
		nauthwssocketdisabled = true; // kill the socket
		if(typeof nauthwsdoupdate == 'function')
			nauthwsdoupdate(msg);
		else
			location.assign(".");
	} else if (knownstatus != 1 && msg.indexOf("LOGIN") == 0) {
		// currently logged out, received login
		nauthwssocketdisabled = true; // kill the socket
		if(typeof nauthwsdoupdate == 'function')
			nauthwsdoupdate(msg);
		else
			location.assign(".");
	} else if (msg.indexOf("TRANSACTION") == 0){
		nauthwssocketdisabled = true; // kill the socket
		if(typeof nauthwsdoupdate_transaction == 'function')
			nauthwsdoupdate_transaction(msg.split(" ")[1]);
		else
			location.assign(".");
		
	} else if (msg.indexOf("CONFIRMPROVOKE") == 0){
		if(typeof nauthwsprovokeconfirm == 'function'){
			nauthwsprovokeconfirm();
		}
	}
}


function log(msg) { /*document.getElementById("log").innerHTML+="<br>"+msg; */
}
