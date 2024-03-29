import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class detection{

    public static LinkedList<Attributes> attributesList = Attributes.attributesList;
    public static LinkedList<Queues> queues = Queues.queues;
    public static boolean responsePacket = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        //String[] commands = {"bash", "-c", "tcpdump", "-i", "any", "port", "8080", "and", "'(tcp-syn|tcp-ack)!=0'"};
        //String cmd = "/home/dnllzc/Desktop/DOS/src/tcpdump.sh";

        // Define the command to be executed
        String cmd2 = "tcpdump -i any port 8080 and '(tcp-syn|tcp-ack)!=0'";

        //ProcessBuilder pb = new ProcessBuilder(cmd2);
        //Process p = pb.start();

        // Process to execute the command
        ProcessBuilder pb2 = new ProcessBuilder();
        pb2.command("bash", "-c", cmd2);
        Process p = pb2.start();

        // Read the output of the command
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            String[] attributes = line.split(" ");
            for (int i = 0; i < attributes.length; i++) {
                // Check if the packet is a response packet
                if (attributes[i].equals("IP")) {
                    responsePacket = attributes[i + 1].contains("http-alt");
                }
            }

            if (responsePacket) {
                System.out.println("Response packet detected");
            }
            else {
                // If it wasn't a response packet, get the attributes
                getAttributes(attributes);
                //printAttributes(attributesList.getLast());)
                PacketChecker.checkAll();
            }
        }
        p.waitFor(1, TimeUnit.MILLISECONDS);

    }

    // Parse the attributes from the command output
    private static void getAttributes(String[] attributes) {
        // Define the attributes
        String time = attributes[0];
        String source = "";
        String flags = "";
        int size = 0;
        int length = 0;
        int recordId = Attributes.numOfRecords;

        // Get the attributes
        label:
        for (int i = 0; i < attributes.length; i++) {
            switch (attributes[i]) {
                case "IP":
                    source = attributes[i + 1];
                    break;
                case "Flags":
                    flags = attributes[i + 1].substring(0, attributes[i + 1].length() - 1);
                    break;
                case "win":
                    size = Integer.parseInt(attributes[i + 1].substring(0, attributes[i + 1].length() - 1));
                    break;
                case "length":
                    if (attributes[i + 1].matches("[0-9]+")) {
                        length = Integer.parseInt(attributes[i + 1]);
                    } else {
                        length = Integer.parseInt(attributes[i + 1].substring(0, attributes[i + 1].length() - 1));
                    }
                    Attributes.numOfRecords++;
                    break label;
            }
        }

        // Add the attributes to the list
        attributesList.add(new Attributes(recordId, time, source, flags, size, length));
    }

    public static void printAttributes(Attributes a) {
        String format = "Record ID:" + a.recordId + "\nTime: " + a.time + "\nSource: " + a.source + "\nFlags: " + a.flags + "\nSize: " + a.size + "\nLength: " + a.length;
        System.out.println(format);
    }

}