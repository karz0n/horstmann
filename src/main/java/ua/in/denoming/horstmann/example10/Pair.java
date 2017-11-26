package ua.in.denoming.horstmann.example10;

class Pair<T> {
    private T first;
    private T second;

    Pair() {
        first = null;
        second = null;
    }

    Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    T getFirst() {
        return first;
    }

    T getSecond() {
        return second;
    }

    void setFirst(T newValue) {
        first = newValue;
    }

    void setSecond(T newValue) {
        second = newValue;
    }

    void swap() {
        T temp = first;
        first = second;
        second = temp;
    }
}