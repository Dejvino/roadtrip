package roadtrip.view;

import java.util.ArrayList;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class HideControl extends AbstractControl
{
    private final float distanceToHide;
    
    private ArrayList<Spatial> children;
    private boolean hidden;
    
    private BoundingVolume prevBv;
    
    private TargetProvider targetProvider;

    public HideControl(float distanceToHide) {
        this(distanceToHide, null);
    }
    
    public HideControl(float distanceToHide, TargetProvider targetProvider) {
        this.distanceToHide = distanceToHide;
        this.targetProvider = targetProvider;
    }

    public void setTargetProvider(TargetProvider targetProvider)
    {
        this.targetProvider = targetProvider;
    }
    
    @Override
    public Control cloneForSpatial(Spatial spatial)
    {
        // TODO
        return null;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
        if (targetProvider == null) {
            final Camera cam = vp.getCamera();
            targetProvider = new TargetProvider() {

                @Override
                public Vector3f getTarget() {
                    return cam.getLocation();
                }
            };
        }
        
        /*BoundingVolume bv = spatial.getWorldBound();

        if (bv == null) {
            bv = prevBv;
        } else {
            prevBv = bv;
        }

        float distance = bv.distanceTo(targetProvider.getTarget());*/
        float distance = spatial.getWorldTranslation().distance(targetProvider.getTarget());

        if (distance > distanceToHide) {
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
    
    public interface TargetProvider
    {
        Vector3f getTarget();
    }
}
