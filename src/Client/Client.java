package Client;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

class Client extends Frame implements Runnable
{
    Socket soc;    
   
    String sendTo;
    String LoginName;
    Thread t=null;
    static DataOutputStream dout;
    DataInputStream din;
    Window window;
   
    Vector availableUsers;
    ArrayList<Pair<Integer,String>>  friends;
    Client(String LoginName,String chatwith) throws Exception
    {
       
        soc=new Socket("127.0.0.1",5217);

        din=new DataInputStream(soc.getInputStream()); 
        dout=new DataOutputStream(soc.getOutputStream());        
        availableUsers=new Vector();
        friends=new ArrayList<Pair<Integer, String>>() {};
        window=new Window();
        t=new Thread(this);
        
       

    }
   
//    public boolean action(Event e,Object o)
//    {
//        if(e.arg.equals("Send"))
//        {
//            try
//            {
//                dout.writeUTF(sendTo + " "  + "DATA" + " " + tf.getText().toString());            
//                ta.append("\n" + LoginName + " Says:" + tf.getText().toString());    
//                tf.setText("");
//            }
//            catch(Exception ex)
//            {
//            }    
//        }
//        else if(e.arg.equals("Close"))
//        {
//            try
//            {
//                dout.writeUTF(LoginName + " LOGOUT");
//                System.exit(1);
//            }
//            catch(Exception ex)
//            {
//            }
//            
//        }
//        
//        return super.action(e,o);
//    }
//    public void 
    public static void sendLogin(String user,String pass) 
    {
        try {
            String dataToSend="{\"action\":\"login\",\"data\":{\"user\":\""+user+"\",\"pass\":\""+pass+"\"}}";
            dout.writeUTF(dataToSend);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void sendLogout(String uname)
    {
        try {
            String dataToSend="{\"action\":\"logout\",\"name\":\""+uname+"\"}";
            dout.writeUTF(dataToSend);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void sendMessage(String recievers,String message,String sender)
    {
        try {
            StringTokenizer tok=new StringTokenizer(message);
            String first=tok.nextToken();
            String dataToSend="";
            if(first.equals("Request"))
            {
                String to=tok.nextToken();
                dataToSend="{\"action\":\"request\",\"sender\":\""+sender+"\",\"to\":\""+to+"\"}";
            }
            else if(first.equals("Accept"))
            {
                String to=tok.nextToken();
                dataToSend="{\"action\":\"Accept\",\"sender\":\""+sender+"\",\"to\":\""+to+"\"}";
            }
            
            else
            {
                if(first.equals("PM_F"))
                {
                    recievers="all_friends";
                    message="";
                    while(tok.hasMoreElements())
                    {
                        message+=tok.nextElement();
                        message+=" ";
                    }
                    
                    
                }
                else if(first.equals("PM"))
                {
                    String r=tok.nextToken();
                    recievers=r;
                    message="";
                    while(tok.hasMoreElements())
                    {
                        message+=tok.nextElement();
                        message+=" ";
                    }
                }
                dataToSend="{\"action\":\"send_message\",\"reciever\":\""+recievers+"\",\"sender\":\""+sender+"\",\"message\":\""+message+"\"}";
            }
            dout.writeUTF(dataToSend);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void updateFriend(int id,int isOnline)
    {
        friends.get(id).setFirst(isOnline);
    }
    public boolean isOnline(String name)
    {
        for(int i=0;i<availableUsers.size();i++)
        {
            if(availableUsers.get(i).equals(name))
            {
                return true;
            }
        }
        return false;
    }
    public static void main(String args[]) throws Exception
    {
        Client Client1=new Client("","");
        Client1.window.show();
        Client1.t.start();
    }   
    private void updateLogOut(String name)
    {
        if(availableUsers.size()>0)
        {
            for(int i=0;i<availableUsers.size();i++)
            {
                if(availableUsers.get(i).equals(name))
                {
                    availableUsers.remove(i);
                    break;
                }
            }
        }
        if(friends.size()>0)
        {
            for(int i=0;i<friends.size();i++)
            {
                if(friends.get(i).getSecond().equals(name))
                {
                    updateFriend(i, 0);
                    break;
                }
            }
        }
    }
    public void run()
    {        
        while(true)
        {
            try
            {
                String res=din.readUTF();
                System.out.println(res);
                JSONObject response=new JSONObject(res);
               
                if(response.getString("response_type").equals("login"))
                {
                    if(response.getString("response").equals("failed"))
                    {
                        window.changeStatus("Failed");
                    }
                    else if(response.getString("response").equals("success"))
                    {
                       if(response.has("available"))
                       {
                            JSONArray userList=response.getJSONArray("available");
                            for(int i=0;i<userList.length();i++)
                            {
                                availableUsers.add(userList.getString(i));
                            }
                       
                       }
                       if(response.has("friends"))
                       {
                            JSONArray friendList=response.getJSONArray("friends");
                            int availables=availableUsers.size();
                            int friendNum=friendList.length();
                            int l=0;
                            
                            for(int i=0;i<friendList.length();i++)
                            {
                                
                                Pair<Integer,String> friendObj=new Pair<Integer, String>(0, friendList.getString(i));
                                
                                friends.add(friendObj);
                                System.out.println(" ");
                                if(this.isOnline(friendObj.getSecond()))
                                {
                                    this.updateFriend(i, 1);
                                }
                                
                            }
                       }
                       if(response.has("messages"))
                       {
                            JSONArray offlineM=response.getJSONArray("messages");
                            for(int i=0;i<offlineM.length();i++)
                            {
                                window.updateTextArea(offlineM.getString(i));
                            }
                           
                       }
                        LoginName=response.getString("name");
                        window.populateFriendList(friends);
                        window.populateAvailableList(availableUsers);
                        window.hideLogin();
                        
                        window.updateTitle(response.getString("name"));
                    }
                    else if(response.getString("response").equals("new"))
                    {
                        availableUsers.add(response.getString("name"));
                        
                        for(int i=0;i<friends.size();i++)
                        {
                            if(friends.get(i).getSecond().equals(response.getString("name")))
                            {
                                this.updateFriend(i, 1);
                            }
                        }
                        window.populateFriendList(friends);
                        window.populateAvailableList(availableUsers);
                    }
                }
                else if(response.getString("response_type").equals("send_message"))
                {
                    String sender=response.getString("name");
                    String msg=response.getString("response");
                     window.updateTextArea(""+sender+">> "+response.getString("response"));
                }
                else if(response.getString("response_type").equals("friend_request"))
                {
                    String sender=response.getString("sender");
                    window.updateTextArea(""+sender+"  Sent you a friend request");
                }
                else if(response.getString("response_type").equals("accept_friend"))
                {
                    String by=response.getString("by");
                    window.updateTextArea(""+by+"  accepted your friend request");
                    Pair<Integer,String> newFriend=new Pair<Integer, String>(1,by);
                    friends.add(newFriend);
                    window.populateFriendList(friends);
                    
                }
                else if(response.getString("response_type").equals("new_friend"))
                {
                    String newf=response.getString("response");
                    window.updateTextArea(""+newf+"  is your new friend");
                    Pair<Integer,String> newFriend=new Pair<Integer, String>(1,newf);
                    friends.add(newFriend);
                    window.populateFriendList(friends);
                    
                }
                else if(response.getString("response_type").equals("log_out"))
                {
                     String logoutf=response.getString("name");
                     window.updateTextArea(""+logoutf+"  is logged out");
                     updateLogOut(logoutf);
                     window.populateAvailableList(availableUsers);
                     window.populateFriendList(friends);
                }
                else if(response.getString("response_type").equals("error"))
                {
                    String errorr=response.getString("response");
                    window.updateTextArea(""+errorr+"  ");
                }
                
               
               
                
                
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
