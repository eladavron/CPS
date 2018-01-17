package server;

import entity.Session;
import ocsf.server.ConnectionToClient;

import java.util.HashMap;

public class SessionManager {
    /**
     * Sessions
     */
    static HashMap<ConnectionToClient, Session> _sessionsMap = new HashMap<ConnectionToClient, Session>();

    /**
     * Get a session from the map by SID.
     * @param SID Session ID to search
     * @return Session if found, null if not.
     */
    public static Session getSession(int SID)
    {
        for (Session session : _sessionsMap.values())
        {
            if (session.getSid() == SID)
            {
                return session;
            }
        }
        return null;
    }

    public static HashMap<ConnectionToClient, Session> getSessionsMap() {
        return _sessionsMap;
    }

    public static Session getSession(ConnectionToClient clientConnection){
        return _sessionsMap.get(clientConnection);
    }

    public static void saveSession(ConnectionToClient clientConnection, Session session){
        _sessionsMap.put(clientConnection, session);
        System.out.println("Session added: " + session);
    }

    public static void dropSession(ConnectionToClient clientConnection)
    {
        _sessionsMap.remove(clientConnection);
    }

    public static void dropSession(Session session)
    {
        for (ConnectionToClient connection : _sessionsMap.keySet())
        {
            if (_sessionsMap.get(connection).equals(session))
                dropSession(connection);
        }
    }

    public static boolean doesSessionExist(ConnectionToClient clientConnection){ return _sessionsMap.containsKey(clientConnection); }
}
