package org.example;
import org.acme.protocol.*;
import java.util.Scanner;

public class Main {
    private static Command parseCommand(String input) {
        String[] parts = input.trim().split("\\s+");
        String cmd = parts[0].toUpperCase();
        return switch (cmd) {
            case "LIST" -> {
                if(parts.length != 1) {
                    throw new IllegalArgumentException("Usage: LIST");
                }
                yield new ListCommand();
            }
            case "MY" -> {
                if(parts.length != 1) {
                    throw new IllegalArgumentException("Usage: MY");
                }
                yield new MyCommand();
            }
            case "EXIT" -> {
                if(parts.length != 1) {
                    throw new IllegalArgumentException("Usage: EXIT");
                }
                yield new ExitCommand();
            }
            case "RESERVE" -> {
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Usage: RESERVE <slotId>");
                }
                try {
                    long slotId = Long.parseLong(parts[1]);
                    yield new ReserveCommand(slotId);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("slotId must be a natural number.");
                }
            }
            case "CANCEL" -> {
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Usage: CANCEL <reservationId>");
                }
                try {
                    Long reservationId = Long.parseLong(parts[1]);
                    yield new CancelCommand(reservationId);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("reservationId must be a natural number.");
                }
            }
            default -> throw new IllegalArgumentException("Unknown command: " + cmd);
        };
    }
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        try(Client client = new Client()){
            client.connect();
            while (true) {
                try {
                    System.out.println("Commands:");
                    System.out.println("1. LIST");
                    System.out.println("2. RESERVE <slotId>");
                    System.out.println("3. CANCEL <reservationId>");
                    System.out.println("4. MY");
                    System.out.println("5. EXIT");
                    String line = in.nextLine();
                    Command command = parseCommand(line);
                    ServerResponse response = client.send(command);
                    if (!response.success()) {
                        System.out.println("Error: " + response.response());
                    } else {
                        System.out.println(response.response());
                    }
                    if(command instanceof ExitCommand) {
                        break;
                    }
                }catch(IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("Could not connect to server:");
            e.printStackTrace();
        }
    }
}