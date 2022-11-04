import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Database_connection d = null;
        try {
            d = new Database_connection();
        }catch (Exception e){
            e.printStackTrace();
        }

//        d.execute("delete from invitation");
        UserInterface userInterface = new UserInterface(d);

        userInterface.firstPage();
//        d.run();
    }

}
