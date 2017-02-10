package main;

/**
 * Created by bse71 on 10.02.2017.
 */
public class People {
    private String name;
    private int age;
    private double salary;

    public People() {
        this("StandartName", 99, 99.0);
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getSalary() {
        return salary;
    }

    public People(String name, int age, double salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    @Override
    public String toString(){
        return name + " " + age + " age";
    }
}
