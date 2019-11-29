package Client4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;


public class ClientHandler {
    static int chunkNum;
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server
    int myPort;

    public void run(int ServerPort,int MyPort, int PeerServerPort){
        this.myPort=MyPort;
        new getFileFromServer(ServerPort).start();
        new ServerPeer(MyPort).start();
        new getFileThread(PeerServerPort).start();
    }

    private class getFileFromServer extends Thread{
        Socket socket;
        int port;

        public getFileFromServer(int serverPort) {
            port=serverPort;
        }

        public void run(){
            System.out.println("Getting the first chunk from server");
            try {
                socket = new Socket("localhost",8000);
                System.out.println("Connected to localhost in port 8000");
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                File dir = new File("test.pdf.001");
                String directory = dir.getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(directory);
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[102400];
                int data=is.read(buffer);
                System.out.println(data);
                fos.write(buffer,0,data);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try{
                    in.close();
                    out.close();
                    socket.close();
                }catch (IOException io){
                    io.printStackTrace();
                }
            }

        }
    }

    private class ServerPeer extends Thread{
        int port;
        ServerSocket serverSocket;

        ServerPeer(int ServerPort){
            port=ServerPort;
        }

        public void run(){
            try {
                serverSocket=new ServerSocket(port);
                System.out.println("Server is up and running");
                try {
                    while (true){
                        Socket socket=serverSocket.accept();
                        if (socket!=null) {
                            System.out.println("connected!");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class getFileThread extends Thread{
        Socket socket;
        int port;
        getFileThread(int getFromPrott){
            port=getFromPrott;
        }

        public void run(){
            System.out.println("Start to get file from"+ port);
            while (chunkNum<5){
                try {
                    //System.out.println("Trying to connect at port"+ port);
                    try {
                        socket = new Socket("localhost", port);
                        if (socket!=null){
                            System.out.println("Connection establised");
                            System.out.println("This is a simulation of getting file");
                            chunkNum++;
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }

                } finally {
                    if (socket!=null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    // this method gets the name of the full file from one chunk
    public static List<File> listOfFiles(File oneOfFiles){
        String tempName = oneOfFiles.getName();
        String targetName = tempName.substring(0,tempName.lastIndexOf('.'));
        File [] files= oneOfFiles.getParentFile().listFiles((File dir,String name) -> name.matches(targetName + "[.]\\d+"));
        Arrays.sort(files);
        for (int i=0;i<files.length;i++){
            System.out.println(files[i].getName());
        }
        return Arrays.asList(files);
    }


    //this method merge the list of chunk into a complete file.

    public static void mergeFiles(File oneOfFiles, File into) throws IOException {
        List<File> files = listOfFiles(oneOfFiles);
        FileInputStream fis;
        FileOutputStream fos = new FileOutputStream(into);
        //int bytesRead=0;
        //BufferedOutputStream mergingStream = new BufferedOutputStream(fos);
        for (int i=0;i<files.size();i++){
            System.out.println(files.get(i).getName());
        }
        byte[] fileBytes;
        for (File f : files) {
            // Files.copy(f.toPath(), mergingStream);
            fis=new FileInputStream(f);
            fileBytes=new byte[(int)f.length()];
            fis.read(fileBytes,0,(int)f.length());
            fos.write(fileBytes);
            fos.flush();
            //fileBytes=null;
            fis.close();
            //fis=null;
        }
        fos.close();
        fos=null;
    }
   /* public static void main(String args[]) throws IOException {
        ClientHandler client = new ClientHandler();
        chunkNum=0;
        File filePointer=new File("/Users/lukeyuan/Documents/P2PFileSystem/test.pdf.001");
        File output=new File("output.pdf");
        //mergeFiles(filePointer,output);
        client.run();
    }*/



    /*void run(int ServerPort, int myPort, int PeerServerPort)
    {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            //String filename=(String)in.readObject();
            File dir = new File("test.pdf.001");
                //System.out.println(filename);
            String directory =dir.getAbsolutePath();
                //FileOutputStream fos = new FileOutputStream("/Users/lukeyuan/IdeaProjects/TCPServerClient/src/Client/test2.txt");
            FileOutputStream fos = new FileOutputStream(directory);
            InputStream is = requestSocket.getInputStream();
            byte[] buffer = new byte[102400];
            int data;
            data = is.read(buffer);
                //System.out.println(data);
            fos.write(buffer,0,data);
            fos.flush();
                *//*System.out.print("Hello, please input a sentence: ");
                //read a sentence from the standard input
                message = bufferedReader.readLine();
                //Send the sentence to the server
                sendMessage(message);
                //Receive the upperCase sentence from the server
                MESSAGE = (String)in.readObject();
                //show the message to the user
                System.out.println("Receive message: " + MESSAGE);*//*

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }*/
    //send a message to the output stream
    void sendMessage(String msg)
    {
        try{
            //stream write the message
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
