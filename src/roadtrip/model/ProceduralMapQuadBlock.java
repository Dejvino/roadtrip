package roadtrip.model;

import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by dejvino on 21.01.2017.
 */
public class ProceduralMapQuadBlock extends AbstractProceduralBlock
{
	protected float cellSize = 64 * 2f * 2f; /* terrainGrid.getPatchSize() * terrainGrid.getLocalScale().x * 2f */

	private List<MapObjectInstance> mapObjects;

	public ProceduralMapQuadBlock(long seed)
	{
		super(seed);
	}

	public void initialize(TerrainQuad terrainQuad)
	{
		mapObjects = new LinkedList<>();
		Random quadRand = getBlockRandom();
		Vector2f prevPos = null;
		Vector2f quadPos = new Vector2f(terrainQuad.getLocalTranslation().x, terrainQuad.getLocalTranslation().z);
		for (int i = 0; i < quadRand.nextInt(10000); i++) {
			Vector2f pos;
			if (prevPos == null || quadRand.nextFloat() < 0.2f) {
				pos = new Vector2f((quadRand.nextFloat() - 0.5f) * cellSize, (quadRand.nextFloat() - 0.5f) * cellSize)
						.addLocal(quadPos);
			} else {
				pos = new Vector2f((quadRand.nextFloat() - 0.5f) * 20f, (quadRand.nextFloat() - 0.5f) * 20f)
						.addLocal(prevPos);
			}
			prevPos = pos;
			float height = terrainQuad.getHeight(pos);
			Vector3f location = new Vector3f(pos.x, height, pos.y);
			mapObjects.add(new MapObjectInstance(location));
		}
	}

	public Iterable<? extends MapObjectInstance> getMapObjects()
	{
		if (mapObjects == null) throw new RuntimeException("Call to getMapObjects on an uninitialized block.");
		return mapObjects;
	}
}
