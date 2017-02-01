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
	private List<MapObjectInstance> mapObjects;

	public ProceduralMapQuadBlock(long seed)
	{
		super(seed);
	}

	public void initialize(TerrainQuad terrainQuad)
	{
                float cellSize = terrainQuad.getPatchSize() * 8f * terrainQuad.getLocalScale().x * 2f;
		mapObjects = new LinkedList<>();
		Random quadRand = getBlockRandom();
		Vector2f prevPos = null;
		Vector2f quadPos = new Vector2f(terrainQuad.getWorldTranslation().x, terrainQuad.getWorldTranslation().z);
		for (int i = 0; i < quadRand.nextInt(terrainQuad.getPatchSize() * terrainQuad.getPatchSize()); i++) {
			Vector2f pos;
			if (prevPos == null || quadRand.nextFloat() < 0.2f) {
				pos = new Vector2f((quadRand.nextFloat() - 0.5f) * cellSize, (quadRand.nextFloat() - 0.5f) * cellSize)
						.addLocal(quadPos);
			} else {
				pos = new Vector2f((quadRand.nextFloat() - 0.5f) * (cellSize / 10f), (quadRand.nextFloat() - 0.5f) * (cellSize / 10f))
						.addLocal(prevPos);
			}
			prevPos = pos;
			float height = terrainQuad.getHeight(pos);
                        String type;
                        if (quadRand.nextInt(10) == 0) {
                            type = "rock";
                        } else {
                            type = "tree";
                        }
			Vector3f location = new Vector3f(pos.x, height, pos.y);
			mapObjects.add(new MapObjectInstance(type, location));
		}
                
                // walls
                if (quadRand.nextInt(3) == 0) {
                    int dir = quadRand.nextInt(4);
                    for (int i = 0; i < quadRand.nextInt(terrainQuad.getPatchSize() * (10 + quadRand.nextInt(100))); i++) {
                            Vector2f pos;
                            if (prevPos == null || quadRand.nextFloat() < 0.1f) {
                                    pos = new Vector2f((quadRand.nextFloat() - 0.5f) * cellSize, (quadRand.nextFloat() - 0.5f) * cellSize)
						.addLocal(quadPos);
                            } else {
                                    if (quadRand.nextInt(10) == 0) {
                                        dir = (dir + quadRand.nextInt(2) * 2 - 1) % 4;
                                    }
                                    pos = new Vector2f(dir == 1 ? 1 : (dir == 3 ? -1 : 0), dir == 0 ? 1 : (dir == 2 ? -1 : 0)).addLocal(prevPos);
                            }
                            prevPos = pos;
                            float height = terrainQuad.getHeight(pos);
                            String type = "wall";
                            Vector3f location = new Vector3f(pos.x, height, pos.y);
                            mapObjects.add(new MapObjectInstance(type, location));
                    }
                }
	}

	public Iterable<? extends MapObjectInstance> getMapObjects()
	{
		if (mapObjects == null) throw new RuntimeException("Call to getMapObjects on an uninitialized block.");
		return mapObjects;
	}
}
