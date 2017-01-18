package roadtrip.view;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * Created by dejvino on 18.01.2017.
 */
public class GameMenuNode extends Node
{
	String menuTextPrefix = "~~~~~~~~~~~~~~~~~~~~\n   Road Trip   \n~~~~~~~~~~~~~~~~~~~~\n";
	String menuEntriesPrefix = "";
	String[] menuEntries = { "New Game", "Load Game", "Settings", "Credits", "Exit" };
	int menuEntryIndex = 0;

	BitmapText uiText;
	Node menuBook;
	String menuText = "";

	public GameMenuNode() {
        this("GameMenu");
    }

    public GameMenuNode(String name) {
        super(name);
    }

    public void initialize(AssetManager assetManager)
    {
	    menuBook = new Node("menu");
	    Geometry book = new Geometry("book", new Box(8, 8, 1));
	    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    mat.setColor("Color", ColorRGBA.Brown);
	    book.setMaterial(mat);
	    book.setLocalTranslation(0f, 0f, -1.1f);
	    menuBook.attachChild(book);
	    BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
	    uiText = new BitmapText(fnt, false);
	    uiText.setBox(new Rectangle(-5, 7, 12, 14));
	    uiText.setAlignment(BitmapFont.Align.Left);
	    uiText.setQueueBucket(RenderQueue.Bucket.Transparent);
	    uiText.setSize(1.0f);
	    setMenuText(getMenuText());
	    menuBook.attachChild(uiText);

	    attachChild(menuBook);
    }

	public void setMenuText(String menuText)
	{
		this.menuText = menuText;
		uiText.setText(menuText);
	}

	public void moveToCamera(Camera cam, Node target)
	{
		setLocalTranslation(cam.getLocation().add(cam.getRotation().mult(Vector3f.UNIT_Z).mult(30.0f)));
		Quaternion textRot = new Quaternion();
		textRot.lookAt(new Vector3f(target.getWorldTranslation()).subtractLocal(cam.getLocation()).negate(), Vector3f.UNIT_Y);
		setLocalRotation(textRot);
	}

	public void nextMenuEntry()
	{
		menuEntryIndex = (menuEntryIndex - 1 + menuEntries.length) % menuEntries.length;
		uiText.setText(getMenuText());
	}

	public void prevMenuEntry()
	{
		menuEntryIndex = (menuEntryIndex + 1) % menuEntries.length;
		uiText.setText(getMenuText());
	}

	public int getMenuEntryIndex()
	{
		return menuEntryIndex;
	}

	public void setMenuEntriesPrefix(String menuEntriesPrefix)
	{
		this.menuEntriesPrefix = menuEntriesPrefix;
		uiText.setText(getMenuText());
	}

	private String getMenuText()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(menuTextPrefix);
		sb.append(menuEntriesPrefix);
		for (int i = 0; i < menuEntries.length; i++) {
			String entry = menuEntries[i];
			boolean selected = (i == menuEntryIndex);
			sb.append(selected ? "]>" : "  ");
			sb.append(entry);
			sb.append(selected ? "<[" : "  ");
			sb.append("\n");
		}
		return sb.toString();
	}

}
