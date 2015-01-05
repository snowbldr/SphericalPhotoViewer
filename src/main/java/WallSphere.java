import com.jogamp.opengl.util.texture.Texture;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

/**
 * Created with IntelliJ IDEA.
 *
 * @author robertk11
 *         Date: 12/27/14
 *         Time: 1:03 AM
 */
public class WallSphere {
    protected GLUquadric q;
    protected Texture texture;

    public WallSphere(GLU glu, Texture texture){
        q = glu.gluNewQuadric();
        glu.gluQuadricTexture(q,true);
        this.texture = texture;
    }

    public void draw(GL2 gl, GLU glu){
        gl.glPushMatrix();
        gl.glRotated(270,1,0,0);
        texture.bind(gl);
        glu.gluSphere(q,200,50,50);
        gl.glPopMatrix();
    }
}
