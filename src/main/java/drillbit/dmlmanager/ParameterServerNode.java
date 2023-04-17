package drillbit.dmlmanager;

import com.google.protobuf.ByteString;
import drillbit.dmlmanager.synmethed.SynFactory;
import drillbit.dmlmanager.synmethed.SynMethed;
import drillbit.optimizer.Optimizers;
import drillbit.parameter.Weights;
import drillbit.protobuf.SyncWeightsPb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class ParameterServerNode {
    //constant
    private static int maxbuf = 2048;
    private final int port;
    private final int schedulerConnectionPort;
    //weights
    private ArrayList<Weights> weights;
    private ArrayList<String> labels;
    private byte[] weightStream;
    private SynMethed sync;
    //server states
    private boolean serverOn;
    private boolean serverStarted;
    //nodes information
    private int nodesNum;
    private ArrayList<String> nodes;
    //optimizer
    private Optimizers.OptimizerBase optimizer;
    //statics
    private long time;

    public ParameterServerNode(int schedulerConnectionPort,
                               int port,
                               Optimizers.OptimizerBase optimizer,
                               String aggregation) {
        serverOn = false;
        serverStarted = false;
        this.port = port;
        this.schedulerConnectionPort = schedulerConnectionPort;
        this.optimizer = optimizer;

        nodes = new ArrayList<>();
        new schedulerConnection().start();

        labels = null;

        sync = SynFactory.getMethed(aggregation);
    }

    public void setWeights(ArrayList<Weights> weightsInput) {
        weights = weightsInput;
    }

    public void setLabels(ArrayList<String> labelsInput) {
        labels = labelsInput;
    }

    public void startTest() {
        new schedulerConnection().start();
    }

    public void displayTime() {
        System.out.println("server process time: " + time);
    }

    public boolean isServerOn() {
        return serverOn;
    }

    private class schedulerConnection extends Thread {
        ServerSocket serverSchedulerConnection;

        public schedulerConnection() {
            try {
                serverSchedulerConnection = new ServerSocket(schedulerConnectionPort);
            } catch (IOException e) {
                throw new RuntimeException("server init failed");
            }
        }

        @Override
        public void run() {
            System.out.print("server wait\n");
            ensureSchedulerConnection();

            serverStart();
            while (true) {
                String bytes = getInfoFromScheduler("stopped");
                if (bytes.equals("stop")) {
                    serverOn = false;
                    break;
                }
            }

            try {
                serverSchedulerConnection.close();
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }

        private void serverStart() {
            String info;

            try {
                // get nodes IP
                while (true) {
                    info = getInfoFromScheduler(null);

                    if (!info.equals("nodes done")) {
                        nodes.add(info);
                    } else {
                        break;
                    }
                }
                nodesNum = nodes.size();

                serverOn = true;
                new server().start();
                while (!serverStarted) {
                    try {
                        //System.out.println(serverStarted);
                        Thread.sleep(100);
                        //.println(serverStarted);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e.toString());
                    }
                }
                // send start info
                //System.out.println("final validation");
                getInfoFromScheduler("started");
            } catch (RuntimeException e) {
                throw e;
            }

        }

        private synchronized void ensureSchedulerConnection() {
            String result = getInfoFromScheduler("server");

            if (!result.equals("scheduler")) {
                throw new RuntimeException("server scheduler connection failed");
            }
        }

        private String getInfoFromScheduler(String send) throws RuntimeException {
            try {
                // get connection
                Socket client = serverSchedulerConnection.accept();

                // receive data
                InputStream reader = client.getInputStream();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byte[] buf = new byte[maxbuf];
                int l;
                while ((l = reader.read(buf)) != -1) {
                    byteArray.write(buf, 0, l);
                }
                //reader.close();

                // send info
                if (send != null) {
                    //System.out.println(send);
                    OutputStream writer = client.getOutputStream();
                    writer.write(send.getBytes());
                    client.shutdownOutput();
                    //writer.close();
                }

                client.close();
                //System.out.println(byteArray.toString());

                return byteArray.toString();
            } catch (IOException e) {
                throw new RuntimeException("server connection failed");
            }
        }
    }

    private class server extends Thread {
        private ServerSocket serverConnection;

        public server() {
            ;
        }

        @Override
        public void run() {
            waitSignal();
            long start = 0;
            // init server
            try {
                serverConnection = new ServerSocket(port);
            } catch (IOException e) {
                throw new RuntimeException("Server start error");
            }
            serverStarted = true;

            SynData accumulated, received;
            if (labels != null) {
                accumulated = new MultiWeightsAccumulator();
                received = new MultiWeightsAccumulator();
            } else {
                accumulated = new SimpleWeightsAccumulator();
                received = new SimpleWeightsAccumulator();
            }

            try {
                while (serverOn) {
                    Socket client;
                    //ps server
                    ArrayList<Socket> nodeConnection = new ArrayList<>();

                    for (int i = 0; i < nodesNum; i++) {
                        client = serverConnection.accept();
                        if (i == 0) {
                            start = System.currentTimeMillis();
                        }
                        //System.out.println("server accept: "+((InetSocketAddress)client.getRemoteSocketAddress()).getAddress().getHostAddress());
                        InputStream reader = client.getInputStream();
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

                        byte[] buf = new byte[maxbuf];
                        int l;
                        while ((l = reader.read(buf)) != -1) {
                            byteArray.write(buf, 0, l);
                        }
                        //accumulate
//                        ConcurrentHashMap<Object, DoubleAccumulator> received = Serializer.accumulatedFromByteArray(byteArray.toByteArray());
//                        for (ConcurrentHashMap.Entry<Object, DoubleAccumulator> e : received.entrySet()) {
//                            Object feature = e.getKey();
//                            DoubleAccumulator v = e.getValue();
//
//                            DoubleAccumulator acc = accumulated.get(ObjectParser.parseInt(feature));
//                            //System.out.println("recieve "+ObjectParser.parseInt(feature)+" acc: "+v.get());
//                            if (acc == null) {
//                                acc = new DoubleAccumulator(v.get());
//                                accumulated.put(ObjectParser.parseInt(feature), acc);
//                            } else {
//                                acc.add(v.get());
//                            }
//                        }
                        received.fromBytes(byteArray.toByteArray());
                        accumulated.addData(received);

                        nodeConnection.add(client);
                    }

                    //process
//                    for (ConcurrentHashMap.Entry<Integer, DoubleAccumulator> e : accumulated.entrySet()) {
//                        Object feature = e.getKey();
//                        DoubleAccumulator v = e.getValue();
//                        double value = v.get();
//                        //System.out.println("acc: "+value);
//                        double weight = weights.getWeight(feature);
//                        final double newWeight = optimizer.update(feature, weight, 0.0, value);
//                        weights.setWeight(feature, newWeight);
//                    }
                    sync.update(accumulated, labels, weights, optimizer);

                    SyncWeightsPb.SyncWeightsArray.Builder weightsBuilder = SyncWeightsPb.SyncWeightsArray.newBuilder();
                    for (Weights w : weights) {
                        weightsBuilder.addWeights(ByteString.copyFrom(Objects.requireNonNull(w.toByteArray())));
                    }
                    weightStream = weightsBuilder.build().toByteArray();

                    accumulated.clear();

                    for (Socket s : nodeConnection) {
                        new serverWorker(s).start();
                    }
                    time = time + System.currentTimeMillis() - start;
                }
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }

            try {
                serverConnection.close();
                System.out.println("server stoped");
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }

        private void waitSignal() {
            while (!serverOn) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.toString());
                }
            }
        }

    }

    private class serverWorker extends Thread {
        private Socket nodeSend;

        public serverWorker(Socket sendTarget) {
            nodeSend = sendTarget;
        }

        @Override
        public void run() {
            try {
                OutputStream writer = nodeSend.getOutputStream();
                writer.write(weightStream);
                nodeSend.shutdownOutput();
                nodeSend.close();
            } catch (IOException e) {
                throw new RuntimeException(e.toString());
            }
        }
    }
}
