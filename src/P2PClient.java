import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.*;

public class P2PClient {
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server


    /*public static void mergeFile(List<File> files, File into) throws IOException{
        try {
            FileOutputStream fos = new FileOutputStream(into);
            BufferedOutputStream mergingStream = new BufferedOutputStream(fos);
            for (File f : files){
                Files.copy(f.toPath(),mergingStream);
            }
        }
    }*/


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
    public static void main(String args[]) throws IOException {
        P2PClient client = new P2PClient();
        File filePointer=new File("/Users/lukeyuan/Documents/P2PFileSystem/test.pdf.001");
        File output=new File("output.pdf");
        mergeFiles(filePointer,output);
        client.run();
    }

    public void Client() {}

    void run()
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
            while(true)
            {
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
                System.out.print("Hello, please input a sentence: ");
                //read a sentence from the standard input
                message = bufferedReader.readLine();
                //Send the sentence to the server
                sendMessage(message);
                //Receive the upperCase sentence from the server
                MESSAGE = (String)in.readObject();
                //show the message to the user
                System.out.println("Receive message: " + MESSAGE);
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
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
    }
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
