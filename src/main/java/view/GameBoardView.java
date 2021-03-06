/*
 *  Brick Destroy - A simple Arcade video game
 *   Copyright (C) 2017  Filippo Ranza
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package view;

import controller.GameBoardController;
import model.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.io.IOException;

//REFACTOR: MVC design pattern
/**
 * This is GameBoardView class which displays the game board.
 * It will also display pause menu if "esc" key is pressed.
 * MVC design pattern:
 * Basically, GameBoardView class will render the view,
 * GameBoardModel class consists of dumb entities (POJO) and
 * GameBoardController class is in charge of changing the state of GameBoardModel and notify the GameBoardView.
 * Listeners are inside GameBoardView class. When the listeners capture event,
 * they pass the events to GameBoardController class through GameBoardView class to handle action.
 */
public class GameBoardView extends JComponent implements KeyListener,MouseListener,MouseMotionListener {

    /**Text of the so-called continue button*/
    private static final String CONTINUE = "Continue";
    /**Text of the so-called restart button*/
    private static final String RESTART = "Restart";
    /**Text of the so-called exit button*/
    private static final String EXIT = "Exit";
    /**Title of pause menu*/
    private static final String PAUSE = "Pause Menu";
    /**size of text*/
    private static final int TEXT_SIZE = 30;
    /**color of menu*/
    private static final Color MENU_COLOR = new Color(0,255,0);

    /**width of game board*/
    private static final int DEF_WIDTH = 600;
    /**height of game board*/
    private static final int DEF_HEIGHT = 450;

    //REFACTOR (change to more meaningful name): change BG_COLOR to BACKGROUND_COLOR
    /**background color of game board*/
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    /**GameBoardController object*/
    private GameBoardController gameBoardController;
    /**GameBoardModel object*/
    private GameBoardModel gameBoardModel;

