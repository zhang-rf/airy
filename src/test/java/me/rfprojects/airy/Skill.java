package me.rfprojects.airy;

import java.io.Serializable;

public class Skill implements Serializable {

    private String name;
    private String level;
    private String description;

    public Skill() {
    }

    public Skill(String name, String level, String description) {
        this.name = name;
        this.level = level;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Skill setName(String name) {
        this.name = name;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public Skill setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Skill setDescription(String description) {
        this.description = description;
        return this;
    }
}
