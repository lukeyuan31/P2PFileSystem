package Client2;

//import Client1.ClientHandler;

public class Client {
    public static void main(String[] args){
        ClientHandler clientHandler=new ClientHandler();
            clientHandler.run(8000,6002,6001);
    }
}
