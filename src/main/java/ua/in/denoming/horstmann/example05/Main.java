package ua.in.denoming.horstmann.example05;

public class Main {
    public static void main(String[] args) {
        try (
            App app = App.getInstance()
        ) {
            app.accept(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
