package me.kivanova;

import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The Breakout game.
 */
public class Breakout extends WindowProgram {
    /* Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /* Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /* Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /* Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /* Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /* Separation between bricks */
    private static final int BRICK_SEP = 4;

    /* Width of a brick */
    private static final int BRICK_WIDTH =
            (APPLICATION_WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /* Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /* Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /* Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /* Number of turns */
    private static final int NTURNS = 3;

    /* Gravitational acceleration in this game. */
    private static final double GRAVITY = 0.01;

    /* Velocity y-axis. */
    private static final double VELOCITY_Y = 3;

    /* The pause in animation. */
    private static final int PAUSE_TIME = 10;

    /* The Paddle object. */
    private GRect paddle;

    /* The Ball object. */
    private GOval ball;

    public void run() {
        addMouseListeners();
        drawPlayingField();
        ballBehaviour();
    }

    /**
     * Draw the playing field: the paddle, bricks, and ball.
     */
    private void drawPlayingField() {
        drawPaddle();
        drawBall();
        drawBricksSpace();
    }

    /**
     * Draw the paddle depending on the paddle y offset, paddle sizes.
     */
    private void drawPaddle() {
        /* set start X coord of the paddle in the center. */
        double startX = (getWidth() - PADDLE_WIDTH) / 2.0;
        /* calculate the start Y coord of the paddle depending on the paddle Y offset. */
        double startY = getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

        paddle = new GRect(startX, startY, PADDLE_WIDTH, PADDLE_HEIGHT);
        println("Reassign paddle " + paddle.hashCode());
        paddle.setFilled(true);
        paddle.setColor(Color.BLACK);
        add(paddle);
    }

    /**
     * Draws the ball.
     */
    private void drawBall() {
        /* set start X coord of the ball in the center. */
        double startX = getWidth() / 2.0 - BALL_RADIUS;
        /* set start Y coord of the ball in the center.  */
        double startY = getHeight() / 2.0 - BALL_RADIUS;

        ball = new GOval(startX, startY, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
        ball.setColor(Color.BLACK);
        ball.setFilled(true);
        add(ball);
    }

    /**
     * The ball behaviour in game.
     */
    private void ballBehaviour() {
        /* If the user clicks on the mouse, the game will start. */
        waitForClick();

        /* The velocity start of the ball. */
        double vx = calculateRandomVX();
        double vy = VELOCITY_Y;

        /* The lives count until finish the game. */
        int livesCount = NTURNS;

        /* The bricks count in the bricks spase. */
        int bricksCount = NBRICK_ROWS * NBRICKS_PER_ROW;

        /* Start animation. */
        while (true) {

            /* The user lost one of lives. */
            if (isBottomWallCollision()) {
                /* Reduce the user's lives count. */
                livesCount--;
                /* If the lives count equals zero, the gave will over. */
                if (livesCount == 0) {
                    gameOver(livesCount);
                    return;
                }
                /* Set the ball on the start position. */
                vy = setStartPosition();
                waitForClick();
                continue;
            }

            /* Find collision. */
            Collision collision = findCollision(vx, vy);
            /* If the collision is not null, the velocity will change. */
            /* If the collision is the brick, the bricks count will change. */
            if (collision != null) {
                ParamChanges paramChanges = new ParamChanges(new GPoint(vx, vy), bricksCount);
                switch (collision.type) {
                    case WALL:
                        paramChanges = onWallCollision(collision, vx, vy);
                        break;
                    case BRICK:
                        paramChanges = onBrickCollision(collision, vx, vy, bricksCount);
                        break;
                    case PADDLE:
                        paramChanges = onPaddleCollision(collision, vx, vy);
                        break;
                }
                vx = paramChanges.gPoint.getX();
                vy = paramChanges.gPoint.getY();
                bricksCount = (paramChanges.bricksCount != null) ? paramChanges.bricksCount : bricksCount;
            }

            /* The user won the game. */
            if (bricksCount == 0) {
                gameOver(livesCount);
                return;
            }

            /* Move the ball with velocity x,y. */
            ball.move(vx, vy);
            /* Change the ball velocity depending on the gravity. */
            vy += GRAVITY;

            pause(PAUSE_TIME);
        }
    }

    /**
     * Set the ball on the start position.
     *
     * @return vy The start velocity y.
     */
    private double setStartPosition() {
        /* Set the ball start position. */
        ball.setLocation((getWidth() - ball.getWidth()) / 2.0, (getHeight() - ball.getHeight()) / 2.0);
        /* Set vy start game value. */
        return VELOCITY_Y;
    }

    /**
     * The ball reached the bottom wall.
     *
     * @return boolean Is the bottom wall collision.
     */
    private boolean isBottomWallCollision() {
        return ball.getY() > getHeight() - ball.getHeight();
    }

    /**
     * The ball reached the paddle.
     *
     * @param collision The collision object.
     * @param vx        The velocity x.
     * @param vy        The velocity y.
     * @return ParamChanges The object keeps the velocity x,y  and the bricksCount.
     */
    private ParamChanges onPaddleCollision(Collision collision, double vx, double vy) {
        if (collision.isHorizontal) {
            vx = -vx;
        } else {
            vy = -vy;
        }
        return new ParamChanges(new GPoint(vx, vy), null);
    }

    /**
     * The ball reached the bricks area.
     *
     * @param collision   The collision object.
     * @param vx          The velocity x.
     * @param vy          The velocity y.
     * @param bricksCount The bricks count in the bricks spase
     * @return ParamChanges The object keeps the velocity x,y  and the bricksCount.
     */
    private ParamChanges onBrickCollision(Collision collision, double vx, double vy, int bricksCount) {
        if (collision.isHorizontal) {
            /* The ball reached the brick, horizontal bounce. */
            vx = -vx;
            /* remove the brick. */
            remove(collision.object);
            /* Decrease the bricks count. */
            bricksCount--;
        } else {
            /* The ball reached the brick, vertical bounce. */
            /* remove the brick. */
            remove(collision.object);
            /* Decrease the bricks count. */
            bricksCount--;
            vy = -vy;
        }
        return new ParamChanges(new GPoint(vx, vy), bricksCount);
    }

    /**
     * If the ball reached the left or the right wall, the collision is horizontal.
     * If the ball reached the top wall, the collision is vertical.
     *
     * @param collision The collision object.
     * @param vx        The velocity x.
     * @param vy        The velocity y.
     * @return ParamChanges The object keeps the velocity x,y  and the bricksCount.
     */
    private ParamChanges onWallCollision(Collision collision, double vx, double vy) {
        if (collision.isHorizontal) {
            vx = -vx;
        } else {
            vy = -vy;
        }
        return new ParamChanges(new GPoint(vx, vy), null);
    }

    /**
     * This method verifies the collision with
     * the wall, the paddle, and the bricks space.
     *
     * @param vx The velocity x.
     * @param vy The velocity y.
     * @return The Collision Object.
     */
    private Collision findCollision(double vx, double vy) {
        /* Calculate the points offset. The offset is 1/6. */
        double pointsOffset = ball.getHeight() / 6.0;

        /* Create the top and bottom points coordinates list. */
        List<GPoint> topBottomPoints = getTopBottomPoints(pointsOffset);
        /* Create the right and left points coordinates list. */
        List<GPoint> leftRightPoints = getLeftRightPoints(pointsOffset);

        GObject collider = null;
        boolean isHorizontal = false;

        /* Try to find vertical collision. */
        for (GPoint point : topBottomPoints) {
            collider = getElementAt(point);
            if (collider != null) {
                break;
            }
        }

        /* Vertical not found. Try to find horizontal. */
        if (collider == null) {
            for (GPoint point : leftRightPoints) {
                collider = getElementAt(point);
                if (collider != null) {
                    isHorizontal = true;
                    break;
                }
            }
        }

        /* Verify the collision type */
        if (isPaddleReached(collider, vy)) {
            return new Collision(GameObjectType.PADDLE, collider, isHorizontal);
        } else if (isBrickReached(collider)) {
            return new Collision(GameObjectType.BRICK, collider, isHorizontal);
        } else if (isRightOrLeftWallReached(vx)) {
            return new Collision(GameObjectType.WALL, null, true);
        } else if (isTopReached(vy)) {
            return new Collision(GameObjectType.WALL, null, false);
        } else {
            return null;
        }
    }

    /**
     * Get the points of the right and left collisions.
     *
     * @param pointsOffset The points offset.
     * @return List<GPoint> The list contains right and left points.
     */
    private List<GPoint> getLeftRightPoints(double pointsOffset) {
        List<GPoint> leftRightPoints = new ArrayList<>();
        /* The centerLeft. */
        leftRightPoints.add(new GPoint(ball.getX(), ball.getY() + pointsOffset));
        leftRightPoints.add(new GPoint(ball.getX(), ball.getY() + ball.getHeight() - pointsOffset));
        /* The centerRight. */
        leftRightPoints.add(new GPoint(ball.getX() + ball.getWidth(), ball.getY() + pointsOffset));
        leftRightPoints.add(new GPoint(ball.getX() + ball.getWidth(), ball.getY() + ball.getHeight() - pointsOffset));
        return leftRightPoints;
    }

    /**
     * Get the points of the top and bottom collisions.
     *
     * @param pointsOffset The points offset.
     * @return List<GPoint> The list contains top and bottom points.
     */
    private List<GPoint> getTopBottomPoints(double pointsOffset) {
        List<GPoint> topBottomPoints = new ArrayList<>();
        /* Exclude the vertical collision at the horizontal collision from the paddle. */
        if (ball.getY() <= paddle.getY()) {
            /* The topLeft. */
            topBottomPoints.add(new GPoint(ball.getX() + pointsOffset, ball.getY()));
            /* The topRight. */
            topBottomPoints.add(new GPoint(ball.getX() + ball.getWidth() - pointsOffset, ball.getY()));
            /* The bottomLeft. */
            topBottomPoints.add(new GPoint(ball.getX() + pointsOffset, ball.getY() + ball.getHeight()));
            /* The bottomRight. */
            topBottomPoints.add(new GPoint(ball.getX() + ball.getWidth() - pointsOffset, ball.getY() + ball.getHeight()));

        }
        return topBottomPoints;
    }

    /**
     * The ball reached the bricks.
     *
     * @param collider The collision object.
     * @return boolean s reached.
     */
    private boolean isBrickReached(GObject collider) {
        return collider != null && collider != paddle && collider != ball;
    }

    /**
     * The ball reached the paddle.
     *
     * @param collider The collision object.
     * @param vy       The velocity y.
     * @return boolean is reached.
     */
    private boolean isPaddleReached(GObject collider, double vy) {
        /* The ball is on the paddle. */
        boolean isBallOnPaddle = ball.getY() <= (getHeight() - PADDLE_Y_OFFSET);
        /* The validation from velocity prevents the ball from sticking. */
        return collider == paddle && vy > 0 && isBallOnPaddle;
    }

    /**
     * The ball reached the top wall.
     *
     * @param vy The velocity y.
     * @return boolean is reached.
     */
    private boolean isTopReached(double vy) {
        /* The validation from velocity prevents the ball from sticking. */
        return ball.getY() < 0 && vy < 0;
    }

    /**
     * The ball reached the right or left wall.
     *
     * @param vx The velocity x.
     * @return boolean is reached.
     */
    private boolean isRightOrLeftWallReached(double vx) {
        /* The ball reached the left wall. */
        /* The validation from velocity prevents the ball from sticking. */
        boolean conditionIsLeftWall = ball.getX() < 0 && vx < 0;
        /* The ball reached the right wall. */
        boolean conditionIsRightWall = ball.getX() > (getWidth() - ball.getHeight()) && vx > 0;

        return (conditionIsRightWall || conditionIsLeftWall);
    }

    /**
     * The game is over.
     *
     * @param livesCount The lives count.
     */
    private void gameOver(int livesCount) {
        removeAll();

        GLabel label = new GLabel("");
        label.setColor(Color.BLACK);
        label.setFont(new Font("Lucida Bright", Font.BOLD, 24));

        if (livesCount > 0) {
            /* There are no bricks on the playing field.*/
            label.setLabel("You won!");
        } else {
            /* The lives count equals zero. */
            label.setLabel("You lost.");
        }

        /* Set the center location for the label. */
        label.setLocation((
                        getWidth() - label.getWidth()) / 2.0,
                (getHeight() - label.getHeight()) / 2.0);
        add(label);
    }

    /**
     * Calculate the random velocity X.
     *
     * @return vx The velocity X.
     */
    private double calculateRandomVX() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        /* A random value of type double in the range 1.0 to 3.0 with a probability of 50%. */
        double vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
        return vx;
    }

    /**
     * Draw the bricks area depending on
     * the bricks sizes, the brick count and the offset.
     */
    private void drawBricksSpace() {
        /* The area width of all bricks in the canvas. */
        double bricksWidth = NBRICKS_PER_ROW * (BRICK_WIDTH + BRICK_SEP) - BRICK_SEP;
        /* The brick area start X coordinates. */
        double bricksAreaStartX = (getWidth() - bricksWidth) / 2.0;

        double widthWithSep = BRICK_WIDTH + BRICK_SEP;
        double heightWithSep = BRICK_HEIGHT + BRICK_SEP;

        for (int i = 0; i < NBRICKS_PER_ROW; i++) {
            for (int j = 0; j < NBRICK_ROWS; j++) {
                drawBrick(bricksAreaStartX + widthWithSep * i,
                        BRICK_Y_OFFSET + heightWithSep * j, j);
            }
        }
    }

    /**
     * Draw the brick depending on
     * the bricks sizes and the brick color.
     *
     * @param x       The x coordinate of the upper-left corner of the brick.
     * @param y       The y coordinate of the upper-left corner of the brick.
     * @param idColor The brick color number.
     */
    private void drawBrick(double x, double y, int idColor) {
        GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);

        brick.setColor(getBrickColor(idColor));
        brick.setFilled(true);
        add(brick);
    }

