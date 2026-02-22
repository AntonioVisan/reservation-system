package org.acme.server;

import org.acme.entity.Slot;
import org.acme.entity.Reservation;
import org.acme.entity.User;
import org.acme.protocol.Command;
import org.acme.protocol.ServerResponse;
import org.acme.protocol.ListCommand;
import org.acme.protocol.ReserveCommand;
import org.acme.protocol.CancelCommand;
import org.acme.protocol.MyCommand;
import org.acme.protocol.ExitCommand;
import org.acme.services.SlotService;
import org.acme.services.ReservationService;
import org.acme.services.UserService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ReservationService reservationService;
    private final SlotService slotService;
    private final UserService userService;
    private final User user;

    public ClientHandler(Socket socket, ReservationService reservationService, SlotService slotService, UserService userService) {
        this.socket = socket;
        this.reservationService = reservationService;
        this.slotService = slotService;
        this.userService = userService;
        this.user = userService.createTemporaryUser();
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
                ServerResponse response = handleCommand(command);
                out.writeObject(response);
                out.flush();

                if (command instanceof ExitCommand)
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerResponse handleCommand(Command command) {
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
            ReservationService.ReservationResult result = reservationService.createReservation(user, slotId);
            return switch (result) {
                case SUCCESS -> new ServerResponse("Reservation created successfully.", true);
                case SLOT_NOT_FOUND -> new ServerResponse("Failed to create reservation because slot was not found.", false);
                case SLOT_ALREADY_RESERVED -> new ServerResponse("Failed to create reservation because slot was already reserved.", false);
            };
        } else if (command instanceof CancelCommand cancelCommand) {
            Long reservationId = cancelCommand.getReservationId();
            boolean success = reservationService.cancelReservation(reservationId, user);
            if (!success) {
                return new ServerResponse("Failed to cancel reservation. It may not exist or does not belong to you.", false);
            }
            return new ServerResponse("Reservation cancelled successfully.", true);
        } else if (command instanceof MyCommand) {
            List<Reservation> reservations = reservationService.getAllReservationsByUser(user);
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
