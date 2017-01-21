package roadtrip.model;

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

	public ProceduralMapQuadBlock getMapQuadBlock(String quadName)
	{
		return getSubBlock(quadName, ProceduralMapQuadBlock.class);
	}
}
