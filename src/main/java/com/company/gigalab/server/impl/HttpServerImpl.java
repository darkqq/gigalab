package com.company.gigalab.server.impl;

import com.company.gigalab.enumeration.ContentType;
import com.company.gigalab.enumeration.HttpCodes;
import com.company.gigalab.enumeration.RequestType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Optional;
import java.util.StringTokenizer;

public class HttpServerImpl implements Runnable {
    private static final Logger log = LogManager.getLogger(HttpServerImpl.class);

    private static final String ROOT = "/files";
    private static final String INDEX = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_ALLOWED = "501.html";

    private final Socket socket;


    public HttpServerImpl(Socket socket) {
        this.socket = socket;
        try {
            this.socket.setKeepAlive(true);
            this.socket.setSoTimeout(3000);
        } catch (SocketException e) {
            System.exit(0);
        }

    }

    @Override
    public void run() {
        String fileRequested = null;
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));


            String input = reader.readLine();
            log.log(Level.INFO, "Input is:\n" + input);
            StringTokenizer parse;
            try {
                parse = new StringTokenizer(input);
            } catch (NullPointerException e) {
                return;
            }

            String method = parse.nextToken().toUpperCase();
            log.info("Request method => {} ", method);
            fileRequested = parse.nextToken().toLowerCase();
            Optional<RequestType> requestType = RequestType.of(method);

            if (!requestType.isPresent()) {
                methodNotSupported(method);
            } else {
                switch (requestType.get()) {
                    case GET:
                        processGet(fileRequested);
                        break;
                    case POST:
                        processPost();
                    case OPTIONS:
                        processOptions();
                }
                log.info("File {} returned", fileRequested);
            }
        } catch (FileNotFoundException notFoundException) {
            try {
                log.warn("File {} not found, load {}", fileRequested, FILE_NOT_FOUND);
                fileNotFound(fileRequested);
            } catch (IOException ioe) {
                log.warn("Error with file not found exception => {}", ioe.getMessage());
            }
        } catch (IOException ioe) {
            log.error("Server error => ", ioe);
        } finally {
            try {
                reader.close();
                socket.close();
            } catch (Exception e) {
                log.error("Error closing stream => {}", e.getMessage());
            }
            log.info("Connection closed");
        }
    }

    protected void processGet(String fileRequested) throws IOException {
        log.info("GET request was accepted");
        if (fileRequested.endsWith("/")) {
            fileRequested += INDEX;
        }
        if (fileRequested.contains("..")) {
            throw new IOException();
        }
        InputStream inputStream = findFile(fileRequested, true);
        ContentType content = ContentType.findByFileName(fileRequested);
        byte[] data = content.getReader().read(inputStream);
        createResponse(HttpCodes.OK, content, data.length, data);
        log.info("File {} with content-type {} returned", fileRequested, content.getText());
    }

    protected void processPost() throws IOException {
        log.info("POST!");
        createResponse(HttpCodes.CREATED, ContentType.PLAIN, 0, new byte[]{});
    }

    protected void processOptions() throws IOException {
        log.log(Level.INFO, "OPTIONS!");
        InputStream inputStream = findFile("options.txt", false);
        byte[] data = ContentType.PLAIN.getReader().read(inputStream);
        createResponse(HttpCodes.OK, ContentType.PLAIN, data.length, data);
    }

    protected void methodNotSupported(String method) throws IOException {
        log.log(Level.WARN, "Unknown method: " + method);
        InputStream inputStream = findFile(METHOD_NOT_ALLOWED, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HttpCodes.NOT_IMPLEMENTED, ContentType.HTML, data.length, data);
    }

    protected void fileNotFound(String fileRequested) throws IOException {
        InputStream inputStream = findFile(FILE_NOT_FOUND, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HttpCodes.NOT_FOUND, ContentType.HTML, data.length, data);
        log.log(Level.WARN, "File " + fileRequested + " not found");

    }

    protected InputStream findFile(String fileName, boolean isInternal) throws FileNotFoundException {
        if (!isInternal) {
            fileName = "/" + fileName;
        } else {
            fileName = ROOT + fileName;
        }
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        log.info("requested file => {}", this.getClass().getResource(fileName));
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return inputStream;
    }

    protected void createResponse(HttpCodes code, ContentType content, int fileLength, byte[] fileData) throws IOException {

        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

        writer.println("HTTP/1.1 " + code.getCode() + " " + code.getDescription());
        writer.println("Server: Java HTTP Server");
        writer.println("Date: " + new Date());
        writer.println("Content-type: " + content.getText());
        writer.println("Content-length: " + fileLength);
        writer.println("Access-Control-Allow-Origin: " + "localhost");
        writer.println("Access-Control-Allow-Methods: " + "GET, POST, OPTIONS");
        writer.println();
        writer.flush();
        outputStream.write(fileData, 0, fileLength);
        outputStream.flush();
        log.info("Content-type: {}, file size {}", content.getExtension(), fileLength);
        try {
            Thread.sleep(fileLength / 100);
        } catch (InterruptedException ignored) {
        }
        log.info("Creating header of response with code: {}", code.getCode());
    }

    public Socket getSocket() {
        return socket;
    }
}