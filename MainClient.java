package client;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) throws IOException {
        File file = new File("license.txt");
        //If the file license.txt doesn't exist. It will ask for the key, if verified with the server, it will save the license on license.txt so it doesn't need to put on the next run
        String license;
        if (!file.exists()) {
            System.out.println("Insert the license key.");
            Scanner reader = new Scanner(System.in);
            license = reader.nextLine();
        }else {
            //License.txt detected, it will go through the process above.
            System.out.println("License.txt detected.");
            FileReader file2 = new FileReader("license.txt");
            BufferedReader buffer = new BufferedReader(file2);
            license = buffer.readLine();
        }
        System.out.println("Verifying with licensing server.");
        JSONObject licensestruct = new JSONObject();
        licensestruct.put("key", license);
        String hwid = getHWID();
        licensestruct.put("hwid", hwid);
        //Make the connection to the ServerSocket, sending the JSONObject with the license key and hwid to verify
        try {
            Socket socket = new Socket("localhost", 5001);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream din=new DataInputStream(socket.getInputStream());
            out.writeUTF(licensestruct.toString());
            out.flush();
            String line = "";
            while (!line.equals("over")) {
                line = din.readUTF();
                if (line.equals("Verified")) {
                    line = "over";
                }else {
                    //License didn't exist or the HWID isn't the same, it terminates the software
                    System.out.println("Server couldn't validate your license. Closing the software.");
                    System.exit(0);
                }
            }
            //Terminating connections, streams and flushing the Command Line
            socket.close();
            din.close();
            out.close();
            System.out.println("You have been verified. Initializing the software.");
            Thread.sleep(1500);
            System.out.flush();
            PrintWriter out2 = new PrintWriter("license.txt");
            out2.print(license);
            out2.close();
            verified();
        }catch (Exception e) {
            //This exception evokes normally when the server isn't online.
            e.printStackTrace();
            System.out.println("Couldn't connect to the Server to verify your license. Please verify your connection or contact the developer.");
        }
    }

    public static void verified() {
        System.out.println("Hi! I'm verified");
    }

    //Obtain some enviroments of the computer and encrypting them to create what's called "HWID"
    public static String getHWID() {
        try{
            String toEncrypt =  System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuilder hexString = new StringBuilder();

            byte[] byteData = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

}