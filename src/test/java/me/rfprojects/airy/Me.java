package me.rfprojects.airy;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Me extends Person implements Serializable {

    private String major;
    private byte cet;
    private Date graduation;
    private List<Skill> skillList;
    private Load loadTest;

    public String getMajor() {
        return major;
    }

    public Me setMajor(String major) {
        this.major = major;
        return this;
    }

    public byte getCet() {
        return cet;
    }

    public Me setCet(byte cet) {
        this.cet = cet;
        return this;
    }

    public Date getGraduation() {
        return graduation;
    }

    public Me setGraduation(Date graduation) {
        this.graduation = graduation;
        return this;
    }

    public List<Skill> getSkillList() {
        return skillList;
    }

    public Me setSkillList(List<Skill> skillList) {
        this.skillList = skillList;
        return this;
    }

    @Override
    public String toString() {
        return "Me{" +
                "major='" + major + '\'' +
                ", cet=" + cet +
                ", graduation=" + graduation +
                ", skillList=" + skillList +
                '}';
    }

    public Load getLoadTest() {
        return loadTest;
    }

    public Me setLoadTest(Load loadTest) {
        this.loadTest = loadTest;
        return this;
    }
}
