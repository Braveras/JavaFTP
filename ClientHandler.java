import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private DataOutputStream outToClient;
    private BufferedReader inFromClient;
    private String ROOT_DIR;


    public ClientHandler(Socket clientSocket, DataOutputStream outToClient, BufferedReader inFromClient, String ROOT_DIR) {
        this.clientSocket = clientSocket;
        this.outToClient = outToClient;
        this.inFromClient = inFromClient;
        this.ROOT_DIR = ROOT_DIR;
    }

    @Override
    public void run() {
        try {
        // Handle the client's commands
        while (true) {
            String command = inFromClient.readLine();
            System.out.println(command);
            if (command == null) {
                break;
            }
            String[] tokens = command.split(" ");
            String cmd = tokens[0];
            if (cmd.equals("QUIT")) {
                outToClient.writeBytes("221 Service closing control connection.\r\n");
                outToClient.flush();
                clientSocket.close();
                break;
            } else if (cmd.equals("RETR")) {
                String fileName = tokens[1];
                File file = new File(ROOT_DIR + fileName);
                if (!file.exists()) {
                    outToClient.writeBytes("550 File not found.\r\n");
                    outToClient.flush();
                } else {
                    outToClient.writeBytes("150 File status okay; about to open data connection.\r\n");
                    outToClient.flush();
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fileInputStream.read(buffer);
                    outToClient.writeBytes("226 Closing data connection.\r\n");
                    outToClient.flush();
                    fileInputStream.close();
                }
            }
            else if (cmd.equals("STOR")) {
                String fileName = tokens[1];
                File file = new File(ROOT_DIR + fileName);
                if (file.exists()) {
                    outToClient.writeBytes("550 File already exists.\r\n");
                    outToClient.flush();
                } else {
                    outToClient.writeBytes("150 File status okay; about to open data connection.\r\n");
                    outToClient.flush();
                    //Receive the file from the client
                    //Handle the incoming data
                    file.createNewFile();
                    outToClient.writeBytes("226 Closing data connection.\r\n");
                    outToClient.flush();
                }
            }
            else if (cmd.equals("LIST") || cmd.equals("NLST")) {
                File folder = new File(ROOT_DIR);
                File[] listOfFiles = folder.listFiles();
                outToClient.writeBytes("150 File status okay; about to open data connection.\r\n");
                outToClient.flush();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        outToClient.writeBytes(file.getName() + "\r\n");
                        outToClient.flush();
                    }
                }
                outToClient.writeBytes("226 Closing data connection.\r\n");
                outToClient.flush();
            } 
            else if (cmd.equals("DELE")) {
                String fileName = tokens[1];
                File file = new File(ROOT_DIR + fileName);
                if (file.delete()) {
                outToClient.writeBytes("250 Requested file action okay, completed.\r\n");
                outToClient.flush();
                } else {
                outToClient.writeBytes("550 Requested action not taken. File not found.\r\n");
                outToClient.flush();
                }
            } 
            else if (cmd.equals("CWD")) {
                String dirName = tokens[1];
                File folder = new File(ROOT_DIR + dirName);
                if (folder.isDirectory()) {
                ROOT_DIR = folder.getAbsolutePath() + "\\";
                outToClient.writeBytes("250 Requested file action okay, completed.\r\n");
                outToClient.flush();
                } else {
                outToClient.writeBytes("550 Requested action not taken. Folder not found.\r\n");
                outToClient.flush();
                }
            } else if (cmd.equals("CDUP")) {
                File folder = new File(ROOT_DIR);
                ROOT_DIR = folder.getParent() + "\\";
                outToClient.writeBytes("200 Command okay.\r\n");
                outToClient.flush();
            } else if (command.startsWith("XMKD ")) {
                String dirName = command.substring(5);
                File newDir = new File(ROOT_DIR + File.separator + dirName);
                if (newDir.mkdir()) {
                    outToClient.writeBytes("257 " + dirName + " created.\r\n");
                } else {
                    outToClient.writeBytes("550 Failed to create directory.\r\n");
                }
            } else {
                outToClient.writeBytes("502 Command" + cmd + " not implemented.\r\n");
                outToClient.flush();
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}