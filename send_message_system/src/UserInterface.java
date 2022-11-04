import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;

public class UserInterface {
    int user_id;
    String user_name;

    int number_users;
    Database_connection database;
    Scanner input = new Scanner(System.in);

    public UserInterface(Database_connection database){
       this.database = database;
       number_users = database.countUsers();
    }

    public void firstPage() {
        boolean checked = false;
        Scanner input = new Scanner(System.in);

        while(true) {
            System.out.println("1)register 2)login");
            int in = Integer.parseInt(input.nextLine());
            if (in == 1) {
                register();
                checked = true;
            } else if (in == 2) {
                checked = login();
            }else{
                continue;
            }
            break;
        }
        if(checked){
            System.out.printf("    [[welcome %s]]    %n",user_name);
            System.out.println();
            secondPage();
        }
    }

    private void secondPage(){
        while (true) {
            System.out.println();
            System.out.println("1)find a friend " +
                    "2)show friendList " +
                    "3)show requests" +
                    "4)block someone" +
                    "5)unblock someone" +
                    "6)delete account" +
                    "7)end program");
            int in = input.nextInt();
            if (in == 1) {
                System.out.print("search_name: ");
                String name = input.next();
                database.searchUser(name, -1, user_id,1);
                System.out.print("enter number to invite otherwise enter '0': ");
                int choose_person = input.nextInt();
                if(choose_person == 0){
                    continue;
                }
                database.searchUser(name, choose_person, user_id,1);

            } else if (in == 2) {
                database.friendList_show(user_id);
            } else if (in == 3) {
                database.showRequests(user_id);

            }else if(in == 4){
                block_user();
            }else if(in == 5){
                database.check_blocked(-1,user_id,2);
            }else if(in == 6){
                String delete_account = String.format("update users set Deleted = 1 where Id = %d",user_id);
                database.execute(delete_account,-1);
                return;
            }else if(in == 7){
                break;
            }
        }
    }

    private void block_user() {
        String searched_name;

        System.out.print("search name: ");
        while (true) {
            searched_name = input.nextLine();
            if (!searched_name.equals("")) {
                break;
            }
        }
        database.searchUser(searched_name, -1, user_id, 2);
        System.out.print("enter number to block otherwise enter '0': ");
        int choose_person = input.nextInt();
        if (choose_person == 0) {
            return;
        }
        int search_id = database.searchUser(searched_name, choose_person, user_id, 2);

        String insert_block = String.format("insert into block values(%d,%d)", user_id, search_id);
        database.execute(insert_block, -1);

//        String delete_from_friendList = String.format("update friend_list where User_Id = %d and Friend_Id = %d",user_id,search_id);

    }


    private boolean login(){
        while(true) {
            System.out.println();
            System.out.print("ID: ");
            int ID = input.nextInt();
            System.out.print("password: ");
            String password = input.next();

            String full_name = database.check_user(ID, password, 1);
            if (full_name != null) {
                if(full_name.equals("DELETED")){
                    continue;
                }
                user_id = ID;
                user_name = full_name;
                return true;
            }
            System.out.println("your ID or password is wrong");
            add_failedLogin(ID);
            System.out.println("1)use securityPass 2)try again 3)exit");
            int in = input.nextInt();
            if(in == 1){
                if(securityLogin()){
                    return true;
                }
            }else if(in == 2){

            }else{
                return false;
            }
        }
//        System.out.println("");
    }

    private void add_failedLogin(int id) {
        Date date = new Date();
        String now = (date.getYear()+1900)+"-"+date.getMonth()+"-"+date.getDate()+" "
                +date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
        int attempt_count = database.count_failedLogin(id);
        String add_failLogin = String.format("insert into failed_login values(%d,%d,'%s')",id,attempt_count+1,now);
        database.execute(add_failLogin,-1);
    }

    private boolean securityLogin() {
        while(true) {
            System.out.println();
            System.out.print("ID: ");
            int ID = input.nextInt();
            System.out.print("what is your best friend: ");
            String security_pass = input.next();

            String full_name = database.check_securityPass(ID, security_pass);
            if (full_name != null) {
                user_id = ID;
                user_name = full_name;
                return true;
            }
            System.out.println("your ID or securityPass is wrong");
            add_failedSecurity(ID);
            System.out.println("1)try again 2)exit");
            int in = input.nextInt();
            if (in == 1) {

            } else {
                return false;
            }
        }
    }

    private void add_failedSecurity(int id) {
        int attempt_count = database.count_failedSecurity(id);
        String add_failSecurity = String.format("insert into failed_security_pass values(%d,%d)",id,attempt_count+1);
        database.execute(add_failSecurity,-1);
    }

    private void register(){
        while(true) {
            System.out.println();
            System.out.print("name : ");
            String name = input.next();
            System.out.print("familyName : ");
            String familyName;
            while (true) {
                String f = input.nextLine();
                if (f != null) {
                    familyName = input.nextLine();
                    break;
                }
            }
            System.out.print("phoneNumber : ");
            String phoneNumber = input.nextLine();
            System.out.print("Email : ");
            String email = input.nextLine();
            System.out.print("password : ");
            String password = input.nextLine();
            System.out.print("what is your best friend : ");
            String securityQuestion_answer = input.nextLine();

            String insert_users = String.format("insert into users(Id,Name,FamilyName,PhoneNumber,Email,Password)" +
                    "values (%d,'%s','%s','%s','%s','%s');", ++number_users, name, familyName, phoneNumber, email, password);
            String insert_security_question = String.format("insert into security_question" +
                    " values (%d,'what is your best friend','%s');", number_users, securityQuestion_answer);
            if(database.execute(insert_users, 1)){
                continue;
            }
            database.execute(insert_security_question, -1);

            System.out.println(String.format("you have successfully registered, this is your ID:(%d)", number_users));
            user_name = name;
            break;
        }
    }


}
