package database;
import java.sql.*;
import java.util.ArrayList;
import model.User;

public class JDBCManager {
	private Connection conn;
	private PreparedStatement prst;
	private ResultSet rs;
	
	public JDBCManager(Connection conn) {
		this.conn = conn;
		rs = null;
		prst = null;
	}
	
	public User getUserByUsername(String username) {
		User user = null;
	
		try {
			String query = "SELECT * FROM UserInfo WHERE Username = ?";
			prst = conn.prepareStatement(query);
			prst.setString(1, username);
			rs = prst.executeQuery();
	
			if (rs.next()) {
				int UID = rs.getInt("UID");
				String password = rs.getString("Password");
				String email = rs.getString("Email");
				String nickname = rs.getString("Nickname");
				String theme = rs.getString("Theme");
				int score = rs.getInt("Score");
	
				user = new User(UID, username, password, email, nickname, theme, score);
			}
	
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return user;
	}
	
	public User getUserByEmail(String email) {
		User user = null;
	
		try {
			String query = "SELECT * FROM UserInfo WHERE Email = ?";
			prst = conn.prepareStatement(query);
			prst.setString(1, email);
			rs = prst.executeQuery();
	
			if (rs.next()) {
				int UID = rs.getInt("UID");
				String username = rs.getString("Username");
				String password = rs.getString("Password");
				String nickname = rs.getString("Nickname");
				String theme = rs.getString("Theme");
				int score = rs.getInt("Score");
	
				user = new User(UID, username, password, email, nickname, theme, score);
			}
	
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return user;
	}
	
	public boolean registerUser(String username, String password, String email, String nickname) {
		if (getUserByUsername(username) != null || getUserByEmail(email) != null) return false;
		try {
			String sql = "INSERT INTO UserInfo(Username,Password,Email,Nickname,Theme,Score) VALUES(?,?,?,?,?,?)";
			prst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			prst.setString(1, username);
			prst.setString(2, password);
			prst.setString(3, email);
			prst.setString(4, nickname);
			prst.setString(5, "Default"); // @TODO Put the name of the actual default theme
			prst.setInt(6, 0); // Score defaults to 0
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
			return false;
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
				return false;
			}
		}
		return true;
	}
	
	public boolean checkCredentials(String username, String password) {
		boolean validCredentials = false;
		try {
			String query = "SELECT username, password FROM UserInfo WHERE Username = ? AND Password = ?";
			prst = conn.prepareStatement(query);
			prst.setString(1, username);
			prst.setString(2, password);
			rs = prst.executeQuery();
	
			// If the query returns a result, the credentials are valid
			if (rs.next()) {
				validCredentials = true;
			}
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
			validCredentials = false;
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
				validCredentials = false;
			}
		}
		return validCredentials;
	}
	
	public ArrayList<User> getLeaderboard() {
		ArrayList<User> userList = new ArrayList<User>(10);
		try {
			String query = "SELECT * FROM UserInfo ORDER BY Score DESC LIMIT 10";
			prst = conn.prepareStatement(query);
			rs = prst.executeQuery();
			while (rs.next()) {
				int UID = rs.getInt("UID");
				String username = rs.getString("Username");
				String password = rs.getString("Password");
				String email = rs.getString("Email");
				String nickname = rs.getString("Nickname");
				String theme = rs.getString("Theme");
				int score = rs.getInt("Score");
	
				User curr_user = new User(UID, username, password, email, nickname, theme, score);
				userList.add(curr_user);
			}
	
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return userList;
	}
	
	public void setPassword(int UID, String password) {
		try {
			String sql = "UPDATE UserInfo SET Password = ? WHERE UID = ?";
			prst = conn.prepareStatement(sql);
			prst.setString(1, password);
			prst.setInt(2, UID);
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}

		
	}
	
	public String getEmail(int UID) {
		String email = null;
		try {
			String query = "SELECT Email FROM UserInfo WHERE UID = ?";
			prst = conn.prepareStatement(query);
			prst.setInt(1, UID);
			rs = prst.executeQuery();
			if (rs.next()) {
				email = rs.getString("Email");
			}
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return email;
	}
	
	public void setEmail(int UID, String email) {
		try {
			String sql = "UPDATE UserInfo SET Email = ? WHERE UID = ?";
			prst = conn.prepareStatement(sql);
			prst.setString(1, email);
			prst.setInt(2, UID);
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}

	public String getNickname(int UID) {
		String nickname = null;
		try {
			String query = "SELECT Nickname FROM UserInfo WHERE UID = ?";
			prst = conn.prepareStatement(query);
			prst.setInt(1, UID);
			rs = prst.executeQuery();
			if (rs.next()) {
				nickname = rs.getString("Nickname");
			}
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return nickname;
	}
	
	public void setNickname(int UID, String nickname) {
		try {
			String sql = "UPDATE UserInfo SET Nickname = ? WHERE UID = ?";
			prst = conn.prepareStatement(sql);
			prst.setString(1, nickname);
			prst.setInt(2, UID);
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}
	
	public String getTheme(int UID) {
		String theme = null;
		try {
			String query = "SELECT Theme FROM UserInfo WHERE UID = ?";
			prst = conn.prepareStatement(query);
			prst.setInt(1, UID);
			rs = prst.executeQuery();
			if (rs.next()) {
				theme = rs.getString("Theme");
			}
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return theme;
	}
	
	public void setTheme(int UID, String theme) {
		try {
			String sql = "UPDATE UserInfo SET Theme = ? WHERE UID = ?";
			prst = conn.prepareStatement(sql);
			prst.setString(1, theme);
			prst.setInt(2, UID);
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}
	
	public int getScore(int UID) {
		int score = 0;
		try {
			String query = "SELECT Score FROM UserInfo WHERE UID = ?";
			prst = conn.prepareStatement(query);
			prst.setInt(1, UID);
			rs = prst.executeQuery();
			if (rs.next()) {
				score = rs.getInt("Score");
			}
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
		return score;
	}
	
	public void setScore(int UID, int score) {
		try {
			String sql = "UPDATE UserInfo SET Score = ? WHERE UID = ?";
			prst = conn.prepareStatement(sql);
			prst.setInt(1, score);
			prst.setInt(2, UID);
			prst.executeUpdate();
		} catch (SQLException sqle) {
			System.out.println(sqle.getMessage());
		} finally {
			try {
				if (prst != null) {
					prst.close();
				}
			} catch (SQLException sqle) {
				System.out.println(sqle.getMessage());
			}
		}
	}

}
