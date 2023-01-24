module slac {
	requires com.google.gson;
	opens server.localization to com.google.gson;
	opens server.speaker to com.google.gson;
	opens server.messages to com.google.gson;
	opens server.messages.localization to com.google.gson;
	opens server.messages.connection to com.google.gson;
}