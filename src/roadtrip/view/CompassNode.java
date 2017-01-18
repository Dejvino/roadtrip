package roadtrip.view;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;

/**
 * Created by dejvino on 18.01.2017.
 */
public class CompassNode extends Node
{
	private BitmapText targetText;

	public CompassNode()
	{
	}

	public CompassNode(String name)
	{
		super(name);
	}
	
	public void initialize(AssetManager assetManager)
	{
		Material matRed = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		matRed.setColor("Color",  ColorRGBA.Red);
		Material matBlack = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		matBlack.setColor("Color",  ColorRGBA.Black);

		Geometry compassGeomN = new Geometry("compass-N", new Arrow(new Vector3f(0.0f, 0.0f, 1.2f)));
		compassGeomN.setMaterial(matRed);
		Geometry compassGeomS = new Geometry("compass-S", new Arrow(new Vector3f(0.0f, 0.0f, -1.0f)));
		compassGeomS.setMaterial(matBlack);
		Geometry compassGeomW = new Geometry("compass-W", new Arrow(new Vector3f(-1.0f, 0.0f, 0.0f)));
		compassGeomW.setMaterial(matBlack);
		Geometry compassGeomE = new Geometry("compass-E", new Arrow(new Vector3f(1.0f, 0.0f, 0.0f)));
		compassGeomE.setMaterial(matBlack);

		attachChild(compassGeomN);
		attachChild(compassGeomS);
		attachChild(compassGeomW);
		attachChild(compassGeomE);

		BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
		targetText = new BitmapText(fnt, false);
		targetText.setBox(new Rectangle(-5, 4, 10, 4));
		targetText.setAlignment(BitmapFont.Align.Center);
		targetText.setQueueBucket(RenderQueue.Bucket.Transparent);
		targetText.setSize( 1.2f );
		targetText.setText("Target");
		targetText.setLocalRotation(new Quaternion().fromAngles(0, 3.1415f, 0));
		attachChild(targetText);
	}

	public void setTargetText(String text)
	{
		targetText.setText(text);
	}
}
