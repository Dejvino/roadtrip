package roadtrip.view;

import java.util.ArrayList;
import com.jme3.bounding.BoundingVolume;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class HideControl extends AbstractControl
{
    private static final float DISTANCE_HIDE = 200;
    
    private ArrayList<Spatial> children;
    private boolean hidden;
    
    private BoundingVolume prevBv;

    @Override
    public Control cloneForSpatial(Spatial spatial)
    {
        // TODO
        return null;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
        Camera cam = vp.getCamera();
        BoundingVolume bv = spatial.getWorldBound();

        if (bv == null) {
            bv = prevBv;
        } else {
            prevBv = bv;
        }

        float distance = bv.distanceTo(cam.getLocation());

        if (distance > HideControl.DISTANCE_HIDE) {
            if (!hidden) {
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).removeFromParent();
                }
                spatial.updateGeometricState();
                hidden = true;
            }
        } else {
            if (hidden) {
                for (int i = 0; i < children.size(); i++) {
                    ((Node) spatial).attachChild(children.get(i));
                }
            }
            spatial.updateGeometricState();
            hidden = false;
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
    }

    @Override
    public void setSpatial(Spatial spatial)
    {
        if (!(spatial instanceof Node)) {
            throw new IllegalArgumentException("only Node type is supported");
        }
        super.setSpatial(spatial);

        children = new ArrayList<>(((Node) spatial).getChildren());
    }
}
