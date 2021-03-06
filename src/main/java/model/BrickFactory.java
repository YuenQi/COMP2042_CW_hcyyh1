package model;

import java.awt.*;

//REFACTOR: Use factory design pattern to create brick
/**
 * BrickFactory class is used to create different types of Brick objects.
 * It abstracts the brick object instantiation process.
 */
public class BrickFactory {

    /**
     * This method is to return brick object to the calling method.
     *
     * @param brickType type of brick
     * @param point point (x-coordinate and y-coordinate) to draw the brick
     * @param size size (width and height) of brick
     * @return Brick object created
     */
    public Brick getBrick(String brickType, Point point, Dimension size){
        if (brickType == null)
            return null;
        if(brickType.equalsIgnoreCase("CLAY"))
            return new ClayBrick(point, size);
        else if(brickType.equalsIgnoreCase("STEEL"))
            return new SteelBrick(point, size);
        else if(brickType.equalsIgnoreCase("CEMENT"))
            return new CementBrick(point, size);
        else if(brickType.equalsIgnoreCase("FAST_BRICK"))
            return new FastBrick(point, size);
        else if(brickType.equalsIgnoreCase("SLOW_BRICK"))
            return new SlowBrick(point, size);
        else if(brickType.equalsIgnoreCase("SPECIAL"))
            return new SpecialBrick(point, size);

        return null;
    }
}