package server;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class MainServer {

    public static HashMap<String, String> licenses = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("License server Starting!");
        //Load license key and HWID associated with it
        try {
            try (BufferedReader br = new BufferedReader(new FileReader("licenses.txt"))) {
                String line = br.readLine();

                while (line != null) {
                    JSONObject obj = new JSONObject(line);
                    licenses.put(obj.getString("key"), obj.getString("hwid"));
                    line = br.readLine();
                }
            }
        }catch (Exception e){
            //Couldn't find or parse the licenses.txt file.
            System.out.println("Couldn't load licenses.txt , proceeding without licenses on the system");
        }
        //Starting new thread for the Socket Server to accept new requests.
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //Start the functions to accept commands on the Command-Line
        listeningCommands();
    }

    public static void listeningCommands() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String next = scanner.nextLine();
        if (next.equals("create")){
            //Generates new license, saves on the hashmap with no HWID associated
            System.out.println("Creating a new License.");
            String key = getAlphaNumericString(25);
            licenses.put(key, "");
            System.out.println("Generated License: " + key);
        }else if (next.equals("list")){
            //Lists all current keys on the system
            Set<String> keys = licenses.keySet();
            for ( String key : keys ) {
                System.out.println( key );
            }
        }
        //finally. saves the system to licenses.txt and restarts the function
        saveFile();
        listeningCommands();
    }

    //Function to save all keys and hwid associated to licenses.txt
    public static void saveFile() throws IOException {
        File myObj = new File("licenses.txt");
        myObj.delete();
        FileWriter myWriter = new FileWriter("licenses.txt");
        Set<String> keys = licenses.keySet();
        for ( String key : keys ) {
            JSONObject obj2 = new JSONObject();
            obj2.put("key", key);
            obj2.put("hwid", licenses.get(key));
            myWriter.write(obj2.toString() + "\n");
        }
        myWriter.close();
    }

    public static void startServer() throws IOException {
        //Creating a new ServerSocket
        ServerSocket server = new ServerSocket(5001);
        Socket socket;
        while (true) {
            //Once it accepts a client, starts a new thread to not overload the thread of the server. and starts listening to the client until it says "disconnect" on the socket message
            socket = server.accept();
            Socket finalSocket = socket;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Client accepted. Checking a license");
                    DataInputStream in = null;
                    DataOutputStream out = null;
                    try {
                        in = new DataInputStream(new BufferedInputStream(finalSocket.getInputStream()));
                        out = new DataOutputStream(finalSocket.getOutputStream());
                    }catch (Exception e) {

                    }
                    String line = "";
                    while(!line.equals("disconnect")) {
                        try {
                            String stuff = in.readUTF();
                            System.out.println(stuff);
                            JSONObject obj = new JSONObject(stuff);
                            System.out.println("Key: " + obj.getString("key"));
                            //Verifies if the license is in the system, and if the HWID provided by the client is the same that's registered, if it doesn't have one, it will associate it.
                            if (licenses.containsKey(obj.getString("key"))) {
                                if (licenses.get(obj.getString("key")).equals("")) {
                                    out.writeUTF("Verified");
                                    out.flush();
                                    licenses.put(obj.getString("key"), obj.getString("hwid"));
                                }else {
                                    if (licenses.get(obj.getString("key")).equals(obj.getString("hwid"))) {
                                        out.writeUTF("Verified");
                                        out.flush();
                                    }else {
                                        out.writeUTF("Unverified");
                                        out.flush();
                                    }
                                }
                                line = "disconnect";
                            }else {
                                out.writeUTF("Unverified");
                                out.flush();
                                line = "disconnect";
                            }
                        }catch (Exception e) {
                            //If any exception is called, it will terminate the connection
                            try {
                                finalSocket.close();
                            } catch (IOException ioException) {

                            }
                        }
                        //Finally, it closes the connection and the thread is finalized.
                        try {
                            finalSocket.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }).start();
        }
    }

    static String getAlphaNumericString(int n)
    {
        String patterntogenerate = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            //Generate a random integer for the index on the patterntogenerate string index
            int index
                    = (int)(patterntogenerate.length()
                    * Math.random());

            //Adds the character to the string builder
            sb.append(patterntogenerate
                    .charAt(index));
        }

        return sb.toString();
    }



}
