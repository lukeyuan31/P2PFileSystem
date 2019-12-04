//package Client2;

//import Client1.ClientHandler;

public class Client {
    public static void main(String[] args){
        if(args.length==3){
            int listeningPort=Integer.parseInt(args[0]);
            int peerPort=Integer.parseInt(args[1]);
            int downloadPort=Integer.parseInt(args[2]);
        ClientHandler clientHandler=new ClientHandler();
            clientHandler.run(listeningPort,peerPort,downloadPort);
        }else{
            System.out.println("Wrong input!!");
        }
    }
}
