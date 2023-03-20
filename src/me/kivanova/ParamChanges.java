package me.kivanova;

import acm.graphics.GPoint;

/**
 * This class keeps the Point Object, the bricks count.
 * Return the ParamChanges object.
 */
 class ParamChanges {
    /* The object keeps the velocity x,y. */
    GPoint gPoint;
    /* The bricks count of the bricks space.*/
    Integer bricksCount;

    public ParamChanges(GPoint gPoint, Integer bricksCount) {
        this.gPoint = gPoint;
        this.bricksCount = bricksCount;
    }
}
