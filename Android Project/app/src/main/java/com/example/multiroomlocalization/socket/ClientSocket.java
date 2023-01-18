public class ClientSocket extends Thread {
    private final int port;
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    private string ip;

    @Override
    public void run() {

    }

    //This thread inizialize the socket and requires for a connection to the server
    public class Connect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(ip, port);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());

                // DA INSERIRE IL PRIMO SCAMBIO MESSAGGI
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class Disconnect extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                dataOut.writeUTF("");
                dataOut.flush();
                socket.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //classe per l'invio dei messaggi JSON all'applicativo python
    public class MessageSender extends AsyncTask<Void,Void,Void> {

        private String message;

        protected MessageSender(JSONObject message) {
            this.message = message.toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}