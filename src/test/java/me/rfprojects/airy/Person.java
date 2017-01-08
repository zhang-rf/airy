package me.rfprojects.airy;

import java.io.Serializable;

public class Person implements Serializable {

    private String name;
    private Gender gender;
    private long mobile;
    private String email;

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Gender getGender() {
        return gender;
    }

    public Person setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public long getMobile() {
        return mobile;
    }

    public Person setMobile(long mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Person setEmail(String email) {
        this.email = email;
        return this;
    }
}
