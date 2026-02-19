package domain.p2p;
//Main del Peer (PeerMain)
public class PeerMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: PeerMain <ID> <PuertoLocal> <IPTracker>");
            return;
        }
        String id = args[0];
        int port = Integer.parseInt(args[1]);
        String trackerIp = args[2];
        
        TrackerClient tc = new TrackerClient(trackerIp, 6000);
        new PeerNode(id, port, tc).start();
    }
}