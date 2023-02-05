package server.speaker;


public class Speaker {
<<<<<<< HEAD
    private String mac;
    private String name;
=======
    public String mac;
    public String name;
>>>>>>> c9ee19ca7f8f0b4b3e357cd6ae29bfbb2e65ab9b

    public Speaker(String id, String name) {
        this.name = name;
    }

    public String getMAC() {
        return this.mac;
    }

    public String getName() {
        return this.name;
    }
}