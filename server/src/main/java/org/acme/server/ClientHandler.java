package org.acme.server;

import org.acme.entity.Slot;
import org.acme.entity.Reservation;
import org.acme.protocol.*;
import org.acme.services.SlotService;
import org.acme.services.ReservationService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ReservationService reservationService;
    private final SlotService slotService;
    private final String clientId;

    public ClientHandler(Socket socket, ReservationService reservationService, SlotService slotService) {
        this.socket = socket;
        this.reservationService = reservationService;
        this.slotService = slotService;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        try (Socket s = socket){
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            while (true) {
                System.out.println("Waiting for command...");
                Command command = (Command) in.readObject();
                System.out.println("Received command: " + command.getClass());
                Response response = handleCommand(command);
                out.writeObject(response);
                out.flush();

                if (command instanceof ExitCommand)
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response handleCommand(Command command) {
        if (command instanceof ListCommand) {
            List<Slot> slotList = slotService.getAvailableSlots();
            if (slotList.isEmpty())
                return new ServerResponse("No slots available", false);
            StringBuilder builder = new StringBuilder();
            builder.append("Available slots:\n");
            for (Slot slot : slotList) {
                builder.append("- ").append(slot).append("\n");
            }
            return new ServerResponse(builder.toString(), true);
        } else if (command instanceof ReserveCommand reserveCommand) {
            Long slotId = reserveCommand.getSlotId();
            if (slotId == null) {
                return new ServerResponse("Failed to create reservation because slot id is invalid.", false);
            }
            ReservationService.ReservationResult result = reservationService.createReservation(clientId, slotId);
            return switch (result) {
                case SUCCESS -> new ServerResponse("Reservation created successfully.", true);
                case SLOT_NOT_FOUND -> new ServerResponse("Failed to create reservation because slot was not found.", false);
                case SLOT_ALREADY_RESERVED -> new ServerResponse("Failed to create reservation because slot was already reserved.", false);
            };
        } else if (command instanceof CancelCommand cancelCommand) {
            Long reservationId = cancelCommand.getReservationId();
            boolean success = reservationService.cancelReservation(reservationId, clientId);
            if (!success) {
                return new ServerResponse("Failed to cancel reservation. It may not exist or does not belong to you.", false);
            }
            return new ServerResponse("Reservation cancelled successfully.", true);
        } else if (command instanceof MyCommand) {
            List<Reservation> reservations = reservationService.getAllReservationsByClient(clientId);
            if (reservations.isEmpty()) {
                return new ServerResponse("You have no reservations.", false);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("Your reservations:\n");
            for (Reservation reservation : reservations) {
                builder.append("- ").append(reservation).append("\n");
            }
            return new ServerResponse(builder.toString(), true);
        } else if (command instanceof ExitCommand) {
            return new ServerResponse("Goodbye!", true);
        } else {
            return new ServerResponse("Unknown command.", false);
        }
    }
}
