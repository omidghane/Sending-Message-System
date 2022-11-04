import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Database_connection {
    Scanner input = new Scanner(System.in);
    Message_Database message;
    int number_users;

    String url = "jdbc:mysql://localhost:3306/send_message_system";
    String username = "root";
    String password = "13812015";
    Connection con = DriverManager.getConnection(url, username, password);
    Statement st = con.createStatement();

    public Database_connection() throws SQLException {

    }

    public void friendList_show(int user_id){
        String deleted_user ;
        ArrayList<Integer> friends_id = new ArrayList<>();
        int index = 0;
        try
        {
            boolean rs = st.execute(String.format("select * from friend_list where User_Id = %d",user_id));
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    int friend_id = result.getInt("Friend_Id");
                    friends_id.add(friend_id);
                }
                System.out.println("1)send message" +
                        "2)delete friend");
                int choose = input.nextInt();
                for(int fr : friends_id){
                    index++;
                    String friend_name = check_user(fr,"",2);
                    System.out.print(index + " )" + friend_name);
                    if(check_user(fr,"",3) != null){
                        System.out.print(" " + "[deleted]");
                    }
                    System.out.println();
                }
                if(choose == 1) {
                    message = new Message_Database(con, st);
                    message.frined_message(user_id, friends_id);
                }else if(choose == 2){
                    delete_friend(friends_id,user_id);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void delete_friend(ArrayList<Integer> friends_id,int user_id) {
        if(friends_id.isEmpty()){
            System.out.println("no one to delete ");
            return;
        }
        System.out.println("choose number to delete (to exit enter '0'): ");
        int choose_n = input.nextInt();
        if(choose_n == 0){
            return;
        }
        System.out.println();
        int friendId_delete = friends_id.get(choose_n-1);
        String delete_friend = String.format("delete from friend_list where User_Id = %d and Friend_Id = %d",friendId_delete,user_id);
        String delete_invitation = String.format("delete from invitation where Inviter_Id = %d and Invited_Id = %d",friendId_delete,user_id);
        execute(delete_invitation,-1);
        execute(delete_friend,-1);
    }

    public void showRequests(int user_id){
        ArrayList<Integer> request_ids = new ArrayList<>();
        try
        {
            boolean rs = st.execute(String.format("select * from invitation where Invited_Id = %d",user_id));
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    int inviter_id = result.getInt("Inviter_Id");
                    int acception = result.getInt("Acception");
                    if(acception == 0) {
                        request_ids.add(inviter_id);
                    }
                }
                for(int req : request_ids){
                    String inviter_name = check_user(req,"",2);
                    System.out.println("1) " + inviter_name);
                }
                if(request_ids.isEmpty()){
                    System.out.println("do not have any request!!");
                    return;
                }
                accept_request(request_ids,user_id);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * if choose = -1 : it shows the same names of input names
     * identity 1 invites the name choose
     * identity 2 give the requested_id
     * @param name
     * @param choose
     * @param user_id
     */
    public int  searchUser(String name,int choose,int user_id,int identity){
        try
        {
            boolean rs = st.execute(String.format("select * from users where name like '%%%s%%'",name));
            if(rs) {
                int index = 0;
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    index++;
                    String searched_name = result.getString("Name");
                    int requested_id = result.getInt("Id");
                    if(choose == -1) {
                        System.out.println(index + ") " + searched_name);
                    }
                    else if (choose == index && requested_id != user_id && identity == 2) {
                        return requested_id;
                    }else if(choose == index && requested_id != user_id && identity == 1){
                        if(check_blocked(requested_id,user_id,1)){
                            System.out.println("you have blocked ...");
                        }else {
                            execute(String.format("insert into invitation values(%d,%d,0)", user_id, requested_id), -1);
                        }
                        return 0;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     *
     * @param ID
     * @param password
     * @param identity if =1 used to check login ,
     *                =2 used to give fullName by ID,
     *                 =3 check is deleted or not
     * @return
     */
    public String check_user(int ID,String password,int identity){
        String full_name;
        try
        {
            boolean rs = st.execute("select * from users");
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    int id_t = result.getInt("Id");
                    int deleted = result.getInt("Deleted");
                    String password_t = result.getString("Password");
                    String name = result.getString("Name");
                    String familyName = result.getString("FamilyName");
                    full_name = name + " " + familyName;
                    if(identity == 2 && ID == id_t){
                        return full_name;
                    }else if(identity == 1 && ID == id_t && deleted == 1){
                        System.out.println("account is deleted");
                        return "DELETED";
                    }else if(identity == 1 && ID == id_t && password.equals(password_t)){
                        return full_name;
                    }else if(identity == 3 && ID == id_t && deleted == 1){
                        return "DELETED";
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public  String check_securityPass(int ID,String security_pass){
        String full_name;
        boolean checked = false;
        try
        {
            boolean rs = st.execute("select * from security_question");
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    int id_t = result.getInt("User_Id");
                    String security_t = result.getString("Answer");
                   if(ID == id_t && security_pass.equals(security_t)){
                        checked = true;
                    }
                }
                full_name = check_user(ID,"",2);
                if(checked){
                    return full_name;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean check_blocked(int req_id,int user_id,int identity){
        ArrayList<Integer> blocked_ids = new ArrayList<>();
        String blocks = "select * from block";
        int index = 0;
        int Blocker = 0;

        try {
            boolean rs = st.execute(blocks);
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    index++;
                    int blocker = result.getInt("Blocker_Id");
                    int blocked = result.getInt("Blocked_Id");
                    if(blocker == req_id && blocked == user_id && identity == 1){
                        return true;
                    }
                    if(blocker == user_id){
                        blocked_ids.add(blocked);
                    }
                }
                if(identity == 2) {
                    for (int blocked : blocked_ids) {
                        String blocked_name = check_user(blocked,"",2);
                        System.out.println(index + ") " + blocked_name);
                    }
                }
                if(identity == 2){
                    System.out.print("choose to unlock otherwise enter (0): ");
                    int choose = input.nextInt();
                    if(choose == 0){
                        return false;
                    }
                    int blocked = blocked_ids.get(choose-1);
                    String deleting_block = String.format("delete from block where Blocker_Id = %d and Blocked_Id = %d",user_id,blocked);
                    execute(deleting_block,-1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public int count_failedSecurity(int id){
        String count = String.format("select User_Id , max(Attempt_Count) from failed_security_pass where User_Id = %d;",id);

        try {
            boolean rs = st.execute(count);
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    return result.getInt("max(Attempt_Count)");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public int count_failedLogin(int id){
        String count = String.format("select User_Id , max(Attempt_Count) from failed_login where User_Id = %d;",id);

        try {
            boolean rs = st.execute(count);
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    return result.getInt("max(Attempt_Count)");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public int countUsers(){
        String count = "select count(Id) as Num_users from users";

        try {
            boolean rs = st.execute(count);
            if(rs) {
                ResultSet result = st.getResultSet();
                while (result.next()) {
                    number_users = result.getInt("Num_users");
                }
                return number_users;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * insert & update & delete
     * identity 1 check exception about duplicate email or phoneNumber
     * @param in
     */
    public boolean execute(String in,int identity){
        try {
            st.execute(con.nativeSQL(in));
        }catch (Exception e){
            if(identity == 1){
                System.out.println("your email or phoneNumber is exist ");
                return true;
            }else {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * choose person want to add to friend
     * @param request_ids
     * @param user_id
     */
    private void accept_request(ArrayList<Integer> request_ids, int user_id) {
        System.out.print("choose number to accept friend otherwise enter '0': ");
        int choose_person = input.nextInt();
        if(choose_person == 0){
            return;
        }
        int req_id = request_ids.get(choose_person-1);
        execute(String.format("update invitation set Acception = 1 where Inviter_Id = %d",req_id),-1);
        execute(String.format("insert into friend_list values(%d,%d)",req_id,user_id),-1);
        execute(String.format("insert into friend_list values(%d,%d)",user_id,req_id),-1);
    }

//    public void run(){
//        System.out.println();
//        try
//        {
//            boolean rs = st.execute("select * from friend_list");
//            if(rs) {
//                ResultSet result = st.getResultSet();
//                while (result.next()) {
//                    System.out.print(result.getInt(1) + " ");
//                    System.out.print(result.getString(2) + " ");
////                    System.out.print(result.getString("FamilyName") + " ");
////                    System.out.print(result.getString("Password") + " ");
//                    System.out.println();
//                }
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
}
