package me.rfprojects.airy;

import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

public class Developer implements Serializable {

    private static final long serialVersionUID = -645762594902117019L;

    private String name;
    private long mobile;
    private String email;
    private URL github;

    public Developer() {
    }

    public Developer(String name, long mobile, String email, URL github) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.github = github;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMobile() {
        return mobile;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public URL getGithub() {
        return github;
    }

    public void setGithub(URL github) {
        this.github = github;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Developer developer = (Developer) o;
        return mobile == developer.mobile &&
                Objects.equals(name, developer.name) &&
                Objects.equals(email, developer.email) &&
                Objects.equals(github, developer.github);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mobile, email, github);
    }
}