    /**
     * Get brick color depending on the row number.
     *
     * @param idColor The brick color number.
     * @return Color The brick color.
     */
    private Color getBrickColor(int idColor) {
        Color brickColor;
        switch (idColor) {
            case 0:
            case 1:
                brickColor = Color.RED;
                break;
            case 2:
            case 3:
                brickColor = Color.ORANGE;
                break;
            case 4:
            case 5:
                brickColor = Color.YELLOW;
                break;
            case 6:
            case 7:
                brickColor = Color.GREEN;
                break;
            case 8:
            case 9:
                brickColor = Color.CYAN;
                break;
            default:
                brickColor = Color.BLACK;
        }

        return brickColor;
    }

    /**
     * Set the paddle to follow the mouse.
     * Set the cursor in the center of the paddle.
     *
     * @param e The MouseEvent.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        /* the center coordinate X of the paddle. */
        /* the min coord X for the paddle. */
        double centerXOfPaddle = PADDLE_WIDTH / 2.0;

        /* the max coord X for the paddle. */
        double maxCoordX = getWidth() - centerXOfPaddle;

        /* set cursor in the center of the paddle. */
        if (e.getX() >= centerXOfPaddle && e.getX() <= maxCoordX) {
            paddle.setLocation(e.getX() - centerXOfPaddle, getHeight() - PADDLE_Y_OFFSET - paddle.getHeight());
        }
    }
}
