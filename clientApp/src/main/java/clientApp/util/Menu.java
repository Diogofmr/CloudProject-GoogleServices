package clientApp.util;

import java.util.Scanner;

public class Menu {
    public static int make() {
        int op;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("===========MENU=============");
            System.out.println(" 1 - Submit an Image");
            System.out.println(" 2 - Get Image Info");
            System.out.println(" 3 - Get Images Names");
            System.out.println(" 4 - Download Image");
            System.out.println(" ⚙ :: Resizing instance group :: ");
            System.out.println(" 5 - Server Instances");
            System.out.println(" 6 - Labels Instances");
            System.out.println("99 - Exit");
            op = scanner.nextInt();
        } while (!((op >= 1 && op <= 6) || op == 99));
        return op;
    }
}
