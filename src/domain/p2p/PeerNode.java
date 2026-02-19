package domain.p2p;

import java.io.*;
import java.net.*;
import java.util.*;
//PeerNode (servidor + cliente + consola)
public class PeerNode {

    private final String peerId;
    private final int listenPort;
    private final TrackerClient tracker;

    public PeerNode(String peerId, int listenPort, TrackerClient tracker) {
        this.peerId = peerId;
        this.listenPort = listenPort;
        this.tracker = tracker;
    }

    public void start() throws IOException {
        tracker.register(peerId, listenPort);
        System.out.println("[PEER " + peerId + "] Registrado en el tracker.");
        
        new Thread(this::listenLoop).start();
        consoleLoop();
    }

    private void listenLoop() {
        try (ServerSocket ss = new ServerSocket(listenPort)) {
            System.out.println("[PEER " + peerId + "] Escuchando en puerto " + listenPort);
            while (true) {
                Socket s = ss.accept();
                new Thread(() -> handleIncoming(s)).start();
            }
        } catch (IOException e) {
            System.out.println("[ERROR LISTENER] " + e.getMessage());
        }
    }

    private void handleIncoming(Socket s) {
        try (s; BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            String line = in.readLine();
            if (line != null && line.startsWith("MSG ")) {
                System.out.println("\n[MENSAJE RECIBIDO] " + line.substring(4));
                System.out.print("> ");
            }
        } catch (IOException e) {
            System.out.println("[ERROR ENTRADA] " + e.getMessage());
        }
    }

    private void consoleLoop() throws IOException {
        System.out.println("Comandos: peers | send <peerId> <mensaje> | exit");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("exit")) break;
            
            if (line.equalsIgnoreCase("peers")) {
                tracker.listPeers().forEach((id, hp) -> System.out.println(id + " -> " + hp.host + ":" + hp.port));
            } else if (line.startsWith("send ")) {
                String[] parts = line.split("\\s+", 3);
                if (parts.length < 3) continue;
                
                var target = tracker.listPeers().get(parts[1]);
                if (target != null) {
                    sendMessage(target.host, target.port, parts[2]);
                } else {
                    System.out.println("Peer no encontrado.");
                }
            }
        }
        System.exit(0);
    }

    private void sendMessage(String host, int port, String msg) {
        try (Socket s = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            out.write("MSG " + peerId + " " + msg);
            out.newLine();
            out.flush();
            System.out.println("[ENVIADO]");
        } catch (IOException e) {
            System.out.println("[ERROR ENVÍO] " + e.getMessage());
        }
    }
}