import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FTP_Server {

    static final int PORT = 21;
    static String ROOT_DIR = "C:\\ftp\\";
    static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) {

        // Populate the list of valid users
        users.put("user1", "password1");
        users.put("user2", "password2");
        // ... add more users as needed

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
                // Send the welcome message
                outToClient.writeBytes("220 Service ready for new user.\r\n");
            
                String request = inFromClient.readLine();
                if (request.startsWith("OPTS")) {
                    String[] parts = request.split(" ");
                    if (parts.length >= 2 && parts[1].equals("UTF8")) {
                        outToClient.writeBytes("200 UTF8 set to on.\r\n");
                    } else {
                        outToClient.writeBytes("500 Syntax error, command unrecognized.\r\n");
                    }
                }

                // Get the username and password from the client
                String username = inFromClient.readLine().split(" ")[1];
                outToClient.writeBytes("331 Password required for " + username + ".\r\n");
                String password = inFromClient.readLine().split(" ")[1];
            
                // Check if the username and password are valid
                if (!users.containsKey(username) || !users.get(username).equals(password)) {
                    outToClient.writeBytes("530 Login incorrect.\r\n");
                    clientSocket.close();
                    continue;
                }
            
                outToClient.writeBytes("230 User " + username + " logged in.\r\n");
            
                // Create a new thread for the client
                Thread clientThread = new ClientHandler(clientSocket, outToClient, inFromClient, ROOT_DIR + username + "\\");
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            }
    }
}
                
