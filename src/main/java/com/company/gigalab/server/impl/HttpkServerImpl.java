package com.company.gigalab.server.impl;

import com.company.gigalab.enumeration.ContentType;
import com.company.gigalab.enumeration.HttpCodes;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class HttpkServerImpl extends HttpServerImpl {
    private final String redirectUrl;

    public HttpkServerImpl(Socket socket, String redirectUrl) {
        super(socket);
        this.redirectUrl = redirectUrl;
    }

    @Override
    protected void processGet(String fileRequested) throws IOException {
        InputStream inputStream = findFile(fileRequested, true);
        ContentType content = ContentType.findByFileName(fileRequested);
        byte[] data = content.getReader().read(inputStream);
        createResponse(HttpCodes.REDIRECT, content, data.length, data);
    }

    @Override
    protected void createResponse(HttpCodes code, ContentType content, int fileLength, byte[] fileData) throws IOException {
        PrintWriter writer = new PrintWriter(getSocket().getOutputStream());
        BufferedOutputStream outputStream = new BufferedOutputStream(getSocket().getOutputStream());

        writer.println("HTTP/1.1 " + code.getCode() + " " + code.getDescription());
        writer.println("Server: Java HTTP Server");
        writer.println("Date: " + new Date());
        writer.println("Content-type: " + content.getText());
        writer.println("Content-length: " + fileLength);
        writer.println("Access-Control-Allow-Origin: " + "localhost");
        writer.println("Access-Control-Allow-Methods: " + "GET, POST, OPTIONS");
        writer.println("Location: " + redirectUrl);
        writer.println();
        writer.flush();
        outputStream.write(fileData, 0, fileLength);
        outputStream.flush();
        try {
            Thread.sleep(fileLength / 100);
        } catch (InterruptedException ignored) {
        }
    }
}
