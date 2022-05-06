/*kireevroi 2022*/
/*Multithreaded user managment system*/
package server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.IOException;

// Implementing runnable which makes it threadable
public class UserHand implements Runnable {
    // The server
    private Server server;
    // The stream for input and output
    private PrintWriter outMessage;
    private Scanner inMessage;
    // Host and port of the server
    private String host = "localhost";
    private int port = 3443;
    // user socket
    private Socket userSocket = null;
    // number of users in chat
    private static int user_count = 0;

    // Constructor, accepts the server and socket
    public UserHand(Socket socket, Server server) {
        try {
            // Adding user count
            user_count++;
            // Initializing everything
            this.server = server;
            this.userSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Overriding run method which starts when the thread is created;
    @Override
    public void run() {
        try {
            // Looping to recieve login
            while (true) {
                if (inMessage.hasNext()) {
                    // Reading the message
                    String loginMessage = inMessage.nextLine();
                    // If the message is an error message closing and breaking
                    if (loginMessage.equals("##0##0##")) {
                        this.close();
                        return;
                    }
                    // Splitting the string
                    String[] loginMessageSplit = loginMessage.split("##");
                    // Creating a user in the database if not exists
                    server.db.createUser(loginMessageSplit[1], Integer.valueOf(loginMessageSplit[2]));
                    // Checking that the passed hash is right
                    if (server.db.getUser(loginMessageSplit[1]) == Integer.valueOf(loginMessageSplit[2])) {
                        sendMsg("##" + loginMessageSplit[1] + "##OK##"); // ##admin##OK## - means hashes are good
                        break;
                    } else {
                        sendMsg("##" + loginMessageSplit[1] + "##FAIL##"); // ##admin##FAIL## - means hashes are a no-no
                    }
                }
            }
            // Sending a message to all, that a user entered chat
            while (true) {
                server.channel("New user entered chat");
                break;
            }
            // Checking if message was recieved
            while (true) {
                if (inMessage.hasNext()) {
                    String userMessage = inMessage.nextLine();
                    /* If ##session##end## is recieved, loop ends
                    and the thread is closed*/
                    if (userMessage.equals("##session##end##")) {
                        break;
                    }
                    // Sending the Message to terminal
                    System.out.println(userMessage);
                    // Sending the message to everyone
                    server.channel(userMessage);
                }
                // Resting for 100 ms
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            // Closing the thread
            this.close();
        }
    }
    // Sending a message
    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Closing the connection
    public void close() {
        server.removeUser(this);
    }
}
