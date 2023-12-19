package doodle_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.*;

import database.JDBCManager;
import model.GuestUser;
import model.Room;
import model.User;

public class ServerThread extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private Gson gson = new Gson();
	
	private User user = null;
	private Room room;
	private Server server;
	private JDBCManager manager;

	public ServerThread(Socket s, Server server, JDBCManager manager) {
		this.server = server;
		this.manager = manager;
		try {
			pw = new PrintWriter(s.getOutputStream());
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}

	public void sendMessage(String message) {
		pw.println(message);
		pw.flush();
	}

	public void run() {
		try {
			while (true) {
				String line = br.readLine();
				parseJson(line);
			}
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
		}
	}

	public User getUser() {
		return user;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	private void parseJson(String message) {
		if (message == null)
			return;
		JsonObject jo = JsonParser.parseString(message).getAsJsonObject();

		// For testing
		Gson gsonpretty = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gsonpretty.toJson(jo));

		String request = jo.get("request").getAsString();
		JsonObject content = jo.get("content").getAsJsonObject();
		switch (request) {
			case "SIGNUP":
				processSignup(content);
				break;
			case "SIGNIN":
				processSignin(content);
				break;
			case "HOSTROOM":
				processHostRoom(content);
				break;
			case "JOINROOM":
				processJoinRoom(content);
				break;
			case "LEAVEROOM":
				processLeaveRoom(content);
				break;
			case "DISMISSROOM":
				processDismissRoom(content);
				break;
			case "GAMESTART":
				processGameStart(content);
				break;
			case "MESSAGE":
				processMessage(content);
				break;
			case "PLAYERSCORE":
				processPlayerScore(content);
				break;
			case "PLAYERDEAD":
				processPlayerDead(content);
				break;
			case "CLIENTCLOSED":
				processClientClosed(content);
				break;
			case "SIGNOUT":
				processSignout(content);
				break;
			case "GUESTSIGNIN":
				processGuestSignIn(content);
				break;
			default:
				break;
		}
	}

	private void processSignup(JsonObject content) {
		String username = content.get("username").getAsString();
		String password = content.get("password").getAsString();
		String email = content.get("email").getAsString();
		String nickname = content.get("nickname").getAsString();

		boolean state = manager.registerUser(username, password, email, nickname);

		JsonObject response = new JsonObject();
		response.addProperty("response", "SIGNUP");

		JsonObject responseContent = new JsonObject();
		if (state) {
			response.addProperty("code", 0);
			responseContent.addProperty("message", "Sign up successful");
		} else {
			response.addProperty("code", 1);
			responseContent.addProperty("message", "Username or email already exists");
		}
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}

	private void processSignin(JsonObject content) {
		String username = content.get("username").getAsString();
		String password = content.get("password").getAsString();

		boolean credentialsCheck = manager.checkCredentials(username, password);
		boolean signedInCheck = server.signedInCheck(username);

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "SIGNIN");
		if (!credentialsCheck) {
			response.addProperty("code", 1);
			responseContent.addProperty("message", "Wrong username or password");
		} else if (!signedInCheck) {
			response.addProperty("code", 1);
			responseContent.addProperty("message", "User signed in elsewhere");
		} else {
			user = manager.getUserByUsername(username);
			response.addProperty("code", 0);
			responseContent.addProperty("message", "Sign in successful");
			responseContent.addProperty("nickname", user.getNickname());
			responseContent.addProperty("personal_best", user.getPersonalBest());
		}
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}

	private void processHostRoom(JsonObject content) {
		server.hostRoom(this);

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "HOSTROOM");
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Host successful");
		responseContent.addProperty("room_code", room.getRoomCode());
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}

	private void processJoinRoom(JsonObject content) {
		String roomCode = content.get("room_code").getAsString();

		int state = server.joinRoom(this, roomCode);
		System.out.println(state);

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "JOINROOM");
		if (state == 1) {
			response.addProperty("code", 1);
			responseContent.addProperty("message", "Room does not exist");
		} else if (state == 2) {
			response.addProperty("code", 2);
			responseContent.addProperty("message", "Game has already started");
		} else if (state == 3) {
			response.addProperty("code", 3);
			responseContent.addProperty("message", "Room full");
		} else {
			response.addProperty("code", 0);
			responseContent.addProperty("message", "Join in successful");
			responseContent.add("players", room.getPlayerNames());
		}
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));

		if (state != 0)
			return;

		response = new JsonObject();
		responseContent = new JsonObject();

		response.addProperty("response", "NEWPLAYER");
		response.addProperty("code", 0);
		responseContent.addProperty("player", user.getNickname());
		responseContent.addProperty("message", user.getNickname() + " has joined the room");
		response.add("content", responseContent);

		room.broadcastInRoom(gson.toJson(response), this);
	}

	private void processLeaveRoom(JsonObject content) {
		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "LEAVEROOM");
		response.addProperty("code", 0);
		responseContent.addProperty("player", user.getNickname());
		responseContent.addProperty("message", user.getNickname() + " has left the room");
		response.add("content", responseContent);

		if (room != null) {
			room.broadcastInRoom(gson.toJson(response), this);
			room.removeThread(this);
		}
		room = null;
	}

	private void processDismissRoom(JsonObject content) {
		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "DISMISSROOM");
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Room dismissed");
		response.add("content", responseContent);

		if (room != null) {
			room.broadcastInRoom(gson.toJson(response), null);
			server.dismissRoom(room);
		}
	}

	private void processGameStart(JsonObject content) {
		room.setGameState(true);
		room.setAlivePlayerCount(room.playerCount());

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "GAMESTART");
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Game starts");
		response.add("content", responseContent);

		room.broadcastInRoom(gson.toJson(response), null);
	}

	private void processMessage(JsonObject content) {
		String message = content.get("message").getAsString();

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "MESSAGE");
		response.addProperty("code", 0);
		responseContent.addProperty("nickname", user.getNickname());
		responseContent.addProperty("message", message);
		response.add("content", responseContent);

		room.broadcastInRoom(gson.toJson(response), null);
	}

	private void processPlayerScore(JsonObject content) {
		int score = content.get("score").getAsInt();
		user.setCurrentScore(score);

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "PLAYERSCORE");
		response.addProperty("code", 0);
		responseContent.add("players", room.getPlayerScores());
		response.add("content", responseContent);

		room.broadcastInRoom(gson.toJson(response), null);
	}

	private void processPlayerDead(JsonObject content) {
		user.setPersonalBest(user.getCurrentScore());
		if (user instanceof User) {
			manager.setScore(user.getUID(), user.getPersonalBest());
		}

		room.playerDead();
	}

	private void processClientClosed(JsonObject content) {
		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "CLIENTCLOSED");
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Client closed");
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}

	private void processSignout(JsonObject content) {
		server.signOut(user.getUsername());
		user = null;

		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "SIGNOUT");
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Signed out");
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}

	private void processGuestSignIn(JsonObject content) {
		JsonObject response = new JsonObject();
		JsonObject responseContent = new JsonObject();

		response.addProperty("response", "SIGNIN");
		user = new GuestUser();
		response.addProperty("code", 0);
		responseContent.addProperty("message", "Sign in successful");
		responseContent.addProperty("nickname", user.getNickname());
		responseContent.addProperty("personal_best", user.getPersonalBest());
		response.add("content", responseContent);

		sendMessage(gson.toJson(response));
	}
}