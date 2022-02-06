import java.net.*;
import java.io.*;

public class Server {
	private ServerSocket Ssocket;

	public void init (int port) {
		try {
			Ssocket = new ServerSocket(port);
			while (true)
				new ClientHandler(Ssocket.accept()).run();
		} catch(IOException e) {
			System.out.println(e.getMessage());
		} finally {
			end();
		}
	}

	public void end() {
		try {
			Ssocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static class ClientHandler implements Runnable {

        	private Socket client;
        	private PrintWriter output;
        	private BufferedReader input;

        public ClientHandler(Socket socket) {
            this.client = socket;
        }

        public void run() {
            try {
                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    if (".".equals(inputLine)) {
                        output.println("bye");
                        break;
                    }
                    output.println(inputLine);
                }

                input.close();
                output.close();
                client.close();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
	System.out.println("Started");
        Server server = new Server();
        server.init(5555);
	System.out.println("Ended");
    }
}


