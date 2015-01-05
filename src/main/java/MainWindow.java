import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author robertk11
 *         Date: 12/27/14
 *         Time: 1:01 AM
 */
public class MainWindow extends JFrame implements GLEventListener, KeyListener{


    //giant pile of locals
    private GLU glu;
    private GLCanvas canvas;
    protected int windowHeight =900;
    protected int windowWidth =900;
    //this determines how big our room is. a wall will be this many units long and wide, but only half as many tall
    private final static double LOOK_AT_DIST = 100;
    private final static double ROTATE_SPEED = 2;
    private double playerX, playerY, playerZ;
    private double lookX, lookY, lookZ;
    //how much x and z change when the button is pressed, it will be the cosin and sin of our viewangle
    private double stepX, stepZ;
    private double viewAngle;
    private float[] light0Pos;
    //this is our key buffer, it keeps track of which keys are currently pressed down
    private Map<Integer,Boolean> keys = new HashMap<>();
    private Texture currentTexture;
    private WallSphere wallSphere;

    public MainWindow(GLCanvas canvas){
        this.canvas = canvas;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, windowWidth, windowHeight);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        //get a handle on our gl and gl utils
        GL2 gl = glAutoDrawable.getGL().getGL2();
        glu = new GLU();
        //clear to black
        gl.glClearColor(0, 0, 0, 0);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_DEPTH_TEST);
        //let there be light!
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        //enable light shading of stuff using glColor instead of having to define materials all over the place
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        float[] white = new float[]{1,1,1,1};
        gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_DIFFUSE, white,0);
        gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_SPECULAR, white, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT,white,0);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        glu.gluPerspective(55, windowWidth / windowHeight, 0.3, 300);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        //place a directional light directly above everything, this is the sun
        this.light0Pos = new float[]{0,1,0,0};
        gl.glLoadIdentity();
        initTextures();
        initCamera();
        initKeys();
        wallSphere = new WallSphere(glu,currentTexture);
    }

    /**
     * This method loads up all of the textures we need to display our game
     */
    private void initTextures() {
        try {
            currentTexture = TextureIO.newTexture(new ByteArrayInputStream(Files.readAllBytes((new File("/home/robertk11/trippix/spheres/burg.jpg")).toPath())),true,"jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This initializes our key map that lets us know when the player pushes and releases the keys that do stuff
     * having these means that they can press more than one button at a time, and makes continuous motion work (without
     * relying on the auto repeat from the operating system)
     */
    private void initKeys() {
        keys.put(KeyEvent.VK_SPACE,false);
        keys.put(KeyEvent.VK_LEFT,false);
        keys.put(KeyEvent.VK_RIGHT,false);
        keys.put(KeyEvent.VK_UP,false);
        keys.put(KeyEvent.VK_DOWN,false);
        keys.put(KeyEvent.VK_S,false);
        keys.put(KeyEvent.VK_W,false);
        keys.put(KeyEvent.VK_D,false);
        keys.put(KeyEvent.VK_A,false);
        keys.put(KeyEvent.VK_R,false);
        keys.put(KeyEvent.VK_CONTROL,false);
    }

    /**
     * Set up the initial position and direction of the camera
     */
    private void initCamera(){
        //start the user in the corner, facing the other corner
        playerX = 0; playerY = 6; playerZ = 0;
        viewAngle = 45;
        stepX = Math.cos( Math.toRadians(viewAngle));
        stepZ = Math.sin( Math.toRadians(viewAngle));
        lookX = playerX + (LOOK_AT_DIST * stepX);
        lookY = 15;
        lookZ = playerZ + (LOOK_AT_DIST * stepZ);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    /**
     * This method does all the work when the player presses keyboard keys
     */
    private void processKeys(){
        //if we're left or right key, rotate the viewangle left or right
        if(keys.get(KeyEvent.VK_LEFT)||keys.get(KeyEvent.VK_A)){
            viewAngle -= ROTATE_SPEED;
            stepX = Math.cos(Math.toRadians(viewAngle));
            stepZ = Math.sin(Math.toRadians(viewAngle));
            //make this an else if so they can't push both at the same time
        } else if(keys.get(KeyEvent.VK_RIGHT)||keys.get(KeyEvent.VK_D)){
            viewAngle += ROTATE_SPEED;
            stepX = Math.cos(Math.toRadians(viewAngle));
            stepZ = Math.sin(Math.toRadians(viewAngle));
        }

        if(keys.get(KeyEvent.VK_UP)){
            lookY += ROTATE_SPEED;
        } else if(keys.get(KeyEvent.VK_DOWN)){
            lookY -= ROTATE_SPEED;
        }

        //new place to look
        lookX = playerX + (stepX * LOOK_AT_DIST);
        lookZ = playerZ + (stepZ * LOOK_AT_DIST);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //set the key to true, until it's released
        keys.put(e.getKeyCode(),true);
    }
    @Override
    public void keyReleased(KeyEvent e) {
        //set the key to false now that it's released
        keys.put(e.getKeyCode(),false);
    }


    /**
     * This is the main method for gl to draw our stuff. Most of the interesting stuff is in the method calls
     * @param glAutoDrawable The thing, that does the stuff
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0Pos, 0);
        //do stuff if keys are pressed
        processKeys();
        //look at something
        glu.gluLookAt(playerX, playerY, playerZ,
                lookX, lookY, lookZ, 0,1,0);
        wallSphere.draw(gl,glu);
    }

    /**
     * This method will make it so that when the window is reshaped, our stuff will keep the correct aspect ratio
     * and such
     */
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        windowHeight = height;
        windowWidth = width;
        canvas.setSize(new Dimension(windowWidth,windowHeight));
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        //reset our perspective based on the new window dimensions
        glu.gluPerspective(55, (double) windowWidth / (double) windowHeight, 0.3, 300);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
