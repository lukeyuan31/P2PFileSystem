import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PServer{
    private static int sPort;

    public static void splitFile(File f) {
        int chunkCounter = 1;
        int chunkSize = 102400;
        byte[] buffer = new byte[chunkSize];
        String filename = f.getName();
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                String chunkName = String.format("%s.%03d", filename, chunkCounter++);
                File newFile = new File(f.getParent(), chunkName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String args[]) throws IOException {
        if(args.length==1){
            sPort=Integer.parseInt(args[0]);
        splitFile(new File("test.pdf"));
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while(true) {
                new Handler(listener.accept(),clientNum).start();
                System.out.println("Client "  + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }
        }else{
            System.out.println("Wrong input!");
        }
    }



    private static class Handler extends Thread{
        private String message;
        private final int no;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private Socket connection;
        private String MESSAGE;


        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }



            public void run(){
            try{
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                //outputStream.flush();
                inputStream = new ObjectInputStream(connection.getInputStream());
                String clientNum=(String)inputStream.readObject();
                String filename="test.pdf.00"+clientNum;
                //System.out.println(filename);

                
                    File dir=new File(filename);
                    if (dir.exists()){
                        String directory = dir.getAbsolutePath();
                        FileInputStream fis=new FileInputStream(directory);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        OutputStream os = connection.getOutputStream();
                        //int length=directory.length();
                        //sendMessage(filename);
                        byte[] bytes = new byte[(int)dir.length()];
                        bis.read(bytes,0,bytes.length);
                        //int data;
                        //data = fis.read(bytes);
                        System.out.println(bytes.length);
                        os.write(bytes, 0, bytes.length);
                            // System.out.println(data);
                        //connection.close();
                        //System.out.println("get complete");
                        os.flush();
                        System.out.println("Chunk"+clientNum+ " sent to peer");
                        connection.close();
                    }
                    while (true) {
                        message = (String) inputStream.readObject();
                        System.out.println("Receive message: " + message + "from client" + no);
                        MESSAGE = message.toUpperCase();
                        sendMessage(MESSAGE);

                    }
                
            } catch (Exception e) {
                System.out.println("Disconnect with Peer");
            }
            finally {
                try {
                    inputStream.close();
                    outputStream.close();
                    connection.close();
                }
                catch (IOException ioException){
                    System.out.println("Disconnect with Peer");
                }
            }
        }

        public void sendMessage(String msg){
            try{
                outputStream.writeObject(msg);
                outputStream.flush();
                System.out.println("Send message: "+ msg+ "to client"+ no);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }




}