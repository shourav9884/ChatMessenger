/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nati
 */
public class DbHandler {
    Database database;
    public DbHandler()
    {
        database=new Database();
        
    }
    public UserEntity checkLogin(String user,String pass)
    {
        UserEntity userObj=null;
        try {
            database.connect();
            ResultSet result;
            result=database.select("user_info", "*", "uname = '"+user+"' AND password='"+pass+"'",null);
            if(database.getRowCount(result)==1)
            {
                userObj=new UserEntity();
                result.next();
                userObj.setId(result.getInt("user_id"));
                userObj.setName(result.getString("uname"));
                userObj.setFriends(this.getFriends(userObj.getId()));
                database.close();
                return userObj;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public ArrayList<Integer> getFriends(int id)
    {
        ArrayList<Integer> result=new ArrayList<Integer>();
        try {
            database.connect();
            ResultSet resultSet;
            resultSet=database.select("friends", "friend_id", "user_id = "+id,null);
            if(database.getRowCount(resultSet)>0)
            {
                result=new ArrayList<Integer>();
                 while (resultSet.next()) {
                     Integer fid=resultSet.getInt("friend_id");
                     result.add(fid);
                 }
                database.close();
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public ArrayList<String> getOfflineMessages(int id)
    {
        ArrayList<String> messages=new ArrayList<String>();
        try {
            
            database.connect();
            ResultSet result=database.select("off_message", "*", "to_id="+id,"time DESC");
            while(result.next())
            {
                try {
                    String from=this.getUserName(result.getInt("from_id"));
                    String m="From: "+from+" At: "+result.getTimestamp("time").toString()+"\\n"+result.getString("message");
                    
                    messages.add(m);
                } catch (SQLException ex) {
                    Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            database.close();
           
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
         return  messages;
    }
    public boolean insertRequset(int uid,int fid)
    {
        database.connect();
        boolean result=database.insert("request", " NULL,"+uid+", "+fid+",0");
        database.close();
        return result;
        
    }
    public boolean insertOfflineMessage(int from , int to, String message)
    {
        database.connect();
        boolean result=database.insert("off_message", " NULL,"+from+", "+to+",'"+message+"',NULL");
        database.close();
        return result;
    }
    public boolean deleteOfflineMessage(int id)        
    {
        database.connect();
        boolean result=database.delete("off_message", "to_id="+id);
        database.close();
        return result;
    }
    public boolean isAlreadyRequested(int uid,int fid)
    {
        try {
            database.connect();
            ResultSet result;
            result=database.select("request", "*", "user_id = "+uid+" AND fr_id="+fid+"",null);
            if(database.getRowCount(result)>0)
            {
                database.close();
                return true;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public boolean addFriend(int uid,int fid)
    {
        database.connect();
        boolean result=database.insert("friends", " NULL,"+uid+", "+fid+"");
        database.insert("friends", " NULL,"+fid+", "+uid+"");
        database.update("request", "accepted=1", "(user_id="+uid+" AND fr_id="+fid+") OR (user_id="+fid+" AND fr_id="+uid+")");
        database.close();
        return result;
    }
    public String getUserName(int id)
    {
        try {
            database.connect();
            ResultSet resultSet;
            resultSet=database.select("user_info", "uname", "user_id="+id,null);
            if(database.getRowCount(resultSet)>0)
            {
                try {
                    resultSet.next();
                    String name=resultSet.getString("uname");
                    database.close();
                    return name;
                } catch (SQLException ex) {
                    Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public Integer getUserId(String name)
    {
        try {
            database.connect();
            ResultSet resultSet;
            resultSet=database.select("user_info", "user_id", "uname='"+name+"'",null);
            if(database.getRowCount(resultSet)>0)
            {
                try {
                    resultSet.next();
                    int id=resultSet.getInt("user_id");
                    database.close();
                    return id;
                } catch (SQLException ex) {
                    Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DbHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}
