package roadtrip;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.*;
import roadtrip.view.GameWorldView;
import roadtrip.view.model.GameWorldState;

/**
 *
 * @author dejvino
 */
public class RoadTripPlanner extends SimpleApplication {

    public static void main(String[] args) {
        RoadTripPlanner app = new RoadTripPlanner();
        app.start();
    }

    public static boolean DEBUG = /*false;/*/true;/**/

    protected BulletAppState bulletAppState;
    private GameWorldState gameWorldState;
    private GameWorldView gameWorldView;

    protected PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }
    
    @Override
    public void simpleInitApp()
    {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        bulletAppState.setDebugEnabled(DEBUG);
        
        gameWorldState = new GameWorldState(1L);
        gameWorldView = GameWorldView.create(gameWorldState, assetManager, cam, rootNode, getPhysicsSpace());
        
        flyCam.setMoveSpeed(300f);
        cam.setLocation(new Vector3f(0, 200f, 0));
        cam.setFrustumNear(1f);
        cam.setFrustumFar(3000f);
    }
}
