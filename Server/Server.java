/*kireevroi 2022*/
/*Implementing the server using simple sockets*/

package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;

public class Server {
    // The port that will be listened to
    private int port = 3443;
    // User lists
    private ArrayList<UserHand> users = new ArrayList<UserHand>();
    // The database to store password hashes
    DBManager db;

    // Constructor
    public Server() {
        // Initializing the database
        db = new DBManager();
        db.createTable();
        // Initializing a socket and serversocket as null
        Socket userSocket = null;
        ServerSocket serverSocket = null;
        try {
            // Making the ServerSocket
            serverSocket = new ServerSocket(port);
            System.out.println("Server Started!");
            // Infinite loop to accept new sockets
            while (true) {
                // Accepting new users
                userSocket = serverSocket.accept();
                // Creating a new user
                UserHand user = new UserHand(userSocket, this);
                // Adding the user to the list
                users.add(user);
                // Creating a new thread for the user
                new Thread(user).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                // Closing everything
                userSocket.close();
                System.out.println("Server Stopped");
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Send message to everybody
    public void channel(String msg) {
        for (UserHand user : users) {
            user.sendMsg(msg);
        }
    }
    // Remove user from list
    public void removeUser(UserHand user) {
        users.remove(user);
    }
}
