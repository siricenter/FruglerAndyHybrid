
	var clicks = 0

	function sendCount(){
		/*toString MUST be called on the callbackFunc function object or the
		 *JSON library will strip the function out of the message.
		 *This means that named or anonymous functions can be used but anonymous functions
		 *can not be treated as closures. The do will retain scope information for later execution.
		 *The anonymous function will not 'capture' values from the scope of the containing function.
		 */
		var message = {"cmd":"increment","count":clicks,"callbackFunc":function(responseAsJSON){

			var response = JSON.parse(responseAsJSON)
			clicks = response['count']
			document.querySelector("#messages_from_java").innerText = "Count is "+clicks
			}.toString()
		}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)

	}

	window.onload = function() {
		// get the sub information from Google Play
		var message = {"cmd":"onload"}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)
	}

	function confirmPurchase() {
		var username = document.querySelector("#username").value
		var email = document.querySelector("#email").value
		var password = document.querySelector("#password").value
		var confirmPwd = document.querySelector("#confirmPwd").value
		// TODO: error handling for incorrect user input
		//do a local pw confirm here
		//if fails, don't continue

		//{"name":username, "mail":email, "pass":password}

		var message = {"cmd":"requestMonthlyPurchase","userinfo":{"name":username, "email":email, "pass":password}, "callbackFunc":function(responseAsJSON){//responseAsJSON is what we you back from swift
			var purchaseResponse = JSON.parse(responseAsJSON)
			//document.querySelector("#messages_from_swift").innerText = "Count is "+purchaseResponse
			//do ajax on success to setup user on PHP server

			//then reset the url of the webview to your php server
			window.location = "http://www.apple.com/"
			document.querySelector("#test").innerText = window.location
		}.toString()}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)
	}

