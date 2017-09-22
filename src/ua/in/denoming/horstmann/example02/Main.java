package ua.in.denoming.horstmann.example02;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String name = args[0];
            ClassReflector.print(System.out, name);
        }

        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Enter class name (e.g. java.util.Date): ");
            String name = in.next();
            ClassReflector.print(System.out, name);
        }
    }
}
