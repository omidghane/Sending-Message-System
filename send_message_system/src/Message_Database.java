import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class Message_Database extends Database_connection{
    Scanner input = new Scanner(System.in);

    Connection con;
    Statement st;

    public Message_Database(Connection con,Statement st) throws SQLException {
        super();
        this.con = con;
        this.st = st;
    }

    public void frined_message(int user_id,ArrayList<Integer> friends_id){
        while (true) {
            int friendId_toMessage;
            String friendName_toMessage;

            if (friends_id.isEmpty()) {
                System.out.println("no one to message ");
                return;
            }
            System.out.println("choose number to message (to exit enter '0'): ");
            int choose = input.nextInt();
            if (choose == 0) {
                return;
            }
            friendId_toMessage = friends_id.get(choose - 1);
            System.out.println();
            if (check_blocked(friendId_toMessage, user_id, 1)) {
                System.out.println("you have blocked ...");
                return;
            }
            friendName_toMessage = check_user(friendId_toMessage, "", 2);

            System.out.printf("    [%s]    ", friendName_toMessage);
            System.out.println("if to exit enter '0' ");
            System.out.println();

            received_messages(user_id, friendId_toMessage, friendName_toMessage);
            execute(String.format("update message set Seen = 1 where Sender_Id = %d and Receiver_Id = %d;", friendId_toMessage, user_id), -1);

            if (check_user(friendId_toMessage, "", 3) != null) {
                System.out.println("");
                continue;
            }
            send_message(user_id, friendId_toMessage);
            break;
        }
    }

    private void received_messages(int user_id,int friendId_toMessage,String friendName_toMessage) {
        String sender_name = friendName_toMessage;
        ArrayList<String> messages = new ArrayList<>();
        try
        {
            boolean rs = st.execute(String.format("select *" +
                    " from message" +
                    " where Sender_Id = '%s' and Receiver_Id = '%s'" +
                    "order by Message_Id asc;",friendId_toMessage,user_id));
            if(rs) {
                int index = 0;
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    index++;
                    int liked = result.getInt("Liked");
                    int seen = result.getInt("Seen");
                    String message = result.getString("Content");
                    messages.add(message);
                    System.out.print(index + ") " + sender_name + " : " + message + " ");
                    if(liked == 1){
                        System.out.print("[liked] ");
                    }
                    if(seen == 1){
                        System.out.println("[seen]");
                    }else{
                        System.out.println("");
                    }
                }
                if(!messages.isEmpty()) {
                    like_message(messages, user_id, friendId_toMessage);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void like_message(ArrayList<String> messages,int user_id,int friendId_toMessage) {
        while(true) {
            System.out.println("choose a message to like otherwise enter (0)");
            System.out.print(": ");
            int choose = input.nextInt();
            if (choose == 0) {
                return;
            }
            String message_liked = messages.get(choose - 1);
            String update_like = String.format("update message set Liked = 1 " +
                    "where Content = '%s' and Sender_Id = %d and Receiver_Id = %d", message_liked, friendId_toMessage, user_id);
            execute(update_like, -1);
        }
    }

    private void send_message(int user_id,int friendId_toMessage) {
        System.out.println("(now send message)");
        String message;
        input.nextLine();
        while(true){
            System.out.print("you : ");
            //Debug input
            while (true) {
                message = input.nextLine();
                if (message.equals("0")) {
                    return;
                }
                if(message != null){
                   // message = input.nextLine();
                    break;
                }
            }
            int number_ofMessages = count_message(user_id,friendId_toMessage);
            execute(String.format("insert into message (Sender_Id,Receiver_Id,Message_Id,Content) values (%d,%d,%d,'%s');",user_id,friendId_toMessage,number_ofMessages+1,message),-1);
        }
    }

    public int count_message(int sender_id,int receiver_id){
        try
        {
            boolean rs = st.execute(String.format("select count(Content) as Num_Message" +
                    " from message" +
                    " where Sender_Id = '%s' and Receiver_Id = '%s';",sender_id,receiver_id));
            if(rs) {
                int index = 0;
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    return result.getInt("Num_Message");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

}
