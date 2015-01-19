package proxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alex on 14/11/14.
 */
public class PerceptionPoint {
    private StateMachine.PerceptionPointStates currentState;
    private String host="localhost";
    private int port;
    private Socket sendSocket;
    private ServerSocket receiveSocket;

    public PerceptionPoint()throws IOException {
        currentState = StateMachine.PerceptionPointStates.SENDING_LOCATION_BROADCAST;
    }

    private PrintWriter getWriter(Socket socket)throws IOException{
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut,true);
    }
    private BufferedReader getReader(Socket socket)throws IOException{
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }
    public String echo(String msg) {
        return "client:" + msg;
    }
    public void sendTalk()throws IOException {
        port = 8000;
        sendSocket = new Socket(host, port);
        if (receiveSocket == null)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        receiveTalk();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        try {
            BufferedReader br=getReader(sendSocket);
            PrintWriter pw=getWriter(sendSocket);
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
                sendSocket.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void receiveTalk()throws IOException {
        port = 8001;
        receiveSocket = new ServerSocket(port);
        while (true) {
            Socket socket=null;
            try {
                socket = receiveSocket.accept(); //等待客户连接
                System.out.println("New clientConnection accepted "
                        +socket.getInetAddress() + ":" +socket.getPort());
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

    public static void main(String args[])throws IOException{
        new PerceptionPoint().sendTalk();
    }
}

