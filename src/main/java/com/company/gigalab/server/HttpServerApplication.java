package com.company.gigalab.server;

import com.company.gigalab.Main;
import com.company.gigalab.enumeration.StartupParamsEnum;
import com.company.gigalab.server.impl.HttpServerImpl;
import com.company.gigalab.server.impl.HttpkServerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


public class HttpServerApplication {
    private static final Logger log = LogManager.getLogger(Main.class);
    private static int port = 8080;

    public static void run(String[] args) {
        List<String> params = List.of(args);
        if (params.contains("-h") || params.contains("--help")) {
            log.info( StartupParamsEnum.HELP.getValue());
            return;
        }
        if(params.contains("-p") && params.contains("--port")){
            log.warn("Duplicated port parameter. Use -h or --help to get help");
            return;
        }
        if (params.contains("-p") ) {
            port = Integer.parseInt(params.get(params.indexOf("-p") + 1));
            log.info("Port is set to {}", port);
        }
        if (params.contains("--port") ) {
            port = Integer.parseInt(params.get(params.indexOf("--port") + 1));
            log.info("Port is set to {}", port);
        }
        if(params.contains("--redirect")){
            log.warn("??");
                HttpkServerImpl server = null;
                try {
                    server = new HttpkServerImpl(new ServerSocket(port).accept(), params.get(params.indexOf("--redirect") + 1));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                log.info("Connection opened");
                new Thread(server).start();
                System.exit(-1337);
        }

        try {
            ServerSocket serverConnect = new ServerSocket(port);
            log.info("Socket started, listening to port => {}", port);

            while (true) {
                HttpServerImpl server = new HttpServerImpl(serverConnect.accept());

                log.info("Connection opened");

                Thread thread = new Thread(server);
                thread.start();
            }

        } catch (IOException e) {
            log.error("Server Connection error : {}", e.getMessage());
        }
    }
}
