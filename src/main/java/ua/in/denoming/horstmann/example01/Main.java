package ua.in.denoming.horstmann.example01;

enum Size {
    SMALL("S"), MEDIUM("M"), LARGE("L"), EXTRA_LARGE("XL");

    private String abbreviation;

    Size(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}

public class Main {
    public static void main(String[] args) {
        Main.printSeparator();
        Main.printExample();
        Main.printSeparator();
    }

    private static void printExample() {
        Size size = Size.valueOf("SMALL");
        System.out.println("toString() = " + size.toString());
        System.out.println("compareTo(Size.SMALL) = " + size.compareTo(Size.SMALL));
        System.out.println("getAbbreviation() = " + size.getAbbreviation());
        System.out.println("ordinal() = " + size.ordinal());
    }

    private static void printSeparator() {
        System.out.println("=================================");
    }
}
