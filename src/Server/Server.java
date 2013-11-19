/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.*;
import java.util.*;
import java.io.*;
import java.security.Timestamp;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;
 
class chatServer 
{
    static ArrayList<UserEntity> users;
    
    chatServer() throws Exception
    {
        ServerSocket soc=new ServerSocket(5217); 
        users=new ArrayList<Server.UserEntity>();

        while(true)
        {    
            Socket CSoc=soc.accept();        
            AcceptClient obClient=new AcceptClient(CSoc);
        }
    }
    public static void main(String args[]) throws Exception
    {
        
        chatServer ob=new chatServer();
    }

class AcceptClient extends Thread
{
    Socket ClientSocket;
    DataInputStream din;
    DataOutputStream dout;
    DbHandler dbHandler;
    int loggedIn;
    
    AcceptClient (Socket CSoc) throws Exception
    {
        ClientSocket=CSoc;

        din=new DataInputStream(ClientSocket.getInputStream());
        dout=new DataOutputStream(ClientSocket.getOutputStream());
        dbHandler=new DbHandler();
        String LoginName=din.readUTF();
        loggedIn=0;
        JSONObject jsonObj=new JSONObject(LoginName);
        System.out.println(jsonObj.getString("action"));
        if(jsonObj.getString("action").equals("login"))
        {
            
            
            
            JSONObject loginInfo=jsonObj.getJSONObject("data");
            String user=loginInfo.getString("user");
            String pass=loginInfo.getString("pass");
            UserEntity userObj=dbHandler.checkLogin(user, pass);
            System.out.println(ClientSocket.toString());
            if(userObj!=null)
            {
                String jsonDatatoOthers="{\"response_type\":\"login\",\"response\":\"new\",\"name\":\""+userObj.getName()+"\"}";
                String jsonRes="{\"response_type\":\"login\",\"response\":\"success\"";
                System.out.println(users.size()+" users Online");
                if(users.size()>0)
                {
                    jsonRes+=",\"available\":[";
                    for(int i=0;i<users.size();i++)
                    {
                        if(users.get(i).getId()!=userObj.getId())
                        {
                            jsonRes+="\"";
                            jsonRes+=users.get(i).getName();

                            jsonRes+="\"";
                            if(i!=users.size()-1)
                            {
                                jsonRes+=",";
                            }

                            DataOutputStream tdout=new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                            tdout.writeUTF(jsonDatatoOthers);
                        }
                    }
                    jsonRes+="]";
                }
                if(userObj.getFriends().size()>0)
                {
                    jsonRes+=",\"friends\":[";
                    for(int i=0;i<userObj.getFriends().size();i++)
                    {
                        jsonRes+="\"";
                        jsonRes+=dbHandler.getUserName(userObj.getFriends().get(i));
                        jsonRes+="\"";
                        if(i!=userObj.getFriends().size()-1)
                        {
                            jsonRes+=",";
                        }
                    }
                    jsonRes+="]";
                }
                if(dbHandler.getOfflineMessages(userObj.getId()).size()>0)
                {
                    ArrayList<String> msgs=dbHandler.getOfflineMessages(userObj.getId());
                    jsonRes+=",\"messages\":[";
                    for(int i=0;i<msgs.size();i++)
                    {
                        jsonRes+="\"";
                        jsonRes+=msgs.get(i);
                        jsonRes+="\"";
                        if(i!=msgs.size()-1)
                        {
                            System.out.println(i+",");
                            jsonRes+=",";
                        }
                    }
                    jsonRes+="]";
                    dbHandler.deleteOfflineMessage(userObj.getId());
                }

                jsonRes+=",\"name\":\""+userObj.getName()+"\"}";
                System.out.println("User Logged In :" + user);

                userObj.setOwnSocket(ClientSocket);
                

                users.add(userObj);

                this.loggedIn=1;
                DataOutputStream tdout=new DataOutputStream(ClientSocket.getOutputStream());
                tdout.writeUTF(jsonRes);  

                            



            }
            else
            {
                System.out.println(ClientSocket.toString());
                String jsonResponse="{\"response_type\":\"login\",\"response\":\"failed\"}";
                DataOutputStream tdout=new DataOutputStream(ClientSocket.getOutputStream());
                tdout.writeUTF(jsonResponse); 
                System.out.println("login failed");
            }
            start();
        }
        
        
        
        
    }

