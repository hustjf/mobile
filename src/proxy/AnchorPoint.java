package proxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alex on 14/11/14.
 */
public class AnchorPoint {
    private StateMachine.AnchorPointStates currentState;
    private String host="localhost";
    private int port;
    private ServerSocket receiveServerSocket;
    private Socket sendServerSocket;
    public AnchorPoint() {
        currentState = StateMachine.AnchorPointStates.WAITING_FOR_CONNECTION;
    }
    public String echo(String msg) {
        return "server:" + msg;
    }
    private PrintWriter getWriter(Socket socket)throws IOException{
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut,true);
    }
    private BufferedReader getReader(Socket socket)throws      IOException{

        InputStream socketIn = socket.getInputStream();

        return new BufferedReader(new InputStreamReader(socketIn));
    }
    public void receiveService() {
        port = 8000;
        try {
            receiveServerSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            Socket socket=null;
            try {
                socket = receiveServerSocket.accept(); //等待客户连接
                System.out.println("New serverConnection accepted "
                        +socket.getInetAddress() + ":" +socket.getPort());
                if (sendServerSocket == null)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendService();
                        }
                    }).start();
                BufferedReader br =getReader(socket);
                PrintWriter pw = getWriter(socket);
                String msg = null;
                while ((msg = br.readLine()) != null) {
                    System.out.println(msg);
                    pw.println(echo(msg));
                    if (msg.equals("bye")) //如果客户发送消息为“bye”，就结束通信
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(socket!=null)
                        socket.close(); //断开连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void sendService() {
        port = 8001;
        try {
            sendServerSocket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br=getReader(sendServerSocket);
            PrintWriter pw=getWriter(sendServerSocket);
            BufferedReader localReader=new BufferedReader(new InputStreamReader(System.in));
            String msg=null;
            while((msg=localReader.readLine())!=null){
                pw.println(msg);
                System.out.println(br.readLine());

                if(msg.equals("bye"))
                    break;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sendServerSocket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[])throws IOException {
        new AnchorPoint().receiveService();
    }
}
