package com.mrikso.anitube.app.model;

import java.util.List;

public class DubbersTeam {
    private final SimpleModel dubberTeam;
    private final List<SimpleModel> dubbers;

    public DubbersTeam(SimpleModel dubberTeam, List<SimpleModel> dubbers) {
        this.dubberTeam = dubberTeam;
        this.dubbers = dubbers;
    }

    public SimpleModel getDubberTeam() {
        return this.dubberTeam;
    }

    public List<SimpleModel> getDubbers() {
        return this.dubbers;
    }
}
