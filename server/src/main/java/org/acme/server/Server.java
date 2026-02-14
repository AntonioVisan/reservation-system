package org.acme.server;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.services.SlotService;
import org.acme.services.ReservationService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class Server {
    private static final int MAX_THREADS = 5;
    private ExecutorService executor;

    @Inject
    private SlotService slotService;
    @Inject
    private ReservationService reservationService;

    void onStart(@Observes StartupEvent ev) {
        executor = Executors.newFixedThreadPool(MAX_THREADS);

        executor.submit( () -> {
            startSocketServer();
        });
    }

    private void startSocketServer() {
        try(ServerSocket socket = new ServerSocket(9090)) {
            while(true) {
                Socket client = socket.accept();
                executor.submit(new ClientHandler(client, reservationService, slotService));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
