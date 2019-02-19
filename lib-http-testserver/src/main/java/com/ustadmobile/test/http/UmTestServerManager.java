package com.ustadmobile.test.http;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class UmTestServerManager extends NanoHTTPD{

    private Hashtable<Integer, UmTestServer> testServers;

    public static final int DEFAULT_CONTROL_PORT = 8400;

    public static final int DEFAULT_FROM_PORT = 8500;

    public static final int DEFAULT_END_PORT = 8600;

    private int fromPort;

    private int endPort;

    private static final String CMD_NEWSERVER = "new";

    private static final String CMD_STOPSERVER = "stop";

    public static final String CMD_THROTTLE = "throttle";

    private File httpDir;

    private String bindAddress;

    public UmTestServerManager(int controlPort, int fromPort, int endPort, File httpDir,
                               String bindAddress) {
        super(controlPort);
        this.fromPort = fromPort;
        this.endPort = endPort;
        this.httpDir = httpDir;
        this.bindAddress = bindAddress;

        testServers = new Hashtable<>();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response response = null;

        Map<String, List<String>> parameters = session.getParameters();
        if(parameters.containsKey("cmd")) {
            String cmd = parameters.get("cmd").get(0);
            if(CMD_NEWSERVER.equals(cmd)) {
                //start a new server - return the port number in JSON
                int newPortNum = findNextPort();
                UmTestServer testServer = new UmTestServer(newPortNum, httpDir, bindAddress);
                testServers.put(newPortNum, testServer);
                try {
                    testServer.start();
                    System.out.println("UmTestServerManager: started new server on port " + newPortNum);
                }catch(IOException e) {
                    System.err.println("UmTestServerManager: Exception starting new server");
                    e.printStackTrace();
                    newPortNum = -1;
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("port", newPortNum);
                response =  NanoHTTPD.newFixedLengthResponse(
                        newPortNum > 0 ? NanoHTTPD.Response.Status.OK  : Response.Status.INTERNAL_ERROR,
                        "application/json", jsonResponse.toString());
            }else if(CMD_STOPSERVER.equals(cmd)) {
                int portNumber = Integer.parseInt(parameters.get("port").get(0));
                UmTestServer testServer = testServers.get(portNumber);
                if(testServer != null) {
                    testServer.stop();
                    response = NanoHTTPD.newFixedLengthResponse(Response.Status.NO_CONTENT,
                            "application/json", "");
                }else {
                    response = NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST,
                            "text/plain", "No such port in operation");
                }
            }else if(CMD_THROTTLE.equals(cmd)) {
                int portNumber = Integer.parseInt(parameters.get("port").get(0));
                UmTestServer testServer = testServers.get(portNumber);
                if(testServer != null) {
                    long bytesPerSecond = Long.parseLong(parameters.get("bytespersecond").get(0));
                    testServer.throttle(bytesPerSecond, 1, TimeUnit.SECONDS);
                    response = NanoHTTPD.newFixedLengthResponse(Response.Status.NO_CONTENT,
                            "application/json", "");
                }else {
                    response = NanoHTTPD.newFixedLengthResponse(Response.Status.BAD_REQUEST,
                            "text/plain", "No such port in operation");
                }
            }
        }


        if(response == null) {
            response = NanoHTTPD.newFixedLengthResponse("UmTestServer - see docs for more info");
        }

        response.addHeader("cache-control", "no-cache");

        return response;
    }

    private int findNextPort() {
        for(int i = fromPort; i < endPort; i++) {
            if(!testServers.containsKey(i))
                return i;
        }

        return -1;
    }

    public static void main(String[] args) {
        Options cmdOptions = new Options();

        Option controlPortOpt = new Option("c", "controlport", true, "Control port");
        cmdOptions.addOption(controlPortOpt);

        Option fromPortOpt = new Option("s", "startport", true, "Start port");
        cmdOptions.addOption(fromPortOpt);

        Option toPortOpt = new Option("e", "endport",true, "End port");
        cmdOptions.addOption(toPortOpt);

        Option httpDirOpt = new Option("d", "httpdir", true, "Http dir");
        cmdOptions.addOption(httpDirOpt);

        Option bindAddrOpt = new Option("b", "bindaddr", true, "Bind address");
        cmdOptions.addOption(bindAddrOpt);

        try {
            CommandLine cmd = new DefaultParser().parse(cmdOptions, args);
            int controlPort = cmd.hasOption("controlport") ?
                    Integer.parseInt(cmd.getOptionValue("controlport")) : DEFAULT_CONTROL_PORT;
            int fromPort = cmd.hasOption("startport") ?
                    Integer.parseInt(cmd.getOptionValue("startport")) : DEFAULT_FROM_PORT;
            int endPort = cmd.hasOption("endport") ?
                    Integer.parseInt(cmd.getOptionValue("endport")) : DEFAULT_END_PORT;
            File httpDir = cmd.hasOption("httpdir") ?
                    new File(cmd.getOptionValue("httpdir")) : new File("./");
            String bindAddress = cmd.hasOption("bindaddr") ?
                    cmd.getOptionValue("bindaddr") : "localhost";

            if(!httpDir.exists()){
                System.err.println("WARNING: http dir " + httpDir.getAbsolutePath() + " does not exist");
            }

            ServerRunner.executeInstance(new UmTestServerManager(controlPort, fromPort, endPort,
                    httpDir, bindAddress));
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println(e.getMessage());
            formatter.printHelp("UmTestServerManager", cmdOptions);
            System.exit(1);
        }
    }
}
