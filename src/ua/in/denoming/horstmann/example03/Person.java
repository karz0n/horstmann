package ua.in.denoming.horstmann.example03;

class Person implements Cloneable {
    private String firstName;
    private String lastName;

    Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    String getFirstName() {
        return firstName;
    }

    String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", this.firstName, this.lastName);
    }
}
