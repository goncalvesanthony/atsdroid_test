package com.ats.atsdroid;

import com.ats.atsdroid.server.AtsWebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Objects;

import androidx.test.platform.app.InstrumentationRegistry;

public class AtsRunnerUsb extends AtsRunner {

    public AtsWebSocketServer tcpServer;

    /* A REFACTO : NECESSAIRE POUR LE MODE UDP USB */
    public int udpPort = DEFAULT_PORT;

    @Override
    public void stop() {
        try {
            tcpServer.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.stop();
    }

    @Override
    public void testMain() {
        super.testMain();

        try {
            /* A REFACTO : NECESSAIRE POUR LE MODE UDP USB */
            udpPort = Integer.parseInt(Objects.requireNonNull(InstrumentationRegistry.getArguments().getString("udpPort")));

            // fetch available port
            ServerSocket serverSocket = new ServerSocket(0);
            int availablePort = serverSocket.getLocalPort();
            serverSocket.close();

            tcpServer = new AtsWebSocketServer(new InetSocketAddress(availablePort), automation);
            tcpServer.start();

            while (running) { }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
