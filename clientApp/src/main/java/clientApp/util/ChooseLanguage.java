package clientApp.util;

import java.util.Scanner;

public class ChooseLanguage {

    private static String targetLanguage = null;

    public static String chooseLanguage() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the target language for the labels:");
        System.out.println("1 - English");
        System.out.println("2 - Portuguese");
        System.out.println("3 - Spanish");
        System.out.println("4 - French");
        System.out.println("5 - German");
        System.out.println("6 - Italian");
        System.out.println("7 - Dutch");
        System.out.println("8 - Russian");
        System.out.println("9 - Chinese");
        System.out.println("99 - Exit");
        int option = scanner.nextInt();
        switch (option) {
            case 1:
                targetLanguage = "en";
                break;
            case 2:
                targetLanguage = "pt";
                break;
            case 3:
                targetLanguage = "es";
                break;
            case 4:
                targetLanguage = "fr";
                break;
            case 5:
                targetLanguage = "de";
                break;
            case 6:
                targetLanguage = "it";
                break;
            case 7:
                targetLanguage = "nl";
                break;
            case 8:
                targetLanguage = "ru";
                break;
            case 9:
                targetLanguage = "zh";
                break;
            case 99:
                System.exit(0);
        }
        return targetLanguage;
    }
}
