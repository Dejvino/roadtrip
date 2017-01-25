package roadtrip.model;

import com.jme3.terrain.geomipmap.LRUCache;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

/**
 * Created by dejvino on 21.01.2017.
 */
public abstract class AbstractProceduralBlock implements ProceduralBlock
{
	private long seed;

        private LRUCache<String, ProceduralBlock> subBlocksCache = new LRUCache<>(getSubBlocksCacheSize());
        
	public AbstractProceduralBlock(long seed)
	{
		this.seed = seed;
	}

	@Override
	public long getBlockSeed()
	{
		return seed;
	}

	@Override
	public Random getBlockRandom()
	{
		return new Random(seed);
	}

	@Override
	public long getSubBlockSeed(String subBlockKey)
	{
		return (String.valueOf(seed) + "::" + subBlockKey).hashCode();
	}

	@Override
	public <T extends ProceduralBlock> T getSubBlock(String subBlockKey, Class<T> subBlockClass)
	{
		if (subBlockClass == null) throw new NullPointerException("subBlockClass");
                T result = (T) subBlocksCache.get(subBlockKey);
                if (result != null) {
                    return result;
                }
		try {
			Constructor<T> constructor = subBlockClass.getConstructor(Long.TYPE);
			result = constructor.newInstance(getSubBlockSeed(subBlockKey));
                        subBlocksCache.put(subBlockKey, result);
                        return result;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Class " + subBlockClass + " does not have the default constructor with a single 'long' parameter.", e);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new IllegalArgumentException("Unable to instantiate sub-block.", e);
		}
	}

    public int getSubBlocksCacheSize()
    {
        return 8;
    }
}
