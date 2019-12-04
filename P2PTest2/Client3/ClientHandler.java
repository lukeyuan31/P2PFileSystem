//package Client3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ClientHandler {
    static int chunkNum;
    Socket requestSocket;           //socket connect to the server

    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server
    int myPort;
    static List<Integer> chunkList = new ArrayList<Integer>(); // the list of chunk that current user has.

    static List<Integer> getChunkList(){
        return chunkList;
    }

    public void run(int ServerPort,int MyPort, int PeerServerPort){
        this.myPort=MyPort;
        chunkList.add(3);
        //chunkList.add(3);
        chunkNum=1;
        new getFileFromServer(ServerPort).start();
        new ServerPeer(MyPort).start();
        new getFileThread(PeerServerPort).start();


    }

    private class getFileFromServer extends Thread{
        Socket socket;
        int port;
        ObjectOutputStream out;         //stream write to the socket
        ObjectInputStream in;          //stream read from the socket

        public getFileFromServer(int serverPort) {
            port=serverPort;
        }

        public void run(){
            System.out.println("[Peer] Getting the first chunk from server");
            try {
                socket = new Socket("localhost",port);
                System.out.println("[Peer] Connected to localhost in port "+port);
                out = new ObjectOutputStream(socket.getOutputStream());
                //out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("[Peer] Sending peer number to server");
                sendMessage(out,"3");
                File dir = new File("test.pdf.003");
                String directory = dir.getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(directory);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                InputStream is = socket.getInputStream();
                /*
                byte[] buffer = new byte[102400];
                int data=is.read(buffer);
                System.out.println(data);
                fos.write(buffer,0,data);
                fos.flush();
                */
                int bytes=0;
                int read;
                while((read=is.read())!=-1){
                    bos.write(read);
                    bytes++;
                }
                bos.flush();
                bos.close();
                fos.close();
                is.close();
                //chunkList.add(1);
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
        ObjectOutputStream out;         //stream write to the socket
        ObjectInputStream in;          //stream read from the socket


        ServerPeer(int ServerPort){
            port=ServerPort;
        }

        public void run(){
            try {
                serverSocket=new ServerSocket(port);
                System.out.println("[ServerPeer] Server is up and running");
                try {
                    while (true){
                        Socket socket=serverSocket.accept();
                        if (socket!=null) {
                            out = new ObjectOutputStream(socket.getOutputStream());
                            out.flush();
                            in = new ObjectInputStream(socket.getInputStream());
                            System.out.println("[ServerPeer] Connected with peer!");
                            String receive=(String)in.readObject();
                            System.out.println("[ServerPeer] Received message: "+receive);
                            //split the message sent from user to execute the command
                            String[] input=receive.split(" ");
                            String response="";
                            switch (input[0]){
                                case "getList":{
                                    System.out.println("[ServerPeer] Received getList command");
                                    List<Integer> chunkList = getChunkList();
                                    for (int i =0;i<chunkList.size();i++){
                                        response=response+chunkList.get(i)+" ";
                                    }
                                    sendMessage(out,response);
                                    //System.out.println("Send"+response);
                                    break;
                                }
                                case "getFile":{
                                    System.out.println("[ServerPeer] Received getFile command");
                                    response="Send "+input[1]+" to next peer";
                                    sendMessage(out,response);
                                    String filename="test.pdf.00"+input[1];
                                    File dir=new File(filename);
                                    try {
                                        if (dir.exists()){
                                            String directory = dir.getAbsolutePath();
                                            FileInputStream fis = new FileInputStream(directory);
                                            OutputStream os = socket.getOutputStream();
                                            byte[] bytes = new byte[(int)dir.length()];
                                            int data;
                                            data=fis.read(bytes);
                                            os.write(bytes,0,data);
                                            os.flush();
                                            os.close();
                                            socket.close();

                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                }
                            }

                        }
                    }
                } catch (IOException e) { e.printStackTrace(); } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) { e.printStackTrace(); }finally {
                try { serverSocket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

    }

    private class getFileThread extends Thread{
        Socket socket;
        int port;
        int nextChunk=-1;    //the next chunk that current user needs
        ObjectOutputStream out;         //stream write to the socket
        ObjectInputStream in;          //stream read from the socket
        getFileThread(int getFromPrott){
            port=getFromPrott;
        }

        public void run(){
            System.out.println("[Download] Start to get file from"+ port);
            while (chunkNum<5){
                try {
                    //System.out.println("Trying to connect at port"+ port);
                    try {
                        socket = new Socket("localhost", port);
                        if (socket!=null){
                            out = new ObjectOutputStream(socket.getOutputStream());
                            out.flush();
                            in= new ObjectInputStream(socket.getInputStream());
                            System.out.println("[Download] Connection establised");
                            String inputMessage;

                            if(nextChunk<0){
                                inputMessage="getList "+port;
                                sendMessage(out,inputMessage);
                                String response = (String)in.readObject();
                                System.out.println("[Download] The chunk list of previous peer is "+response);
                                String[] splittedResponse = response.split(" ");
                                if (splittedResponse.length>0){
                                    List<Integer> getChunkList = new ArrayList<Integer>();
                                    List<Integer> myList = getChunkList();
                                    for (int i=0;i<splittedResponse.length;i++){
                                        if (!myList.contains(Integer.parseInt(splittedResponse[i]))){
                                            getChunkList.add(Integer.parseInt(splittedResponse[i]));
                                        }
                                    }
                                    if (getChunkList.size()>0) {
                                        nextChunk = getChunkList.get(0);
                                        System.out.println("[Download] Getting chunk num " + nextChunk + "from previous peer");
                                    }
                                    else {
                                        sleep(1000);
                                    }
                                }


                            }
                            else if (nextChunk>0){
                                inputMessage="getFile "+nextChunk;
                                sendMessage(out,inputMessage);
                                String response = (String)in.readObject();
                                System.out.println(response);
                                String filename="test.pdf.00"+nextChunk;
                                File dir = new File(filename);
                                String directory = dir.getAbsolutePath();
                                FileOutputStream fos = new FileOutputStream(directory);
                                BufferedOutputStream bos=new BufferedOutputStream(fos);
                                InputStream is = socket.getInputStream();
                                /*
                                byte[] buffer = new byte[102400];
                                int data = is.read(buffer);
                                System.out.println(data);
                                fos.write(buffer,0,data);
                                fos.flush();
                                */
                                int bytes=0;
                                int read;
                                while((read = is.read()) !=-1){
                                    bos.write(read);
                                    bytes++;
                                }
                                bos.flush();
                                bos.close();
                                is.close();
                                fos.close();
                                chunkList.add(nextChunk);
                                nextChunk=-1;
                                chunkNum++;
                                System.out.println(chunkNum);
                            }
                            //System.out.println("This is a simulation of getting file");
                            // chunkNum++;
                        }
                        else {
                            sleep(1000);
                            System.out.println("[Download] No peer to connect, sleep for one sec");
                        }
                    } catch (Exception e) {
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
            File filePointer=new File("test.pdf.001");
            String directory=filePointer.getAbsolutePath();
            filePointer=new File(directory);
            File output=new File("test.pdf");
            try{
                mergeFiles(filePointer, output);
            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    // this method gets the name of the full file from one chunk
    public static List<File> listOfFiles(File oneOfFiles){
        String tempName = oneOfFiles.getName();
        String targetName = tempName.substring(0,tempName.lastIndexOf('.'));
        File [] files= oneOfFiles.getParentFile().listFiles((File dir,String name) -> name.matches(targetName + "[.]\\d+"));
        Arrays.sort(files);
        return Arrays.asList(files);
    }


    //this method merge the list of chunk into a complete file.

    public static void mergeFiles(File oneOfFiles, File into) throws IOException {
        List<File> files = listOfFiles(oneOfFiles);
        FileInputStream fis;
        FileOutputStream fos = new FileOutputStream(into);
        //int bytesRead=0;
        //BufferedOutputStream mergingStream = new BufferedOutputStream(fos);
        System.out.println("Merging Files...");
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
    void sendMessage(ObjectOutputStream out,String msg)
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

