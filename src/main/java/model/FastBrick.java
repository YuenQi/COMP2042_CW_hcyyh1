package model;

import java.awt.*;
import java.awt.Point;
import java.awt.geom.Point2D;

//ADDDITION: Additional playble level
/**
 * This is FastBrick class which inherits Brick class.
 * Fast brick means if the ball hits the brick, the speed of the ball
 * will become faster.
 * This FastBrick is for additional playable level.
 * FastBrick will change the speed of ball in x direction to 4 and
 * speed of ball in y direction to 3 to make the ball moves faster when
 * the ball hits the brick.
 */
public class FastBrick extends Brick {

    /**name of brick*/
    private static final String NAME = "Fast Brick";
    /**inner color of brick*/
    private static final Color INNER_COLOR = new Color(0x8bd2d9);
    /**border color of brick*/
    private static final Color BORDER_COLOR = new Color(0x1815bd);
    /**full strength of fast brick*/
    private static final int FAST_BRICK_STRENGTH = 1;

    /**
     * This is a constructor which initialises variables in FastBrick class.
     *
     * @param point point (x-coordinate and y-coordinate) to draw the brick
     * @param size size (width and height) of brick
     */
    public FastBrick(Point point, Dimension size){
        super(NAME,point,size, BORDER_COLOR, INNER_COLOR,FAST_BRICK_STRENGTH);
    }

    /**
     * This method is to set impact on brick.
     *
     * @param point point at which the ball hits the brick
     * @param direction direction of crack of brick (if there is crack)
     * @return state of brick (broken / not broken)
     */
    @Override
    public boolean setImpact(Point2D point , int direction){
        if(super.isBroken())
            return false;
        else {
            impact();
            makeBallSpeedFaster();
        }
        return super.isBroken();
    }

    /**
     * This method will set the speed of the ball
     * in x direction to 4 and the speed of the ball
     * in y direction to 3 which will make the ball
     * move faster when the ball hits the blue color brick
     */
    private void makeBallSpeedFaster() {
        if (super.isBroken()){
            Level.getWall().setBallXSpeed(4);
            Level.getWall().setBallYSpeed(3);
        }
    }

    /**
     * This method is to make a brick and return the brick
     * that has been created using the specified point and
     * size of type Shape to the calling method.
     *
     * @param position point (x-coordinate and y-coordinate) to draw the brick
     * @param size size (width and height) of brick
     * @return brick that has been created using the specified point and size of type Shape
     */
    @Override
    protected Shape makeBrickFace(Point position, Dimension size) {
        return new Rectangle(position,size);
    }

    /**
     * This method is to return a brick to the calling method.
     *
     * @return brick of type Shape
     */
    @Override
    public Shape getBrick() {
        return super.brickFace;
    }
}