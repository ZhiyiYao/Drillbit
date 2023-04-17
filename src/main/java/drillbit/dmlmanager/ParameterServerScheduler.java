package drillbit.dmlmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ParameterServerScheduler {
    private static final int maxbuf = 2048;
    private int port;
    private String ServerIP;
    private int ServerPort;
    private boolean stopped;

    public ParameterServerScheduler(int port, String ServerIP, int ServerPort, ArrayList<String> nodes) {
        this.port = port;
        this.ServerIP = ServerIP;
        this.ServerPort = ServerPort;
        this.stopped = false;

        new Scheduler(nodes).start();
    }

    public boolean isStopped() {
        return stopped;
    }

    private class Scheduler extends Thread {
        private ArrayList<String> nodesIP;
        private int nodesNum;

        public Scheduler(ArrayList<String> nodes) {
            this.nodesIP = new ArrayList<>();
            nodesNum = nodes.size();
        }

        @Override
        public void run() {
            try {
                ensureServerConnection();
                //System.out.println("out of connect");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Socket serverConnection;
            Socket node;
            ArrayList<Socket> nodes = new ArrayList<Socket>();

            try {
                ServerSocket schedulerConnection = new ServerSocket(port);
                // get nodes connection
                for (int i = 0; i < nodesNum; i++) {
                    Socket client = schedulerConnection.accept();
                    InputStream reader = client.getInputStream();
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

                    byte[] buf = new byte[40];
                    int l;
                    while ((l = reader.read(buf)) != -1) {
                        byteArray.write(buf, 0, l);
                    }
                    nodesIP.add(byteArray.toString());

                    // send to server
                    serverConnection = new Socket(ServerIP, ServerPort);
                    OutputStream writer = serverConnection.getOutputStream();
                    writer.write(byteArray.toByteArray());
                    serverConnection.shutdownOutput();
                    serverConnection.close();

                    nodes.add(client);
                }

                startServer();

                // inform nodes
                for (int i = 0; i < nodesNum; i++) {
                    node = nodes.get(i);
                    OutputStream writer = node.getOutputStream();
                    writer.write("started".getBytes());
                    node.shutdownOutput();
                    node.close();
                }
                System.out.println("node num: " + nodesNum);

                //stop iteration
                for (int i = 0; i < nodesNum; i++) {
                    Socket client = schedulerConnection.accept();
                    InputStream reader = client.getInputStream();
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

                    byte[] buf = new byte[40];
                    int l;
                    while ((l = reader.read(buf)) != -1) {
                        byteArray.write(buf, 0, l);
                    }
                    //System.out.println("node: "+Integer.toString(i));
                    client.close();
                }
                schedulerConnection.close();

                stopServer();
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }

            stopped = true;
        }

        private void stopServer() {
            if (!serverConnect("stop", "stopped")) {
                throw new RuntimeException("scheduler server stop failed");
            }
            System.out.println("scheduler stop");
        }

        private void startServer() {
            if (!serverConnect("nodes done", null)) {
                throw new RuntimeException("scheduler server validation failed");
            }

            if (!serverConnect("start", "started")) {
                throw new RuntimeException("scheduler server start failed");
            }
        }

        private void ensureServerConnection() throws InterruptedException {
            while (!serverConnect("scheduler", "server")) Thread.sleep(100);
            //throw new RuntimeException("scheduler connection failed");
        }

        private boolean serverConnect(String send, String receive) {
            try {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                // send info to server
                //System.out.println(ServerIP);
                Socket serverConnection = new Socket(ServerIP, ServerPort);
                //System.out.println(ServerPort);
                OutputStream writer = serverConnection.getOutputStream();
                writer.write(send.getBytes());
                //writer.close();
                serverConnection.shutdownOutput();
                //System.out.println(send);

                // receive data
                if (receive != null) {
                    InputStream reader = serverConnection.getInputStream();
                    byte[] buf = new byte[maxbuf];
                    int l;
                    while ((l = reader.read(buf)) != -1) {
                        byteArray.write(buf, 0, l);
                    }
                    String result = byteArray.toString();
                    //reader.close();
                    //System.out.println(result);

                    // validation
                    if (!result.equals(receive)) {
                        //System.out.println(receive + " " + result);
                        serverConnection.close();
                        return false;
                    }
                }
                serverConnection.close();
            } catch (IOException e) {
                System.out.println(e.toString());
                return false;
            }
            //System.out.println("right");
            return true;
        }

    }
}
