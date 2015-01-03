import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import java.awt.*;

public class Main {


    public static void main(String[] args) {

        GLCanvas canvas = new GLCanvas();
        MainWindow mainWindow = new MainWindow(canvas);
        //make a canvas and set it up
        canvas = new GLCanvas(new GLCapabilities(GLProfile.getDefault()));
        //get input focus
        canvas.setFocusable(true);
        canvas.requestFocus();
        Dimension dimension = new Dimension(mainWindow.windowWidth, mainWindow.windowHeight);
        canvas.setSize(dimension);
        //add ourselves as listeners
        canvas.addGLEventListener(mainWindow);
        canvas.addKeyListener(mainWindow);
        //start an animator thread that will drive our animations
        FPSAnimator animator = new FPSAnimator(canvas,60);
        animator.start();
        mainWindow.add(canvas);
        mainWindow.setVisible(true);

    }
}
