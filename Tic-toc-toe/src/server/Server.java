package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

import Exception.AddPlayerException;
import model.*;

public class Server {
	static final HashMap<String, ServerSession> serverSessions = new HashMap<>();

	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(6789);
			System.out.println("Server is ready ...");
			System.out.println("Waiting for connect request...");
			while (true) {
				Socket client = server.accept();
				System.out.println("New Client:" + client.getInetAddress().getHostAddress() + "/" + client.getPort());
				
				new Thread(new ServerHandler(client, serverSessions)).start();
				/**/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

class ServerHandler implements Runnable{
	Socket client;
	ObjectOutputStream toClient;
	ObjectInputStream fromClient;
	HashMap<String, ServerSession> serverSessions;
	
	
	public ServerHandler(Socket client, HashMap<String, ServerSession> serverSessions) {
		this.client = client;
		this.serverSessions = serverSessions;
		try {
			this.fromClient = new ObjectInputStream(client.getInputStream());
			this.toClient = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				Object[] clientInfo = (Object[]) fromClient.readObject();
				Message message = (Message) clientInfo[0];
				String name = (String) clientInfo[1];
				System.out.println("Message from " + client.toString() + " - name: " + name + ":"
				        + message.toString());
				if (message == message.CREATE_SESSION) {
					
					SessionInfo sessionInfo = (SessionInfo) fromClient.readObject();
					ServerSession serverSession = new ServerSession(sessionInfo.getNumberOfRows(), sessionInfo.getNumberOfPlayersAllowed());
					System.out.println("Session created: "+ serverSession.getSessionInfo().toString());
					String sessionId = serverSession.getId();
					System.out.println("Id: " + sessionId);
//					toClient.writeObject(sessionId);
					
					try {
						serverSession.addPlayer(new Player(name, fromClient, toClient));
						serverSessions.put(sessionId, serverSession);
						new Thread(serverSession).start();
						break;
					} catch (AddPlayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				
				} else {// state = Message.JOIN_SESSION
					String sessionId = (String)fromClient.readObject();
					System.out.println("Message: ID:  " + sessionId);
					if (serverSessions.containsKey(sessionId)) {
						try {
							serverSessions.get(sessionId).addPlayer(new Player(name, fromClient, toClient));
//							toClient.writeObject(Message.JOIN_SESSION_SUCCESS);
							break;
						} catch (AddPlayerException e) {
							if(e.getMessage() == Message.SESSION_IS_FULL.toString())
								toClient.writeObject(Message.SESSION_IS_FULL);
							else
								toClient.writeObject(Message.NICKNAME_EXISTS);
						}
						
					} else {
						toClient.writeObject(Message.UNVALID_ID);
					}
				}
			}
		}catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	private void showSessionID() {
		System.out.println("Number of Sessions : " + serverSessions.size());
		for(String id : serverSessions.keySet()) {
			System.out.println(id);
		}
		
	}
}
