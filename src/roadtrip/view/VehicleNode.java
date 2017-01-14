/*
 */
package roadtrip.view;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import roadtrip.model.VehicleInstance;

/**
 *
 * @author dejvino
 */
public class VehicleNode extends Node
{
    VehicleInstance vehicleInstance;

    VehicleControl vehicleControl;

    Spatial vehicleModel;

    AudioNode engineAudio;
    AudioNode wheelsAudio;
    AudioNode wheelSlipAudio;

    public VehicleNode(String name, VehicleInstance vehicleInstance,
            VehicleControl vehicleControl, Spatial vehicleModel)
    {
        super(name);
        this.vehicleInstance = vehicleInstance;
        this.vehicleControl = vehicleControl;
        this.vehicleModel = vehicleModel;
    }
}