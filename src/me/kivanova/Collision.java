package me.kivanova;

import acm.graphics.GObject;

/**
 * This class keeps the GameObjectTyp, the object and the isHorizontal.
 * Return a collision object or null if it's a wall.
 */
class Collision {
    /* The Game object type: wall, brick, paddle. */
    GameObjectType type;
    /* The object that collided with the ball. */
    GObject object;
    /* The type collision: horizontal is true, vertical is false.*/
    boolean isHorizontal;

    Collision(GameObjectType type, GObject object, boolean isHorizontal) {
        this.type = type;
        this.object = object;
        this.isHorizontal = isHorizontal;
    }

    /* For testing the collision. */
    @Override
    public String toString() {
        return "Collision{" +
                "type=" + type +
                ", isHorizontal=" + isHorizontal +
                ", object=" + object +
                '}';
    }
}
