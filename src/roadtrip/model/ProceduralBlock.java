package roadtrip.model;

import java.util.Random;

/**
 * Created by dejvino on 21.01.2017.
 */
public interface ProceduralBlock
{
	/**
	 * The main PRNG seed defining this block's content.
	 * @return Seed value.
	 */
	long getBlockSeed();

	/**
	 * Random generator initialized with the block's seed.
	 *
	 * @return fresh PRNG.
	 */
	Random getBlockRandom();

	/**
	 * Provides a seed to be used with a sub-block with a given key (identifier).
	 *
	 * @param subBlockKey
	 * @return Sub-block seed.
	 */
	long getSubBlockSeed(String subBlockKey);

	/**
	 * Provides a sub-block of the given class. The sub-block's seed is based on this block's seed.
	 *
	 * @param subBlockClass
	 * @param <T>
	 * @return Newly added sub-block.
	 */
	<T extends ProceduralBlock> T getSubBlock(String subBlockKey, Class<T> subBlockClass);
}
