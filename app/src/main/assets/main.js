
	var clicks = 0

	var theURL = "https://www.google.com/"

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
		var message = {"cmd":"onload", "callbackFunc":function(responseAsJSON){

                var response = JSON.parse(responseAsJSON)
                var token = (response['token'] != null ? "isNotNull" : null)
                document.getElementById("test").innerText = response['token']
                if (token != null) {
    				replacePageWithURL(theURL)
                }
			}.toString()
		}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)
	}

	var displayError = function() {
		for (var i = 0; i < 4; i++) {
			document.querySelector(".req_fields")[i].style.visibility = "visible";
		}
	}

	function confirmPurchase() {
		document.querySelector(".req_fields").style.visibility = "hidden";
		var message = ""
		var username = document.querySelector("#username").value
		var email = document.querySelector("#email").value
		var password = document.querySelector("#password").value
		var confirmPwd = document.querySelector("#confirmPwd").value
		// TODO: error handling for incorrect user input
		//do a local pw confirm here
		//if fails, don't continue
		if (username && email && password && confirmPwd) {
			if (confirmPwd == password) {
				//{"name":username, "mail":email, "pass":password}

				message = {"cmd":"requestMonthlyPurchase","userinfo":{"name":username, "email":email, "pass":password}, "callbackFunc":function(responseAsJSON){//responseAsJSON is what we you back from swift
					var purchaseResponse = JSON.parse(responseAsJSON)
					//document.querySelector("#messages_from_swift").innerText = "Count is "+purchaseResponse
					//do ajax on success to setup user on PHP server


					replacePageWithURL(theURL)
					// replacePageWithURL("http://ec2-54-152-204-90.compute-1.amazonaws.com/app/")


					//then reset the url of the webview to your php server
					document.querySelector("#test").innerText = window.location
				}.toString()}
			} else {
				message = {"cmd":"errorMsg", "msg":"Passwords do not match"}
				document.querySelector("#login_error").innerText = "* Passwords do not match"
//				document.querySelector(".req_fields").style.visibility = "visible"
				displayError()
			}
		} else {
			message = {"cmd":"errorMsg", "msg":"Required fields must be entered"}
			document.querySelector("#login_error").innerText = "* Required fields must be entered"
//			document.querySelector(".req_fields").style.visibility = "visible"
			displayError()
		}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)
	}

	function replacePageWithURL(aURL){
    	if(aURL){
    		if(navigator.userAgent.match(/Android/i)) {
    			var loadMessage = {"cmd":"load_page","url":aURL}
    			var messageAsString = JSON.stringify(loadMessage)
    			native.postMessage(messageAsString)
    		  }
    		  else{
    			window.location = aURL
    		  }
    	}
    }

