package vn.cinema;

import java.time.*;

public class club {
    public static void main(String[] args) {

        System.out.println("Hello World");
        Thread thread = new Thread(()-> System.out.println("Task 1"));

        thread.start();

        System.out.println("Task 2");
    }
}
