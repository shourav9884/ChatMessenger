/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;


/**
 *
 * @author Nati
 */

public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private Socket ownSocket;
    private int id;
    private ArrayList<Integer> friends;
    private String name;
    
    

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getOwnSocket() {
        return ownSocket;
    }

    public void setOwnSocket(Socket ownSocket) {
        this.ownSocket = ownSocket;
    }

    

    @Override
    public String toString() {
        return "Server.UserEntity[ id=" + id + " ]";
    }
    
    public void setFriends(ArrayList<Integer> friends)
    {
        this.friends=friends;
    }
    public ArrayList<Integer> getFriends() {
        return friends;
    }
    public boolean isFriend(int id)
    {
        for(int i=0;i<this.friends.size();i++)
        {
            if(this.friends.get(i)==id)
            {
                return true;
            }
            
        }
        return false;
    }
    
}
