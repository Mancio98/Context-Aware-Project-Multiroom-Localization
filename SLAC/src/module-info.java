module slac {
	requires com.google.gson;
<<<<<<< HEAD
=======
	opens server.localization to com.google.gson;
	opens server.speaker to com.google.gson;
	opens server.messages to com.google.gson;
	opens server.messages.localization to com.google.gson;
	opens server.messages.connection to com.google.gson;
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b
}