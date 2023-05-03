package com.mrikso.anitube.app.model;

import java.io.Serializable;

public abstract class BaseModel implements Serializable {
    private String id;
    private String name;

    public BaseModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BaseModel[id=" + id + ", name=" + name + "]";
    }
}
