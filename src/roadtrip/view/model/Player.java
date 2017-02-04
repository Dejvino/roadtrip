package roadtrip.view.model;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import roadtrip.view.VehicleNode;

/**
 * Created by dejvino on 14.01.2017.
 */
public class Player {
    public Node node;
    public Node headNode;
    public BetterCharacterControl characterControl;
    public Vector3f jumpForce = new Vector3f(0, 3000, 0);
    public Vector3f walkDir = new Vector3f();
    public VehicleNode vehicleNode;
}
