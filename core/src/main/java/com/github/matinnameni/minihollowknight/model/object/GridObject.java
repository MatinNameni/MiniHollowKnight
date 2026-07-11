package com.github.matinnameni.minihollowknight.model.object;

import com.badlogic.gdx.math.Rectangle;

public class GridObject extends Rectangle {
    public boolean isDeadly;
    public boolean canPogo;
    public String transferTo;

    public GridObject(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public GridObject(Rectangle rect) {
        super(rect);
    }

    public GridObject(float x, float y, float width, float height, boolean isDeadly) {
        super(x, y, width, height);
        this.isDeadly = isDeadly;
    }

    public GridObject(Rectangle rect, boolean isDeadly) {
        super(rect);
        this.isDeadly = isDeadly;
    }

    public GridObject(float x, float y, float width, float height, boolean isDeadly, boolean canPogo) {
        super(x, y, width, height);
        this.isDeadly = isDeadly;
        this.canPogo = canPogo;
    }

    public GridObject(Rectangle rect, boolean isDeadly, boolean canPogo) {
        super(rect);
        this.isDeadly = isDeadly;
        this.canPogo = canPogo;
    }

    public GridObject(float x, float y, float width, float height, boolean isDeadly, boolean canPogo, String transferTo) {
        super(x, y, width, height);
        this.isDeadly = isDeadly;
        this.canPogo = canPogo;
        this.transferTo = transferTo;
    }
}
