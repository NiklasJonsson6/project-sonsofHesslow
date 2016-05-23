package com.sonsofhesslow.games.risk.graphics;

import com.sonsofhesslow.games.risk.graphics.Geometry.BeizierPath;

public class SVGPath {
    public SVGPath(BeizierPath path, boolean isDashed, boolean isRegion, boolean isContinent) {
        this.path = path;
        this.isDashed = isDashed;
        this.isRegion = isRegion;
        this.isContinent = isContinent;
    }

    final BeizierPath path;
    final boolean isDashed;
    final boolean isRegion;
    final boolean isContinent;
}
