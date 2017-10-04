package ua.in.denoming.horstmann.example06;

public class Main {
    public static void main(String[] args) {
        try (
            App app = App.getInstance("ua.in.denoming.horstmann.example06")
        ) {
            app.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
