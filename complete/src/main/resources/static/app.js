var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#connect1").prop("disabled", connected);
    $("#connect2").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function logout() {
	$.get("/logout", function(userId) {
		showGreeting(userId + ' loggedout');
	});
}
function fnStompSetup(userId){
    var socket = new SockJS('/websocketapi');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/public', function (greeting) {
            showGreeting('Pub: ' + JSON.parse(greeting.body).content);
        });
        stompClient.subscribe('/user/queue/private', function (greeting) {
            showGreeting('Pri: ' + JSON.parse(greeting.body).content);
        });
    }, function(message){
    	console.log('Server disconnected the WebSocket');
    	setConnected(false);
    });
}
function connect() {
	$.get("/subscribe4PrivateMsgs",{"_":$.now() }, fnStompSetup());
}
function connect1() {
	$.get("/lessAndSlow",{"_":$.now() }, fnStompSetup());
}
function connect2() {
	$.get("/lotsAndFast",{"_":$.now() }, fnStompSetup());
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/public", {}, JSON.stringify({'name': $("#name").val()}));
}

function sendPrivateMsg() {
    stompClient.send("/app/private", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").prepend("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#connect1" ).click(function() { connect1(); });
    $( "#connect2" ).click(function() { connect2(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( "#sendPrivate" ).click(function() { sendPrivateMsg(); });
    $( "#logout" ).click(function() { logout(); });
});

