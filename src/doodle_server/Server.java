package doodle_server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

import database.JDBCManager;
import model.Room;

public class Server {
	private Set<String> aliveUsers = new HashSet<>();
	private Vector<ServerThread> serverThreads;
	private Map<String, Room> roomMap = new HashMap<>();
	private String DBusername;
	private String DBpassword;

	private JDBCManager manager = null;

	public Server(int port) {
		String filePath = "config.env";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            DBusername = br.readLine().split("=")[1];
            DBpassword = br.readLine().split("=")[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
		Connection conn = null;
		try {
			conn = DriverManager
					.getConnection("jdbc:mysql://localhost/DoodleDB?user="+DBusername+"&password="+DBpassword);
			manager = new JDBCManager(conn);

			System.out.println("Binding to port " + port);
			serverThreads = new Vector<ServerThread>();
			try (ServerSocket ss = new ServerSocket(port)) {
				System.out.println("Bound to port " + port);
				while (true) {
					Socket s = ss.accept();
					System.out.println("Connection from: " + s.getInetAddress());
					ServerThread st = new ServerThread(s, this, manager);
					serverThreads.add(st);
				}
			}
		} catch (IOException ioe) {
			System.out.println("ioe in Serveer constructor: " + ioe.getMessage());
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}

	public void hostRoom(ServerThread st) {
		String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String roomCode = "";
		do {
			roomCode = "";
			Random random = new Random();
			for (int i = 0; i < 6; i++) {
				int index = random.nextInt(alphaNumericString.length());
				char randomChar = alphaNumericString.charAt(index);
				roomCode += randomChar;
			}
		} while (roomMap.containsKey(roomCode));
		Room room = new Room(roomCode);
		st.setRoom(room);
		room.addThread(st);
		roomMap.put(roomCode, room);
	}

	public int joinRoom(ServerThread st, String roomCode) {
		Room room = roomMap.get(roomCode);
		if (room == null)
			return 1;
		if (room.getGameState() == true)
			return 2;
		if (room.playerCount() == 5)
			return 3;
		st.setRoom(room);
		room.addThread(st);
		return 0;
	}

	public void dismissRoom(Room room) {
		room.dismissRoom();
		roomMap.remove(room.getRoomCode());
	}

	public boolean signedInCheck(String username) {
		return aliveUsers.add(username);
	}

	public void signOut(String username) {
		aliveUsers.remove(username);
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Server server = new Server(8567);
	}
}
