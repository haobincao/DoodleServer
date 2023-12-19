package model;

import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import doodle_server.ServerThread;

public class Room {
    private Vector<ServerThread> serverThreads = new Vector<>();
    private String roomCode;
    private boolean gameState = false;

    private int alivePlayerCount = 0;

    public Room(String roomCode) {
        this.roomCode = roomCode;
    }

    public void broadcastInRoom(String s, ServerThread st) {
        for (ServerThread serverThread : serverThreads) {
            if (serverThread != st)
                serverThread.sendMessage(s);
        }
    }

    public void addThread(ServerThread st) {
        serverThreads.add(st);
    }

    public void removeThread(ServerThread st) {
        serverThreads.remove(st);
    }

    public void dismissRoom() {
        for (ServerThread st : serverThreads) {
            st.setRoom(null);
        }
    }

    public int playerCount() {
        return serverThreads.size();
    }

    public JsonArray getPlayerNames() {
        JsonArray ja = new JsonArray();
        for (ServerThread st : serverThreads) {
            ja.add(st.getUser().getNickname());
        }
        return ja;
    }

    public JsonArray getPlayerScores() {
        JsonArray ja = new JsonArray();
        for (ServerThread st : serverThreads) {
            JsonObject jo = new JsonObject();
            jo.addProperty("player", st.getUser().getNickname());
            jo.addProperty("score", st.getUser().getCurrentScore());
            ja.add(jo);
        }
        return ja;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public boolean getGameState() {
        return gameState;
    }

    public void setGameState(boolean gameState) {
        this.gameState = gameState;
    }

    public int getAlivePlayerCount() {
        return alivePlayerCount;
    }

    public void setAlivePlayerCount(int apc) {
        alivePlayerCount = apc;
    }

    public void playerDead() {
        alivePlayerCount -= 1;

        if (alivePlayerCount == 0) {
            String winner = "";
            int highest = 0;
            for (ServerThread st : serverThreads) {
                if (st.getUser().getCurrentScore() > highest) {
                    winner = st.getUser().getNickname();
                    highest = st.getUser().getCurrentScore();
                }
            }
            for (ServerThread st : serverThreads) {
                JsonObject response = new JsonObject();
                JsonObject responseContent = new JsonObject();

                response.addProperty("response", "GAMEOVER");
                response.addProperty("code", 0);
                responseContent.addProperty("message", "Gameover");
                responseContent.addProperty("personal_best", st.getUser().getPersonalBest());
                responseContent.addProperty("winner", winner);
                response.add("content", responseContent);

                st.sendMessage(new Gson().toJson(response));

                st.setRoom(null);
            }
        }
    }
}