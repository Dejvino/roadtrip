package roadtrip.model;

import com.jme3.math.Vector3f;

/**
 * Created by dejvino on 21.01.2017.
 */
public class MapObjectInstance
{
	private Vector3f position;
        private String type;

	public MapObjectInstance(String type, Vector3f position)
	{
            this.type = type;
            this.position = new Vector3f(position);
	}

	public Vector3f getPosition()
	{
		return new Vector3f(position);
	}

	public void setPosition(Vector3f position)
	{
		this.position = new Vector3f(position);
	}

    public String getType() {
        return type;
    }
}
