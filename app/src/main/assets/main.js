
	var clicks = 0

	var theURL = 'https://www.google.com/'

      // set the root domain location for development, stage, or production
      var sysRoot = 'local'

      var servicesRoot = ''
      if (sysRoot == 'local') {
        servicesRoot = 'http://localhost/f5admin/services/'
      } else if (sysRoot == 'staging') {
        servicesRoot = 'http://ec2-54-152-204-90.compute-1.amazonaws.com/services/'
      } else if (sysRoot == 'prod') {
        servicesRoot = 'https://www.f5admin.com/services/'
      } else {
        var rootError = 'Code location specified incorrectly'
      }

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
			document.querySelectorAll(".req_fields")[i].style.display = "block";
		}
	}

	function confirmPurchase() {
		document.querySelector(".req_fields").style.display = "none";
		var message = ""
             var email = document.querySelector("#email").value
		var confEmail = document.querySelector("#confEmail").value
		var password = document.querySelector("#password").value
		var confirmPwd = document.querySelector("#confirmPwd").value
		// TODO: error handling for incorrect user input
		//do a local pw confirm here
		//if fails, don't continue
		if (email && confEmail && password && confirmPwd) {
			if (confirmPwd == password && confEmail == email) {
				//{"name":username, "mail":email, "pass":password}

				message = {"cmd":"requestMonthlyPurchase","userinfo":{"email":email, "pass":password}, "callbackFunc":function(responseAsJSON){//responseAsJSON is what we you back from swift
					var purchaseResponse = JSON.parse(responseAsJSON)
					//document.querySelector("#messages_from_swift").innerText = "Count is "+purchaseResponse

					// do ajax on success to setup user on PHP server
                                var xhr = new XMLHttpRequest()
                                var postUrl = servicesRoot + '/sec.php'

                                // set up the stateChange callback
                                xhr.onreadystatechange = function() {
                                  if (xhr.readyState == 4 && xhr.status == 200) {
                                    var acctcreateResponse = JSON.parse(xhr.responseText);
                                    acctcreateCallback(acctcreateResponse);
                                  }
                                }

                                xhr.open("POST", postUrl, true)
                                xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")

                               //TODO: find out what to put in place of the term variable for a month term.
                               //TODO: Find out what to replace the "stripetoken" variable with.
                               //TODO: Might need to wrap the call back in an actual function and only sent the function name
                               xhr.send(JSON.stringify({"called":"sec",
                                                                       "params":{
                                                                          "sentdata":[{
                                                                            "username": email,
                                                                            "email": email,
                                                                            "password": password,
                                                                            "promocode":"",
                                                                            "term": "some term",
                                                                            "stripetoken": applegoogleToken ,
                                                                            "req": "acctcreate"
                                                                          }]}}))



					//then reset the url of the webview to your php server
					document.querySelector("#test").innerText = window.location
				}.toString()}
			} else {
				message = {"cmd":"errorMsg", "msg":"Email or passwords do not match"}
				document.querySelector("#login_error").innerText = "* Email or passwords do not match"
//				document.querySelector(".req_fields").style.display = "block"
				displayError()
			}
		} else {
			message = {"cmd":"errorMsg", "msg":"Required fields must be entered"}
			document.querySelector("#login_error").innerText = "* Required fields must be entered"
//			document.querySelector(".req_fields").style.display = "block"
			displayError()
		}
		var messageAsString = JSON.stringify(message)
		native.postMessage(messageAsString)
	}

      // callback function that runs after creating the user in sec.php
      function acctcreateCallback(data) {
        // body of the callback after user has been created
        //TODO: check the data object returned from sec.php to see if everything went well

        // if everything is good send the user on to the app webview
        // replacePageWithURL("http://ec2-54-152-204-90.compute-1.amazonaws.com/app/")
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

