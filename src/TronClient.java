import java.io.*;
import java.net.*;

public class TronClient {
    public static void main(String[] args) throws Exception {
        try {
            Socket tSocket = new Socket(InetAddress.getByName("68.5.19.136"), 4401);
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
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to ");
            e.printStackTrace();
            System.exit(1);
        }
    }
}