package me.rfprojects.airy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Project extends RootProject implements Serializable {

    private static final long serialVersionUID = 2073449274934111076L;

    private String group;
    private String version;
    private License license;
    private Developer[] developers;

    public Project() {
    }

    public Project(String name, String group, String version, License license, Developer... developers) {
        super(name);
        this.group = group;
        this.version = version;
        this.license = license;
        this.developers = developers;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public Developer[] getDevelopers() {
        return developers;
    }

    public void setDevelopers(Developer... developers) {
        this.developers = developers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(group, project.group) &&
                Objects.equals(version, project.version) &&
                license == project.license &&
                Arrays.equals(developers, project.developers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, version, license, developers);
    }

    @Override
    public String toString() {
        return "Project{" +
                "group='" + group + '\'' +
                ", version='" + version + '\'' +
                ", license=" + license +
                ", developers=" + Arrays.toString(developers) +
                '}';
    }
}
