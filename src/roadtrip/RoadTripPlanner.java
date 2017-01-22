package roadtrip;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource.Status;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.util.Random;
import roadtrip.model.VehicleInstance;
import roadtrip.view.CompassNode;
import roadtrip.view.GameMenuNode;
import roadtrip.view.GameWorldView;
import roadtrip.view.VehicleNode;
import roadtrip.view.model.GameWorldState;
import roadtrip.view.model.Player;

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
    }
}
