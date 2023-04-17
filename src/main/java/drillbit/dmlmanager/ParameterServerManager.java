package drillbit.dmlmanager;

import drillbit.optimizer.Optimizers;
import drillbit.parameter.Weights;
import drillbit.protobuf.SyncWeightsPb;
import drillbit.utils.common.DoubleAccumulator;
import drillbit.utils.parser.Serializer;
import drillbit.utils.parser.StringParser;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterServerManager {
    private static final int MAXBUFF = 1024;
    //if server or scheduler
    ParameterServerNode server;
    ParameterServerScheduler scheduler;
    //calculator info
    Optimizers.OptimizerBase op;
    //local info
    private int localPort;
    private String localIP;
    private boolean isServer;
    private boolean isScheduler;
    //other works
    private int serverPort;
    private int schedulerPort;
    private int schedulerServerPort;
    private String serverIP;
    private String schedulerIP;
    private ArrayList<String> nodesIP;
    private ArrayList<Integer> nodesPort;

    public ParameterServerManager(Optimizers.OptimizerBase optimizer) throws InterruptedException {
        nodesIP = new ArrayList<>();
        nodesPort = new ArrayList<>();
        isServer = false;
        isScheduler = false;

        op = optimizer;

        loadConfig();
        preStart();

        joinConnection();
    }

    public final ArrayList<Weights>
    batchUpdate(ArrayList<Weights> weights,
                @Nonnull ConcurrentHashMap<Object, DoubleAccumulator> accumulated) {
        if (isServer) {
            server.setWeights(weights);
        }

        boolean breakLoop = false;
        do {
            try {
                Socket client = new Socket(serverIP, serverPort);
                OutputStream writer = client.getOutputStream();

                writer.write(Serializer.accumulatedToByteArray(accumulated));
                client.shutdownOutput();

                InputStream reader = client.getInputStream();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();


                byte[] buf = new byte[MAXBUFF];
                int l;
                while ((l = reader.read(buf)) != -1) {
                    byteArray.write(buf, 0, l);
                }

                SyncWeightsPb.SyncWeightsArray weightArray = SyncWeightsPb.SyncWeightsArray.parseFrom(byteArray.toByteArray());
                for (int i = 0; i < weights.size(); i++) {
                    weights.get(i).fromByteArray(weightArray.getWeights(i).toByteArray());
                }
                breakLoop = true;
            } catch (IOException ignored) {

            }

        } while (!breakLoop);
        //System.out.println("circle");

        return weights;
    }

    public void stopIteration() {
        connectScheduler("stop", null);

        if (isScheduler) {
            try {
                while (!scheduler.isStopped()) Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.toString());
            }
        } else if (isServer) {
            try {
                while (server.isServerOn()) Thread.sleep(100);
                server.displayTime();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.toString());
            }
        }
    }

    private void preStart() {
        //System.out.print("prestart\n");

        if (isServer) {
            server = new ParameterServerNode(schedulerServerPort, serverPort, op, "avg");
        }

        if (isScheduler) {
            scheduler = new ParameterServerScheduler(schedulerPort, serverIP, schedulerServerPort, nodesIP);
        }
    }

    private void loadConfig() {
        Map<String, String> env = System.getenv();
        String sysPath = env.get("DRILL_HOME") + "/conf/DML.conf";
        InetAddress localHost;
        int context = 0;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            throw new RuntimeException(e1.toString());
        }
        localIP = localHost.getHostAddress();

        System.out.print("load configure\n");

        try {
            BufferedReader cfg = new BufferedReader(new FileReader(sysPath));
            while (true) {
                String line = cfg.readLine();
                if (line == null) {
                    break;
                }

                int pos = line.indexOf(':');

                if (pos != -1) {
                    String nodeIP = line.substring(0, pos);

                    if (!nodeIP.equals(localIP)) {
                        if (context == 0) {
                            nodesIP.add(line.substring(0, pos));
                            nodesPort.add(StringParser.parseInt(line.substring(pos + 1), 31015));
                        } else if (context == 1) {
                            serverIP = line.substring(0, pos);
                            String ports = line.substring(pos + 1);
                            pos = ports.indexOf(':');
                            serverPort = StringParser.parseInt(ports.substring(0, pos), 31016);
                            schedulerServerPort = StringParser.parseInt(ports.substring(pos + 1), 31018);
                            context += 1;
                            //System.out.println("context + 1\n");
                        } else {
                            schedulerIP = line.substring(0, pos);
                            schedulerPort = StringParser.parseInt(line.substring(pos + 1), 31017);
                        }
                    } else {
                        if (context == 0) {
                            nodesIP.add(line.substring(0, pos));
                            nodesPort.add(StringParser.parseInt(line.substring(pos + 1), 31015));
                            localPort = StringParser.parseInt(line.substring(pos + 1), 31015);
                        } else if (context == 1) {
                            isServer = true;
                            serverIP = localIP;
                            String ports = line.substring(pos + 1);
                            pos = ports.indexOf(':');
                            serverPort = StringParser.parseInt(ports.substring(0, pos), 31016);
                            schedulerServerPort = StringParser.parseInt(ports.substring(pos + 1), 31018);
                            context += 1;
                            //System.out.println("self context + 1\n");
                        } else {
                            isScheduler = true;
                            schedulerPort = StringParser.parseInt(line.substring(pos + 1), 31017);
                            //System.out.println("self context + 2\n");
                        }
                    }
                } else {
                    //System.out.println("context + 1\n");
                    context = 1;
                }
            }

            cfg.close();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }

        System.out.print("load configure over\n");
    }

    private void joinConnection() throws InterruptedException {
        while (!connectScheduler(localIP, "started")) Thread.sleep(100);
    }

    private boolean connectScheduler(@NotNull String send, String receive) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            // send info to server
            Socket serverConnection = new Socket(schedulerIP, schedulerPort);
            OutputStream writer = serverConnection.getOutputStream();
            writer.write(send.getBytes());
            serverConnection.shutdownOutput();

            // receive data
            if (receive != null) {
                InputStream reader = serverConnection.getInputStream();
                byte[] buf = new byte[MAXBUFF];
                int l;
                while ((l = reader.read(buf)) != -1) {
                    byteArray.write(buf, 0, l);
                }
                String result = byteArray.toString();
                reader.close();

                // validation
                if (!result.equals(receive)) {
                    serverConnection.close();
                    return false;
                }
            }
            //System.out.println(receive);
            serverConnection.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
