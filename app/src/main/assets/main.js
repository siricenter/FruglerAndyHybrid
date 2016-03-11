/*
 The MIT License (MIT)
 
 Copyright (c) 2015 Lee Barney
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

//you can put your regular JavaScript code in this file or any other.

var clicks = 0

var theURL = 'http://ec2-54-152-204-90.compute-1.amazonaws.com'
var theURL = 'https://www.google.com/'

// set the root domain location for development, stage, or production
var sysRoot = 'staging'

var servicesRoot = ''
if (sysRoot == 'local') {
    servicesRoot = 'http://localhost/f5admin/services/'
} else if (sysRoot == 'staging') {
    servicesRoot = 'http://ec2-54-152-204-90.compute-1.amazonaws.com/services'
} else if (sysRoot == 'prod') {
    servicesRoot = 'https://www.f5admin.com/services/'
} else {
    var rootError = 'Code location specified incorrectly'
}
var google = "https://www.google.com/"

var messageAsString

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
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    
}

window.onload = function() {
    message = {"cmd":"log", "string":"JS onload()" }
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    
    // check if there was a purchase error
    var message = {"cmd":"onload", "callbackFunc":function(responseAsJSON){
        response = JSON.parse(responseAsJSON)
        
        message = {"cmd":"log", "string":"JS onLoad purchaseERROR: " + response['purchaseError']}
        messageAsString = JSON.stringify(message)
        native.postMessage(messageAsString)
        
        if (response['purchaseError'] == "true") {
            document.querySelector("#login_error").innerText = "* Purchase Error Try again"
            var email = response['user_email']
            message = {"cmd":"log", "string":"JS purchaseERROR user_email:" + email}
            messageAsString = JSON.stringify(message)
            native.postMessage(messageAsString)
            // TODO: delete/unauth user from our DB
            
            var xhr = new XMLHttpRequest()
            var postUrl = servicesRoot + '/sec.php/'
            
            xhr.onreadystatechange = function() {
                message = {"cmd":"log", "string": "Delete inside onreadystatechange " + xhr.readyState + " " + xhr.status}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)
                
                if (xhr.readyState == 4 && xhr.status == 200) {
                    message = {"cmd":"log", "string": "Delete works! onreadystatechange " + xhr.readyState + " " + xhr.status}
                    messageAsString = JSON.stringify(message)
                    native.postMessage(messageAsString)
                    var acctcreateResponse = JSON.parse(xhr.responseText);
                    
                    if (!acctcreateResponse.errmsg) {
                        message = {"cmd":"log", "string": "response stuff: " + acctcreateResponse.user_id}
                        messageAsString = JSON.stringify(message)
                        native.postMessage(messageAsString)

                    } else {
                        message = {"cmd":"log", "string": "Error from sec.php: " + acctcreateResponse.errmsg}
                        messageAsString = JSON.stringify(message)
                        native.postMessage(messageAsString)
                    }
                }
                
                
            }
            
            xhr.open("POST", postUrl, true)
            xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
            
            var data = JSON.stringify({"sentdata": [{
                                                    "username": email,
                                                    "email": email,
                                                    "promocode":"",
                                                    "term": "1",
                                                    "stripetoken": "applegoogleToken",
                                                    "req": "reg_deleteuser"
                                                    }]})
            
            xhr.send(data)
        } else {
            // do nothing
        }}.toString()
    }
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
}

var displayError = function() {
    for (var i = 0; i < 4; i++) {
        document.querySelectorAll(".req_fields")[i].style.display = "block";
    }
}

function restorePurchases() {
    message = {"cmd":"log", "string":"JS restorePurchases()"}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    
    message = {"cmd":"restorePurchases", "callbackFunc":function(responseAsJSON){
        var response = JSON.parse(responseAsJSON);
        
        message = {"cmd":"log", "string":"restorePurchases() callback: " + response['restore']}
        messageAsString = JSON.stringify(message)
        native.postMessage(messageAsString)
        
        replacePageWithURL('http://ec2-54-152-204-90.compute-1.amazonaws.com/app')
    }.toString()
    }
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
}

function confirmPurchase() {
    message = {"cmd":"log", "string":"does this confirm button work?"}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    document.querySelector(".req_fields").style.display = "none";
    var message = ""
    var purchaseBtn = document.querySelector("#purchase-btn")
    var processingBtn = document.querySelector("#processing-btn")
    var email = document.querySelector("#email").value
    var confEmail = document.querySelector("#confEmail").value
    var password = document.querySelector("#password").value
    var confirmPwd = document.querySelector("#confirmPwd").value
    
    message = {"cmd":"log", "string": "email contents outside: " + email}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)

    // TODO: error handling for incorrect user input
    //do a local pw confirm here
    //if fails, don't continue
    if (email && confEmail && password && confirmPwd) {
        if (confirmPwd == password && confEmail == email) {
            //{"name":username, "mail":email, "pass":password}
            
            purchaseBtn.style.display = "none"
            processingBtn.style.display = "block"

            createAccount(email, password, function(email, ePass) {
                message = {"cmd":"log", "string":"Test that this is getting called " + email + " " + ePass}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)

                message = {"cmd":"requestMonthlyPurchase", "email": email, "ePass": ePass, "callbackFunc":function(responseAsJSON){
                    // responseAsJSON is what we get back from swift
                    var response = JSON.parse(responseAsJSON)

                    var email = response['user_email']
                    var ePass = response['ePass']

                    message = {"cmd":"log", "string":"email = " + response['user_email'] + " password = " + response['ePass']}
                    messageAsString = JSON.stringify(message)
                    native.postMessage(messageAsString)

                    message = {"cmd":"log", "string": "successCallback URL: " + "http://ec2-54-152-204-90.compute-1.amazonaws.com/app/?email='" + email + "'&password=" + ePass + "'"	}
                    messageAsString = JSON.stringify(message)
                    native.postMessage(messageAsString)
                    // load our webview
//                    replacePageWithURL("http://ec2-54-152-204-90.compute-1.amazonaws.com/app/?email='" + email + "'&password='" + ePass + "'")
//                    replacePageWithURL(google)
                }.toString()}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)

            }, function() {
                // handle the account creation fail
                // Potential reasons: duplicate email
                message = {"cmd":"log", "string":"User creation error"}
                document.querySelector("#login_error").innerText = "* User creation error (email already in use)"

                purchaseBtn.style.display = "block"
                processingBtn.style.display = "none"
            })
            
        } else {
            message = {"cmd":"log", "string":"Email or passwords do not match"}
            document.querySelector("#login_error").innerText = "* Email or passwords do not match"
            //				document.querySelector(".req_fields").style.display = "block"
            displayError()
        }
    } else {
        message = {"cmd":"log", "string":"Required fields must be entered"}
        document.querySelector("#login_error").innerText = "* Required fields must be entered"
        //			document.querySelector(".req_fields").style.display = "block"
        displayError()
    }
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
}

function createAccount(email, password, successCallback, failureCallback) {

//    var ePass = btoa(CryptoJS.AES.encrypt(password, "Frugler:dealzfordayz!"));
//    successCallback(email, ePass)

    // TODO: figure out how to keep the callback from being fired prematurely
    
    // do ajax, on success setup user on PHP server
    var xhr = new XMLHttpRequest()
    var postUrl = servicesRoot + '/sec.php/'
    
    message = {"cmd":"log", "string": "password contents unencrypted: " + password}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    
    var ePass = btoa(CryptoJS.AES.encrypt(password, "Frugler:dealzfordayz!"));
    
    message = {"cmd":"log", "string": "password contents encrypted: " + ePass}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)
    
    
    xhr.onreadystatechange = function() {
//        message = {"cmd":"log", "string": "inside onreadystatechange " + xhr.readyState + " " + xhr.status}
//        messageAsString = JSON.stringify(message)
//        native.postMessage(messageAsString)
        
        if (xhr.readyState == 4 && xhr.status == 200) {
            message = {"cmd":"log", "string": "works! onreadystatechange " + xhr.readyState + " " + xhr.status}
            messageAsString = JSON.stringify(message)
            native.postMessage(messageAsString)

            var acctcreateResponse = JSON.parse(xhr.responseText);
            
            if (!acctcreateResponse.errmsg) {

                message = {"cmd":"log", "string": "response stuff: " + acctcreateResponse.user_id}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)
                
                message = {"cmd":"log", "string": "createAccount URL: " + "http://ec2-54-152-204-90.compute-1.amazonaws.com/app/?email='" + email + "'&password=" + ePass + "'"	}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)

                // call successCallback to load our webview
                successCallback(email, ePass)
            } else {
                message = {"cmd":"log", "string": "Error from sec.php: " + acctcreateResponse.errmsg}
                messageAsString = JSON.stringify(message)
                native.postMessage(messageAsString)

                // call failureCallback because of an error creating user
                failureCallback()
            }
        }
    }
    
    xhr.open("POST", postUrl, true)
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
    
//    var readyState = xhr.readyState
//    message = {"cmd":"log", "string": "outside onreadystatechange " + readyState + " " + xhr.status}
//    native.postMessage(messageAsString)
    
    var data = JSON.stringify({"sentdata": [{
                                            "username": email,
                                            "email": email,
                                            "password": password,
                                            "promocode":"",
                                            "term": "1",
                                            "stripetoken": "applegoogleToken",
                                            "req": "acctcreate"
                                            }]})

    
    document.querySelector("#test").innerText = email.value + " " + password.value
    
    message = {"cmd":"log", "string": "email after data def: " + data}
    messageAsString = JSON.stringify(message)
    native.postMessage(messageAsString)

    xhr.send(data)

    //then reset the url of the webview to your php server
    //document.querySelector("#test").innerText = window.location
}

function replacePageWithURL(aURL){
    if(aURL){
        message = {"cmd":"log", "string": "What is the url: " + aURL}
        messageAsString = JSON.stringify(message)
        native.postMessage(messageAsString)
        if(navigator.userAgent.match(/Android/i)) {
            var loadMessage = {"cmd":"load_page","url":aURL}
            messageAsString = JSON.stringify(loadMessage)
            native.postMessage(messageAsString)
        } else{
            window.location = aURL
            message = {"cmd":"log", "string": "iOS should be redirecting to: " + aURL}
            messageAsString = JSON.stringify(message)
            native.postMessage(messageAsString)
        }
    }
}