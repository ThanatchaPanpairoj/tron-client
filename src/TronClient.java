import java.io.*;
import java.net.*;

public class TronClient {
    public static void main(String[] args) throws Exception {
        try {
            Socket tSocket = new Socket(InetAddress.getByName("68.96.95.22"), 4401);
            PrintWriter out = new PrintWriter(tSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(tSocket.getInputStream()));
            String fromServer;
            String fromUser;
            Renderer r = new Renderer();

            while ((fromServer = in.readLine()) != null) {
                r.updatePositions(fromServer);
                fromUser = r.getPosition();
                out.println(fromUser);
                if(fromUser.equals("b"))
                    System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Serve Offline");
            System.exit(0);
        }
    }
}