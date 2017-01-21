package roadtrip.view.model;

import roadtrip.view.VehicleNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dejvino on 14.01.2017.
 */
public class GameWorldState
{
    public final ProceduralMapBlock proceduralMap;
    public final List<VehicleNode> vehicles = new LinkedList<>();

    public GameWorldState(long worldSeed)
    {
        this.proceduralMap = new ProceduralMapBlock(worldSeed);
    }
}
