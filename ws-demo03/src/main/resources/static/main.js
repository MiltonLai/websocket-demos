// Try to set up WebSocket connection with the handshake at "http://localhost:8080/stomp"
let sock = new SockJS("http://127.0.0.1:8763/stomp");
// Create a new StompClient object with the WebSocket endpoint
let client = Stomp.over(sock);
/*
 Start the STOMP communications, provide a callback for when the CONNECT frame arrives. Provide the username and password
 in the headers
 */
client.connect({'username': 'Jimbob', 'password': 'pass'}, frame => {

  // Subscribe to "/topic/messages"
  client.subscribe("/topic/messages", payload => {
    let message_list = document.getElementById('message-list');
    let message = document.createElement('li');
    message.appendChild(document.createTextNode(JSON.parse(payload.body).message));
    message_list.appendChild(message);
  });

  // Subscribe to "/user/topic/private-messages", defined by @SendToUser
  client.subscribe("/user/topic/private-messages", payload => {
    let message_list = document.getElementById('message-list');
    let message = document.createElement('li');
    message.appendChild(document.createTextNode(JSON.parse(payload.body).message));
    message_list.appendChild(message);
  });
});

// Take the value in the ‘message-input’ text field and send it to the server with empty headers.
function sendMessage() {

  let input = document.getElementById("message-input");
  let message = input.value;

  client.send('/ws/chat', {}, JSON.stringify({message: message}));
}

function sendPrivateMessage() {
  let input = document.getElementById("message-private");
  let message = input.value;
  client.send('/ws/private-message', {}, JSON.stringify({message: message}));
}

