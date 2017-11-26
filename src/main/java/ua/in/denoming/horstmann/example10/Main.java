package ua.in.denoming.horstmann.example10;

public class Main {
    public static void main(String[] args) {
        Manager ceo = new Manager("Gus Greedy", 800000, 2003, 12, 15);
        Manager cfo = new Manager("Sid Sneaky", 600000, 2003, 12, 15);
        Pair<Manager> buddies = new Pair<>(ceo, cfo);
        printBuddies(buddies);

        ceo.setBonus(1000000);
        cfo.setBonus(500000);
        Manager[] managers = {ceo, cfo};

        Pair<Employee> result = new Pair<>();
        minmaxBonus(managers, result);
        System.out.print("Min-max bonus: ");
        System.out.println("first = " + result.getFirst().getName()
            + ", second = " + result.getSecond().getName());
        System.out.print("Max-min bonus: ");
        maxminBonus(managers, result);
        System.out.println("first = " + result.getFirst().getName()
            + ", second = " + result.getSecond().getName());
    }

    /**
     * Wildcard admits all classes that extend Employee
     * type of T is Employee (we can only read)
     */
    private static void printBuddies(Pair<? extends Employee> p) {
        Employee first = p.getFirst();
        Employee second = p.getSecond();
        System.out.println(first.getName() + " and " + second.getName() + " are buddies.");
    }

    /**
     * Wildcard admits all super classes up to Manager (inclusive)
     * The smallest common type is Object (? can be: Object || Employee || Manager)
     * So the type of T will be Object
     * In pair we can store objects that has type Manager or all their subtype
     * (because in this situation we will have safe cast of types)
     * Wildcard with super restriction is for write operation
     */
    private static void minmaxBonus(Manager[] a, Pair<? super Manager> r) {
        if (a.length == 0) return;
        Manager min = a[0];
        Manager max = a[0];
        for (int i = 1; i < a.length; i++) {
            if (min.getBonus() > a[i].getBonus()) min = a[i];
            if (max.getBonus() < a[i].getBonus()) max = a[i];
        }
        r.setFirst(min);
        r.setSecond(max);
    }

    private static void maxminBonus(Manager[] a, Pair<? super Manager> r) {
        minmaxBonus(a, r);
        r.swap();
    }
}
