package client;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class UserWindow extends JFrame {
    // host / port
    private String server_host = "localhost";
    private int server_port = 3443;
    // The socket to connect to
    private Socket userSocket;
    // IO messaging
    private Scanner inMessage;
    private PrintWriter outMessage;
    // Fields for UI
    private JTextField jMessage;
    private JTextField jName;
    private JTextArea jTextAreaMessage;
    // User name
    private String userName = "";
    // Constructor
    public UserWindow() {
        // Initiating selection loop until result us a success
        while(selectServer()!=1);
        // Setting the window parameters
        setBounds(600, 300, 600, 600);
        // Setting the title
        setTitle(this.server_host + ":" + server_port);
        // Setting default close habit
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // New text are
        jTextAreaMessage = new JTextArea();
        jTextAreaMessage.setEditable(false);
        jTextAreaMessage.setLineWrap(true);
        // Turning it into a scroll pane
        JScrollPane j = new JScrollPane(jTextAreaMessage);
        // adding it to the JFrame
        add(j, BorderLayout.CENTER);
        // Label for people in chat
        JLabel jNumberOfUsers = new JLabel("People in chat: ");
        // Adding it to the jFrame
        add(jNumberOfUsers, BorderLayout.NORTH);
        // Bottom panel
        JPanel jBottomPanel = new JPanel(new BorderLayout());
        // Adding to jFrame
        add(jBottomPanel, BorderLayout.SOUTH);
        // Adding button with Send
        JButton jSendMessage = new JButton("Send");
        // Adding to Bottom Panel
        jBottomPanel.add(jSendMessage, BorderLayout.EAST);
        // Adding text field
        jMessage = new JTextField("Enter your message: ");
        // Adding to Bottom Panel
        jBottomPanel.add(jMessage, BorderLayout.CENTER);
        // Adding listener to button for click
        jSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If the text field isn't empty - send
                if (!jMessage.getText().trim().isEmpty()) {
                    sendMsg();
                    jMessage.grabFocus();
                }
            }
        });
        // Adding listener for Enter button
        jMessage.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER){
                    if (!jMessage.getText().trim().isEmpty()) {
                        sendMsg();
                        jMessage.grabFocus();
                    }
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        // Clearing the text field when focusing
        jMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jMessage.setText("");
            }
        });
        // Opening another thread to interact with server
        new Thread(new Runnable() {
            // Overriding the run that happens after thread start
            @Override
            public void run() {
                try {
                    // login cycle until everything is good
                    while(login()!=1);
                    // Reading messages
                    jMessage.grabFocus();
                    while (true) {
                        if (inMessage.hasNext()) {
                            // Reading the message
                            String inMes = inMessage.nextLine();
                            // Sending message
                            jTextAreaMessage.append(inMes);
                            // Appending new line
                            jTextAreaMessage.append("\n");
                        }
                    }
                } catch (Exception e) {}
            }
        }).start();
        // Adding a listner for window closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                // Sending everyone a message that the user left
                outMessage.println(userName + " left chat!");
                // closing everything
                close();
            }
        });
        // Showing the frame and centering it
        setVisible(true);
        setLocationRelativeTo(null);
    }

    // Sending message to server
    public void sendMsg() {
        // Formatting the message
        String mesStr = this.userName + ": " + jMessage.getText();
        // Sending the message and clearing the text box
        outMessage.println(mesStr);
        outMessage.flush();
        jMessage.setText("");
    }
    // Selectint the server by host and ip
    private int selectServer() {
        // Return status
        int result = 0;
        // Fields for popup
        JTextField server_ip = new JTextField();
        JTextField server_p = new JTextField();
        Object[] server_message = {
            "Server IP:", server_ip,
            "Port:", server_p
        };
        // Showing popup
        int option = JOptionPane.showConfirmDialog(null, server_message,
                                                    "Server",
                                                    JOptionPane.OK_CANCEL_OPTION);
        // Checking if OK was pressed
        if (option == JOptionPane.OK_OPTION) {
            // Checking that something was actually entered
            // Only then changing the standard ip and port
            if (server_ip.getText().length() >= 1 &&
                server_p.getText().length() >= 1) {

                server_host = server_ip.getText();
                server_port = Integer.valueOf(server_p.getText());
            }
            try {
                // Connecting to the server
                userSocket = new Socket(server_host, server_port);
                inMessage = new Scanner(userSocket.getInputStream());
                outMessage = new PrintWriter(userSocket.getOutputStream());
                // Setting result as success
                result = 1;
              } catch (IOException e) {
                e.printStackTrace();
                // Error popup
                JOptionPane.showMessageDialog(null,
                                            "Unable to connect, try again");
              }
        } else {
            // If not OK was pressed, close everything
            System.exit(0);
        }
        // returning status code
        return result;
    }

    // Logging in to server
    private int login() {
        // Initializing fields
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        Object[] login_message = {
            "Username:", username,
            "Password:", password
        };
        // Showing popup with fields
        int option = JOptionPane.showConfirmDialog(null, login_message,
                                                    "Login",
                                                    JOptionPane.OK_CANCEL_OPTION);
        // Setting standard result to cancel = 2
        int loginResult = 0;
        // Checking if OK was pressed
        if (option == JOptionPane.OK_OPTION) {
            // Setting username and password
            String userName = username.getText();
            String passWord = password.getText();
            // Comparison strings for Server answer
            String okMes = "##" + userName + "##OK##";
            String failMes = "##" + userName + "##FAIL##";
            // Sending the data to server
            outMessage.println("##" + userName + "##" + String.valueOf(Hashing.hash(passWord)) + "##");
            outMessage.flush();
            // Waiting for the answer from the server
            while (true) {
                if (inMessage.hasNext()) {
                    // Reading the message
                    String inMes = inMessage.nextLine();
                    // If it is good
                    if (inMes.equals(okMes)) {
                        loginResult = 1;
                        break;
                    }
                    // If it's bad
                    if (inMes.equals(failMes)) {
                        loginResult = 0;
                        break;
                    }
                }
            }
            // Printing result depending on loginResult
            if (loginResult == 1) {
                System.out.println("Login successful");
                // Setting username
                this.userName = userName;
            } else if (loginResult == 0) {
                System.out.println("login failed");
            }
        } else {
            // If anything is pressed except OK - close.
            this.close();
        }
        // returning result
        return loginResult;
    }

    // Closing threads and sending exit signal
    public void close() {
        try {
            outMessage.println("##session##end##");
            outMessage.flush();
            outMessage.flush();
            outMessage.close();
            inMessage.close();
            userSocket.close();
        } catch (IOException e) {
        } finally {
            System.exit(0);
        }
    }
}