    /**
     * This is a constructor which is used to initialise some variables in GameBoardView class
     * as well as creating object of other classes.
     *
     * @param gameBoardModel GameBoardModel object
     */
    public GameBoardView(GameBoardModel gameBoardModel){
        super();

        gameBoardController = new GameBoardController(gameBoardModel, this);
        this.gameBoardModel = gameBoardModel;

        gameBoardModel.setStrLen(0);
        gameBoardModel.setShowPauseMenu(false);

        gameBoardModel.setMenuFont(new Font("Monospaced",Font.PLAIN,TEXT_SIZE));

        this.initialize();
        gameBoardModel.setMessage("");
        gameBoardModel.setScoreMessage("");
        gameBoardModel.setHighScoreMessage("");
        gameBoardModel.setTimeMessage("");

        gameBoardModel.setWall(new Wall(new Rectangle(0,0,DEF_WIDTH,DEF_HEIGHT),new Point(300,430)));

        gameBoardModel.setLevel(new Level(new Rectangle(0,0,DEF_WIDTH,DEF_HEIGHT),30,3, 3, gameBoardModel.getWall()));

        gameBoardModel.setGameTimer(new GameTimer());
        gameBoardModel.setAudio(new Audio());

        gameBoardModel.setDebugConsole(new DebugConsole(gameBoardModel.getOwner(),gameBoardModel.getWall(),gameBoardModel.getLevel(),this));

        //initialize the first level
        gameBoardModel.getLevel().nextLevel();

        //ADDITION: display score message, high score message and time message
        gameBoardModel.setTimer(new Timer(10, e ->{
            gameBoardModel.getWall().move();
            gameBoardModel.getWall().findImpacts();
            gameBoardModel.getGameTimer().setGaming(true);
            gameBoardModel.setMessage(String.format("Bricks: %d Balls %d",gameBoardModel.getWall().getBrickCount(),gameBoardModel.getWall().getBallCount()));
            gameBoardModel.setScoreMessage(String.format("Score: %d", gameBoardModel.getWall().getScore()));
            gameBoardModel.setHighScoreMessage("High Score Record: " + gameBoardModel.getWall().readHighScore());
            gameBoardModel.setTimeMessage(String.format("Time: %02d minute(s) %02d second(s)", gameBoardModel.getGameTimer().getMinutes(), gameBoardModel.getGameTimer().getSeconds()));
            if(gameBoardModel.getWall().isBallLost()){
                if(gameBoardModel.getWall().ballEnd()){
                    gameBoardModel.getWall().reward();
                    gameBoardModel.getWall().checkScore();
                    gameBoardModel.getWall().wallReset();
                    gameBoardModel.setHighScore(new HighScoreView(gameBoardModel.getWall()));
                    gameBoardModel.setMessage("Game over");
                    gameBoardModel.getGameTimer().resetTimer();
                    //PENALTY: play melancholy music when the user loses all the balls
                    try {
                        gameBoardModel.getAudio().playGameOver();
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                }
                gameBoardModel.getGameTimer().setGaming(true);
                gameBoardModel.getWall().ballReset();
                gameBoardModel.getTimer().stop();
            }
            else if(gameBoardModel.getWall().isDone()){
                if(gameBoardModel.getLevel().hasLevel()){
                    gameBoardModel.setMessage("Go to Next Level");
                    gameBoardModel.getTimer().stop();
                    gameBoardModel.getWall().ballReset();
                    gameBoardModel.getWall().wallReset();
                    gameBoardModel.getLevel().nextLevel();
                    /*
                    REWARD: when user successfully breaks all the bricks in 1 level,
                    play a cheering music when he / she goes to next level
                     */
                    try {
                        gameBoardModel.getAudio().playNextLevel();
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    gameBoardModel.setMessage("ALL WALLS DESTROYED");
                    gameBoardModel.getWall().reward();
                    gameBoardModel.getWall().checkScore();
                    gameBoardModel.getGameTimer().resetTimer();
                    gameBoardModel.getTimer().stop();
                    gameBoardModel.setHighScore(new HighScoreView(gameBoardModel.getWall()));
                    //REWARD: when user destroys all the wall, play a cheering music
                    try {
                        gameBoardModel.getAudio().playAllWallDestroyed();
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            repaint();
        }));

    }

    /**
     * This method is to set the preferred size of this component,
     * set this component focusable and add some listeners to this component
     * so that actions can be performed accordingly.
     */
    private void initialize(){
        this.setPreferredSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    /**
     * This method is to paint the game board.
     *
     * @param g Graphics object which is used to render output
     *          (can derive Graphics2D object from this Graphics object
     *          in order to render output)
     */
    public void paint(Graphics g){

        Graphics2D g2d = (Graphics2D) g;

        clear(g2d);

        g2d.setColor(Color.BLUE);
        g2d.drawString(gameBoardModel.getMessage(),250,225);
        g2d.drawString(gameBoardModel.getTimeMessage(),215,245);
        g2d.setColor(Color.BLACK);
        g2d.drawString(gameBoardModel.getScoreMessage(),0,390);
        g2d.drawString(gameBoardModel.getHighScoreMessage(),0,410);

        drawBall(gameBoardModel.getWall().getBall(),g2d);

        for(Brick b : gameBoardModel.getWall().getBricks())
            if(!b.isBroken())
                drawBrick(b,g2d);

        drawPlayer(gameBoardModel.getWall().getPlayer(),g2d);

        if(gameBoardModel.isShowPauseMenu())
            drawMenu(g2d);

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * This method is to set the background color of this game
     * board to white.
     *
     * @param g2d Graphics2D object
     */
    private void clear(Graphics2D g2d){
        Color tmp = g2d.getColor();
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0,0,getWidth(),getHeight());
        g2d.setColor(tmp);
    }

    /**
     * This method is to draw brick using color get from Brick class.
     *
     * @param brick Brick object
     * @param g2d Graphics2D object
     */
    private void drawBrick(Brick brick,Graphics2D g2d){
        Color tmp = g2d.getColor();

        g2d.setColor(brick.getInnerColor());
        g2d.fill(brick.getBrick());

        g2d.setColor(brick.getBorderColor());
        g2d.draw(brick.getBrick());

        g2d.setColor(tmp);
    }

    /**
     * This method is to draw ball using color and shape get from Ball class.
     *
     * @param ball Ball object
     * @param g2d Graphics2D object
     */
    private void drawBall(Ball ball, Graphics2D g2d){
        Color tmp = g2d.getColor();

        Shape s = ball.getBallFace();

        g2d.setColor(ball.getInnerColor());
        g2d.fill(s);

        g2d.setColor(ball.getBorderColor());
        g2d.draw(s);

        g2d.setColor(tmp);
    }

    /**
     * This method is to draw player using shape and color get from Player class.
     *
     * @param p Player object
     * @param g2d Graphics2D object
     */
    private void drawPlayer(Player p,Graphics2D g2d){
        Color tmp = g2d.getColor();

        Shape s = p.getPlayerFace();
        g2d.setColor(Player.INNER_COLOR);
        g2d.fill(s);

        g2d.setColor(Player.BORDER_COLOR);
        g2d.draw(s);

        g2d.setColor(tmp);
    }

    /**
     * This method is used to call methods to draw pause menu.
     *
     * @param g2d Graphics2D object
     */
    private void drawMenu(Graphics2D g2d){
        obscureGameBoard(g2d);
        drawPauseMenu(g2d);
    }

    /**
     * This method is to set the game board to become transparent when
     * the pause menu is displayed.
     *
     * @param g2d Graphics2D object
     */
    private void obscureGameBoard(Graphics2D g2d){

        Composite tmp = g2d.getComposite();
        Color tmpColor = g2d.getColor();

        //set transparency
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.55f);
        g2d.setComposite(ac);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,DEF_WIDTH,DEF_HEIGHT);

        g2d.setComposite(tmp);
        g2d.setColor(tmpColor);
    }

    /**
     *This method is to draw pause menu which consists of some texts and
     * place them accordingly in the location calculated.
     *
     * @param g2d Graphics2D object
     */
    private void drawPauseMenu(Graphics2D g2d){
        Font tmpFont = g2d.getFont();
        Color tmpColor = g2d.getColor();

        g2d.setFont(gameBoardModel.getMenuFont());
        g2d.setColor(MENU_COLOR);

        if(gameBoardModel.getStrLen() == 0){
            FontRenderContext frc = g2d.getFontRenderContext();
            gameBoardModel.setStrLen(gameBoardModel.getMenuFont().getStringBounds(PAUSE,frc).getBounds().width);
        }

        int x = (this.getWidth() - gameBoardModel.getStrLen()) / 2;
        int y = this.getHeight() / 10;

        g2d.drawString(PAUSE,x,y);

        x = this.getWidth() / 8;
        y = this.getHeight() / 4;

        if(gameBoardModel.getContinueButtonRect() == null){
            FontRenderContext frc = g2d.getFontRenderContext();
            gameBoardModel.setContinueButtonRect(gameBoardModel.getMenuFont().getStringBounds(CONTINUE,frc).getBounds());
            gameBoardModel.getContinueButtonRect().setLocation(x,y-gameBoardModel.getContinueButtonRect().height);
        }

        g2d.drawString(CONTINUE,x,y);

        y *= 2;

        if(gameBoardModel.getRestartButtonRect() == null){
            gameBoardModel.setRestartButtonRect((Rectangle) gameBoardModel.getContinueButtonRect().clone());
            gameBoardModel.getRestartButtonRect().setLocation(x,y-gameBoardModel.getRestartButtonRect().height);
        }

        g2d.drawString(RESTART,x,y);

        y *= 3.0/2;

        if(gameBoardModel.getExitButtonRect() == null){
            gameBoardModel.setExitButtonRect((Rectangle) gameBoardModel.getContinueButtonRect().clone());
            gameBoardModel.getExitButtonRect().setLocation(x,y-gameBoardModel.getExitButtonRect().height);
        }

        g2d.drawString(EXIT,x,y);

        g2d.setFont(tmpFont);
        g2d.setColor(tmpColor);
    }

    /**
     * This method is used to define some actions performed if
     * a key has been typed but it does nothing in this class
     * as no implementation is provided.
     *
     * @param keyEvent a low-level event which indicates that a keystroke occurred in a component
     *                 (generated by a component object (such as a text field) when a key is pressed, released, or typed)
     */
    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    /**
     * This method checks if a key is pressed and then passes the keyEvent to GameBoardController
     * to update the view.
     * @param keyEvent a low-level event which indicates that a keystroke occurred in a component
     *                 (generated by a component object (such as a text field) when a key is pressed, released, or typed)
     */
    @Override
    public void keyPressed(KeyEvent keyEvent) {
        gameBoardController.checkKeyPressed(keyEvent);
    }

    /**
     * This method stops the motion of the player bar when a key is released.
     *
     * @param keyEvent a low-level event which indicates that a keystroke occurred in a component
     *                 (generated by a component object (such as a text field) when a key is pressed, released, or typed)
     */
    @Override
    public void keyReleased(KeyEvent keyEvent) {
        gameBoardModel.getWall().getPlayer().stop();
    }

    /**
     * This method checks if the mouse button has been clicked (pressed and released) and then passes the mouseEvent to GameBoardController
     * to update the view.
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        gameBoardController.checkMouseClicked(mouseEvent);
    }

    /**
     * This method is used to define some actions performed when
     * the mouse button has been pressed on this component but
     * it does nothing in this class as no implementation is provided.
     *
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    /**
     * This method is used to define some actions performed when
     * the mouse button has been released on this component but
     * it does nothing in this class as no implementation is provided.
     *
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    /**
     * This method is used to define some actions performed when
     * the mouse enters this component but
     * it does nothing in this class as no implementation is provided.
     *
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    /**
     * This method is used to define some actions performed when
     * the mouse exits this component but
     * it does nothing in this class as no implementation is provided.
     *
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    /**
     * This method is used to define some actions performed when
     * a mouse button is pressed on this component and then dragged but
     * it does nothing in this class as no implementation is provided.
     *
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    /**
     * This method checks if the mouse cursor has been moved onto this component but no buttons have been pushed,
     * and then passes the mouseEvent to GameBoardController to update the view.
     * @param mouseEvent a low-level event which indicates that a mouse action occurred in a component
     *                   (generated when a mouse button is pressed, released or clicked (pressed and released),
     *                   or when the mouse cursor enters or exits the unobscured part of component's geometry)
     */
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        gameBoardController.checkMouseMoved(mouseEvent);
    }

    /**
     * This method is to set hand cursor.
     */
    public void setHandCursor(){
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * This method is to set default mouse cursor.
     */
    public void setDefaultCursor(){
        this.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * This method is to stop timer, display a "Focus Lost" message and
     * repaint the game board.
     */
    public void onLostFocus(){
        gameBoardModel.getTimer().stop();
        gameBoardModel.getGameTimer().setGaming(false);
        gameBoardModel.setMessage("Focus Lost");
        repaintGameBoard();
    }

    /**
     * This method is to repaint the game board.
     */
    public void repaintGameBoard(){
        repaint();
    }

    /**
     * This method is to get game board model (mainly used for testing purpose).
     * @return GameBoardModel object
     */
    public GameBoardModel getGameBoardModel() {
        return gameBoardModel;
    }
}