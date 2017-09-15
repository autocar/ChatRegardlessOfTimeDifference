This is a social app running on Android device. Users can sign in with their Google account and chat with friends by sending texts, voice messages images and emoji.

Designed this app following MVC(model-view-controller) routine. 
Created several classes which contain the account information, downloaded messages, etc. 
Constructed a chat window using a Recycler View and several Image Views. 
Combined models with views using a controller class. 

Users can check their friends¡¯ time zone by clicking a button. When the user send a message to a friend who may be sleeping, this app will remind the user of the time difference, then the user can either send it out directly or recall this message.



FOR BACKEND  /****************************************************************************************************************/

We deploy our backend on google app engine that include 3 php file: register.php, push_notification.php, quickstart.php. 

register.php is working for register service. Android client will post their ID and Authentication Tokens which is used for pushing notifications. When the server receives a POST request to register, register.php will be called to receive ID and Token and store them into google cloud SQL.If there is a same ID in SQL, the token will be updated.

push_notification.php is working for pushing notification service. Android clien will post a request with a ID and a message. When the server receives a POST request to push a notification, push_notification.php will be called to finish this work. Firstly, it will find the token in google cloud SQL according to the ID it received. Then it will send a request with the message and the token to FCM(firebase cloud messaging). When FCM receives the token and the message, it will send a notification with that message to a android client pointed by the token.

quickstart.php is working for Speech to Text service. When a android client post a Speech to Text request with a URL where the audio file store. When the server receive this request, quickstart.php will be called and use google speech api to do this. Then it will send the result back to that android client.





FOR FRONT END  /****************************************************************************************************************/

The front end is a messaging app. 

Users can send pictures, voice messages and text messages to another user. All messages(pictures' download url, voice messages' download url and text messages) are sent to Firebase real-time database and stored on it by Firebase real-time database API. And all files(picture files, audio files) are sent to Firebase Storage and stored on it using Firebase Storage API. When the user want to watch a full screen picture or listen a voice message. It will be downloaded from Firebase Storage using the download url. 

Every user will store their time zone on firebase real-time database and update them termly which can be get by other users. When you want to send a message(any type message) to someone whose current time is later than 11PM or earlier than 8AM, there will be a alert popup to warn you that maybe he/her is sleeping. There are two choices you can click on. The first one is SEND. When you click it, the message will be sent out. Another one is Cancel. When you click it, the message would not be sent out. There is also a "time in there" button, so you can see the current time on the other side whenever you want.  

The voice messages are recorded by the audiorecord. The sampling rate is 16000 and the encoding mode is configured to PCM_16bit. Particularly, then endian that is used to store the audio file should be set to little endian. At the beginning, we meet a bottleneck in here, since the google speech API documents don't mention that we should submit a audio file that is stored in little endian. So, we don't know why the speech API doesn't work for one week. Finally we find the problem using a audio software named SoX. So, when a user click a voice message for a short time, the audio will be played. When a user click a voice message for a long time, the audio file's download url will be sent to the backend and transfromed to a text, then, the user can get the result and it is displayed by a popup. 

When the message is sent out, a notification request will be sent to the backend too. So, when another user's app is run in background. he will receive a nitification: "You got a message!". 

And every user will send their ID and token to the backend and update them termly. ID and token are used for receive notification