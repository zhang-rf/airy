package me.rfprojects.airy;

import java.io.Serializable;

public class Load implements Serializable {

    private byte[] load;

    public Load() {
    }

    public Load(byte[] load) {
        this.load = load;
    }

    public byte[] getLoad() {
        return load;
    }

    public Load setLoad(byte[] load) {
        this.load = load;
        return this;
    }
}