    public void run()
    {
        while(true)
        {
            
            try
            {
                if(this.loggedIn==1)
                {    
                    System.out.println("Logged In");
                    
                    String msgFromClient=new String();
                    msgFromClient=din.readUTF();
                    System.out.println(msgFromClient);
                    JSONObject msgJson=new JSONObject(msgFromClient);
                    if(msgJson.getString("action").equals("send_message"))
                    {
                        String msg=msgJson.getString("message");
                        String rec=msgJson.getString("reciever");
                        String sender=msgJson.getString("sender");
                        int user_id=1;
                        for(int i=0;i<users.size();i++)
                        {
                            if(users.get(i).getName().equals(sender))
                            {
                                user_id=users.get(i).getId();
                            }
                        }
                        if(rec.equals(""))
                        {
                            rec="all";
                        }
                        this.sendMessage(user_id, rec, msg, sender);
                            
                        
                    }
                    else if(msgJson.getString("action").equals("request"))
                    {
                        String uname=msgJson.getString("sender");
                        String to=msgJson.getString("to");
                        this.addRequest(uname, to);
                    }
                    else if(msgJson.getString("action").equals("Accept"))
                    {
                        String uname=msgJson.getString("sender");
                        String to=msgJson.getString("to");
                        this.acceptFriend(uname, to);
                    }
                    else if(msgJson.getString("action").equals("logout"))
                    {
                        logOut(msgJson.getString("name"));
                        break;
                    }
                    else if(msgJson.getString("action").equals("file_transfer"))
                    {
                        saveFile(msgJson.getString("to"),msgJson.getString("file_content"),msgJson.getString("sender"));
                        break;
                    }
//                    String msgFromClient=new String();
//                    msgFromClient=din.readUTF();
//                    StringTokenizer st=new StringTokenizer(msgFromClient);
//                    String Sendto=st.nextToken();                
//                    String MsgType=st.nextToken();
//                    int iCount=0;

//                    if(MsgType.equals("LOGOUT"))
//                    {
//                        for(iCount=0;iCount<users.size();iCount++)
//                        {
//                            if(users.get(iCount).getName().equals(Sendto))
//                            {
//                                users.remove(iCount);
//                                
//                                System.out.println("User " + Sendto +" Logged Out ...");
//                                break;
//                            }
//                        }
//
//                    }
//                    else
//                    {
//                        String msg="";
//                        while(st.hasMoreTokens())
//                        {
//                            msg=msg+" " +st.nextToken();
//                        }
//                        for(iCount=0;iCount<users.size();iCount++)
//                        {
//                            if(users.get(iCount).getName().equals(Sendto))
//                            {    
//                                Socket tSoc=(Socket)users.get(iCount).getOwnSocket();                            
//                                DataOutputStream tdout=new DataOutputStream(tSoc.getOutputStream());
//                                tdout.writeUTF(msg);                            
//                                break;
//                            }
//                        }
//                        if(iCount==users.size())
//                        {
//                            dout.writeUTF("I am offline");
//                        }
//                        else
//                        {
//
//                        }
//                    }
//                    if(MsgType.equals("LOGOUT"))
//                    {
//                        break;
//                    }
                }
                else if(this.loggedIn==0)
                {
                    System.out.println("Not Logged In");
                    String dataRe=din.readUTF();
                    JSONObject jsonObj=new JSONObject(dataRe);
                    System.out.println(jsonObj.getString("action"));
                    if(jsonObj.getString("action").equals("login"))
                    {



                        JSONObject loginInfo=jsonObj.getJSONObject("data");
                        String user=loginInfo.getString("user");
                        String pass=loginInfo.getString("pass");
                        UserEntity userObj=dbHandler.checkLogin(user, pass);
                         System.out.println(ClientSocket.toString());
                        if(userObj!=null)
                        {
                            String jsonDatatoOthers="{\"response_type\":\"login\",\"response\":\"new\",\"name\":\""+userObj.getName()+"\"}";
                            String jsonRes="{\"response_type\":\"login\",\"response\":\"success\"";
                            System.out.println(users.size()+" users Online");
                            if(users.size()>0)
                            {
                                jsonRes+=",\"available\":[";
                                for(int i=0;i<users.size();i++)
                                {
                                    if(users.get(i).getId()!=userObj.getId())
                                    {
                                        jsonRes+="\"";
                                        jsonRes+=users.get(i).getName();

                                        jsonRes+="\"";
                                        if(i!=users.size()-1)
                                        {
                                            jsonRes+=",";
                                        }

                                        DataOutputStream tdout=new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                                        tdout.writeUTF(jsonDatatoOthers);
                                    }
                                }
                                jsonRes+="]";
                            }
                            if(userObj.getFriends().size()>0)
                            {
                                jsonRes+=",\"friends\":[";
                                for(int i=0;i<userObj.getFriends().size();i++)
                                {
                                    jsonRes+="\"";
                                    jsonRes+=dbHandler.getUserName(userObj.getFriends().get(i));
                                    jsonRes+="\"";
                                    if(i!=userObj.getFriends().size()-1)
                                    {
                                        jsonRes+=",";
                                    }
                                }
                                jsonRes+="]";
                            }
                            if(dbHandler.getOfflineMessages(userObj.getId()).size()>0)
                            {
                                ArrayList<String> msgs=dbHandler.getOfflineMessages(userObj.getId());
                                jsonRes+=",\"messages\":[";
                                for(int i=0;i<msgs.size();i++)
                                {
                                    jsonRes+="\"";
                                    jsonRes+=msgs.get(i);
                                    jsonRes+="\"";
                                    if(i!=msgs.size()-1)
                                    {
                                        System.out.println(i+",");
                                        jsonRes+=",";
                                    }
                                }
                                jsonRes+="]";
                                dbHandler.deleteOfflineMessage(userObj.getId());
                            }
                            jsonRes+=",\"name\":\""+userObj.getName()+"\"}";
                            System.out.println("User Logged In :" + user);

                            userObj.setOwnSocket(ClientSocket);
                           

                            users.add(userObj);

                            this.loggedIn=1;
                            DataOutputStream tdout=new DataOutputStream(ClientSocket.getOutputStream());
                            tdout.writeUTF(jsonRes);  

                            
                            
                        }
                        else
                        {
                            String jsonResponse="{\"response_type\":\"login\",\"response\":\"failed\"}";
                            DataOutputStream tdout=new DataOutputStream(ClientSocket.getOutputStream());
                            tdout.writeUTF(jsonResponse); 
                            System.out.println("login failed");
                        }
                       
                       
                    }
                }
                    
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
            
            
        }        
    }
    
    private void addRequest(String uname,String fname)
    {
        if(users.size()>0)
        {
            int i=0;
            int fid=0,uid=0;
            UserEntity fr=null,user=null;
            int fflag=0;
            for(;i<users.size();i++)
            {
                if(users.get(i).getName().equals(fname))
                {
                    fflag=1;
                    fid=users.get(i).getId();
                    fr=users.get(i);
                }
                else if(users.get(i).getName().equals(uname))
                {
                    
                    uid=users.get(i).getId();
                    user=users.get(i);
                }
                
            }
            if(fid!=0)
            {
                if(!dbHandler.isAlreadyRequested(uid, fid)&&!dbHandler.isAlreadyRequested(fid, uid)&&!user.isFriend(fid))
                {
                    boolean res=dbHandler.insertRequset(uid, fid);
                    System.out.println(res);
                    if(res)
                    {
                            try {
                                String jsonRes="{\"response_type\":\"friend_request\",\"sender\":\""+user.getName()+"\"}";
                                System.out.println(jsonRes);
                                DataOutputStream tdout=new DataOutputStream(fr.getOwnSocket().getOutputStream());
                                tdout.writeUTF(jsonRes);
                            } catch (IOException ex) {
                                Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    }
                    
                }
               
                else
                {
                   
                    try {
                        DataOutputStream tdout=null;

                        String jsonRes="{\"response_type\":\"error\",\"response\":\"You have already sent friend request to "+fr.getName()+"\"}";
                        if(dbHandler.isAlreadyRequested(fid, uid))
                        {
                            jsonRes="{\"response_type\":\"error\",\"response\":\""+fr.getName()+" sent you a friend request already. \\n To accept: Accept "+fr.getName()+"\"}";
                        }
                        else if(user.isFriend(fid))
                        {
                            jsonRes="{\"response_type\":\"error\",\"response\":\""+fr.getName()+" and you are already friend\"}";
                        }
                        System.out.println(jsonRes);

                        tdout = new DataOutputStream(user.getOwnSocket().getOutputStream());
                        tdout.writeUTF(jsonRes);

                    } catch (IOException ex) {
                        Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        
                }
            }
            else
            {
                try {
                    DataOutputStream tdout=null;

                    String jsonRes="{\"response_type\":\"error\",\"response\":\""+fname+" is now Offline\"}";
                    tdout = new DataOutputStream(user.getOwnSocket().getOutputStream());
                    tdout.writeUTF(jsonRes);
                } catch (IOException ex) {
                    Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    private void sendMessage(int sender,String reciever,String msg,String senderName)
    {
        System.out.println(sender+" "+reciever+" "+msg+" "+senderName);
        int errorFlag=0;
        if(reciever.equals("all_friends"))
        {
            for(int i=0;i<users.size();i++)
            {
                if(users.get(i).getId()==sender)
                {
                    for(int j=0;j<users.get(i).getFriends().size();j++)
                    {
                        this.sendMessage(sender, dbHandler.getUserName(users.get(i).getFriends().get(j)), msg, senderName);
                        
                    }
                }
            }
        }
        else if(reciever.equals("all"))
        {
            for(int i=0;i<users.size();i++)
            {  
                if(users.get(i).getId()!=sender)
                {
                    
                    DataOutputStream tdout = null;
                    try {
                        tdout = new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                        tdout.writeUTF("{\"response_type\":\"send_message\",\"response\":\""+msg+"\",\"name\":\""+senderName+"\"}");
                    } catch (IOException ex) {
                        Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                

            }
        }
        
        else
        {
            int i=0;
            for(;i<users.size();i++)
            {                           
                if(users.get(i).getName().equals(reciever))
                {
                    if(users.get(i).isFriend(sender))
                    {
                        DataOutputStream tdout = null;
                        try {
                            tdout = new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                            tdout.writeUTF("{\"response_type\":\"send_message\",\"response\":\""+msg+"\",\"name\":\""+senderName+"\"}");
                        } catch (IOException ex) {
                            Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                        } 
                    }
                    else
                    {
                        errorFlag=1;
                    }
                    break;
                        
                }
                

            }
            if(i==users.size())
            {
                for(int j=0;j<users.size();j++)
                {
                    if(users.get(j).getName().equals(senderName))
                    {
                        if(users.get(j).isFriend(dbHandler.getUserId(reciever)))
                        {
                            dbHandler.insertOfflineMessage(sender, dbHandler.getUserId(reciever), msg);
                        }
                        else
                        {
                            errorFlag=1;
                        }
                    }
                }
            }
            
        }
        for(int i=0;i<users.size();i++)
        {

            if(users.get(i).getId()==sender)
            {
                System.out.println("got the sender");
                DataOutputStream tdout = null;
                try {
                    tdout = new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                    if(errorFlag==1)
                    {
                        tdout.writeUTF("{\"response_type\":\"error\",\"response\":\"You can not send message to "+reciever+" as you are not friends\",\"name\":\""+senderName+"\"}");
                    }
                    else
                    {
                        tdout.writeUTF("{\"response_type\":\"send_message\",\"response\":\""+msg+"\",\"name\":\""+senderName+"\"}");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    private void acceptFriend(String uname,String fname)
    {
        if(users.size()>0)
        {
            int i=0;
            int fid=0,uid=0;
            UserEntity fr=null,user=null;
            int fflag=0;
            for(;i<users.size();i++)
            {
                if(users.get(i).getName().equals(fname))
                {
                    fflag=1;
                    fid=users.get(i).getId();
                    fr=users.get(i);
                }
                else if(users.get(i).getName().equals(uname))
                {
                    
                    uid=users.get(i).getId();
                    user=users.get(i);
                }
                
            }
            if(fflag!=0)
            {
                System.out.println(dbHandler.isAlreadyRequested(uid, fid)+" "+user.isFriend(fid));
                if(dbHandler.isAlreadyRequested(fid, uid)&&!user.isFriend(fid))
                {
                    dbHandler.addFriend(uid,fid);
                        try {
                            
                            String jsonRes="{\"response_type\":\"accept_friend\",\"by\":\""+user.getName()+"\"}";
                            DataOutputStream tdout=null;
                            tdout = new DataOutputStream(fr.getOwnSocket().getOutputStream());
                            tdout.writeUTF(jsonRes);
                        } catch (IOException ex) {
                            Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                         try {
                        String jsonRes="{\"response_type\":\"new_friend\",\"response\":\""+user.getName()+"\"}";
                        DataOutputStream tdout=null;
                        tdout = new DataOutputStream(user.getOwnSocket().getOutputStream());
                        tdout.writeUTF(jsonRes);
                        } catch (IOException ex) {
                            Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                else
                {
                    try {
                        DataOutputStream tdout=null;

                        String jsonRes="";
                        if(!dbHandler.isAlreadyRequested(fid, uid))
                        {
                            jsonRes="{\"response_type\":\"error\",\"response\":\""+fr.getName()+" did not send you a friend request already. \\n To request write: Accept "+fr.getName()+"\"}";
                        }
                        else if(user.isFriend(fid))
                        {
                            jsonRes="{\"response_type\":\"error\",\"response\":\""+fr.getName()+" and you are already friend\"}";
                        }
                        System.out.println(jsonRes);

                        tdout = new DataOutputStream(user.getOwnSocket().getOutputStream());
                        tdout.writeUTF(jsonRes);

                    } catch (IOException ex) {
                        Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    private void logOut(String uname)
    {
        
        for(int i=0;i<users.size();i++)
        {
            if(users.get(i).getName().equals(uname))
            {
                users.remove(i);
                break;
            }
            
        }
        for(int i=0;i<users.size();i++)
        {
            try {
                    String jsonRes="{\"response_type\":\"log_out\",\"name\":\""+uname+"\"}";
                    DataOutputStream tdout=null;
                    tdout = new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
                    tdout.writeUTF(jsonRes);
                } catch (IOException ex) {
                    Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
    private void saveFile(String reciever,String file_content,String sender)
    {
        //
        
        Date date=new Date();
        String d=date.toString();
        d=d.replace(" ", "");
        d=d.replace(":", "");
        d=d.replace(".", "");
        String path="uploads/"+reciever+"_"+sender+"_"+d+".txt";
        
        File file=new File(path);
        FileOutputStream fis=null;
        if(!file.exists())
        {
                try {
                    fis=new FileOutputStream(file);
                    for(int i=0;i<file_content.length();i++)
                    {
                        fis.write(file_content.charAt(i));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(chatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                int fid=0,uid=0;
                
                for(int i=0;i<users.size();i++)
                {
                    if(users.get(i).getName().equals(sender))
                    {
                        uid=users.get(i).getId();
                        DataOutputStream tdout = null;
//                        tdout = new DataOutputStream(users.get(i).getOwnSocket().getOutputStream());
//                        tdout.writeUTF("{\"response_type\":\"send_message\",\"response\":\""+msg+"\",\"name\":\""+senderName+"\"}");
                        break;
                    }
                }
                for(int i=0;i<users.size();i++)
                {
                    if(users.get(i).getName().equals(reciever))
                    {
                        fid=users.get(i).getId();
                        break;
                    }
                }
                
            
        }
    }
    
}
}