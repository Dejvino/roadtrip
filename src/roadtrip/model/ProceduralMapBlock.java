package roadtrip.model;

import com.jme3.terrain.geomipmap.TerrainQuad;
import roadtrip.model.AbstractProceduralBlock;
import roadtrip.model.ProceduralMapQuadBlock;

/**
 * Created by dejvino on 21.01.2017.
 */
public class ProceduralMapBlock extends AbstractProceduralBlock
{
	public ProceduralMapBlock(long seed)
	{
		super(seed);
	}

	public ProceduralMapQuadBlock getMapQuadBlock(TerrainQuad terrainQuad)
	{
		ProceduralMapQuadBlock quadBlock = getSubBlock(terrainQuad.getName(), ProceduralMapQuadBlock.class);
		quadBlock.initialize(terrainQuad);
		return quadBlock;
	}
}
