package ua.in.denoming.horstmann.example03;

import java.util.ArrayList;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        sortByFirstName();
        sortByLastName();
        sortByLengthOfFirstName();
    }

    /**
     * Sort by length of first name
     */
    private static void sortByLengthOfFirstName() {
        ArrayList<Person> persons = getPersons();
        persons.sort(
            Comparator.comparingInt(p -> p.getFirstName().length())
        );
        printPersons(persons, "sort by length of first name");
    }

    /**
     * Sort by first name
     */
    private static void sortByFirstName() {
        ArrayList<Person> persons = getPersons();
        persons.sort(
            Comparator.comparing(Person::getFirstName, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        printPersons(persons, "sort by first name");
    }

    private static void sortByLastName() {
        ArrayList<Person> persons = getPersons();
        persons.sort(
            Comparator.comparing(Person::getLastName, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        printPersons(persons, "sort by last name");
    }

    private static ArrayList<Person> getPersons() {
        ArrayList<Person> persons = new ArrayList<>();
        persons.add(new Person("Jesus", "Christ"));
        persons.add(new Person("Michael ", "Jackson"));
        persons.add(new Person("Adolf ", "Hitler"));
        persons.add(new Person("Muhammad", null));
        persons.add(new Person("Albert", "Einstein"));
        return persons;
    }

    private static void printPersons(ArrayList<Person> persons, String method) {
        System.out.println("==========================");
        System.out.println("Sort method: " + method);
        for (Person person : persons) {
            System.out.println(person.toString());
        }
        System.out.println("==========================");
    }
}
