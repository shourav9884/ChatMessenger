/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.sql.Statement;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nati
 */
public class Database {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    
    
    public void connect()
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect=DriverManager.getConnection("jdbc:mysql://localhost/chat_server?"+ "user=root&password=");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean insert(String tableName,String values)
    {
        boolean result=false;
        if(connect!=null)
        {
            try {
                statement=(Statement) connect.createStatement();
                String query="INSERT into "+tableName+" Values("+values+")";
                System.out.println(query);
                result=statement.execute(query);
                
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    public boolean delete(String tableName,String where)
    {
        try {
            statement=(Statement) connect.createStatement();
            String query="DELETE from "+tableName+" WHERE "+where;
            System.out.println(query);
            statement.execute(query);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public boolean update(String tableName,String updateString,String where)
    {
        try {
            statement=(Statement) connect.createStatement();
            String query="UPDATE "+tableName+" SET "+updateString+" where "+where;
            System.out.println(query);
            statement.execute(query);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    public ResultSet select(String tableName,String columns,String where)
    {
        if(connect!=null)
        {
            try {
                statement = (Statement) connect.createStatement();
                String query=null;
                query="Select "+columns+" from "+tableName+" ";
                if(where!=null)
                {
                    query+=" where "+where;
                }
                System.out.println(query);
                resultSet=statement.executeQuery(query);
                return resultSet;
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    public int getRowCount(ResultSet set) throws SQLException  
    {  
        int rowCount;  
        int currentRow = set.getRow();            // Get current row  
        rowCount = set.last() ? set.getRow() : 0; // Determine number of rows  
        if (currentRow == 0)                      // If there was no current row  
            set.beforeFirst();                     // We want next() to go to first row  
        else                                      // If there WAS a current row  
            set.absolute(currentRow);              // Restore it  
        return rowCount;  
    } 
    public void close() {
    try {
      if (resultSet != null) {
        resultSet.close();
      }

      if (statement != null) {
        statement.close();
      }

      if (connect != null) {
        connect.close();
      }
    } catch (Exception e) {

    }
  }
}
