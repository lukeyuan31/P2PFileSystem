package Client5;

public class Client {
    public static void main(String[] args){
        ClientHandler clientHandler=new ClientHandler();
        clientHandler.run(8000,6005,6004);
    }
}
