package roadtrip.view;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.grid.FractalTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.texture.Texture;
import roadtrip.model.TerrainDataProvider;
import roadtrip.view.model.GameWorldState;

/**
 * Created by dejvino on 14.01.2017.
 */
public class GameWorldView {

    private GameWorldState state = new GameWorldState();

    private AssetManager assetManager;
    private Camera camera;
    private Node rootNode;

    public TerrainView terrain = new TerrainView(new TerrainDataProvider());

    public static GameWorldView create(AssetManager assetManager, Camera camera, Node rootNode) {
        GameWorldView gameWorldView = new GameWorldView();
        gameWorldView.assetManager = assetManager;
        gameWorldView.camera = camera;
        gameWorldView.rootNode = rootNode;
        gameWorldView.initialize();
        return gameWorldView;
    }

    private void initialize()
    {
        // TERRAIN TEXTURE material
        terrain.mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

        // Parameters to material:
        // regionXColorMap: X = 1..4 the texture that should be appliad to state X
        // regionX: a Vector3f containing the following information:
        //      regionX.x: the start height of the region
        //      regionX.y: the end height of the region
        //      regionX.z: the texture scale for the region
        //  it might not be the most elegant way for storing these 3 values, but it packs the data nicely :)
        // slopeColorMap: the texture to be used for cliffs, and steep mountain sites
        // slopeTileFactor: the texture scale for slopes
        // terrainSize: the total size of the terrain (used for scaling the texture)
        // GRASS texture
        Texture grass = this.assetManager.loadTexture("Textures/solid-grass.png");
        grass.setWrap(Texture.WrapMode.Repeat);
        Texture dirt = this.assetManager.loadTexture("Textures/solid-road.png");
        dirt.setWrap(Texture.WrapMode.Repeat);
        Texture rock = this.assetManager.loadTexture("Textures/solid-stone.png");
        rock.setWrap(Texture.WrapMode.Repeat);

        terrain.mat_terrain.setTexture("region1ColorMap", dirt);
        terrain.mat_terrain.setVector3("region1", new Vector3f(0, 80, terrain.dirtScale));

        terrain.mat_terrain.setTexture("region2ColorMap", grass);
        terrain.mat_terrain.setVector3("region2", new Vector3f(100, 160, terrain.grassScale));

        terrain.mat_terrain.setTexture("region3ColorMap", rock);
        terrain.mat_terrain.setVector3("region3", new Vector3f(190, 240, terrain.rockScale));

        terrain.mat_terrain.setTexture("region4ColorMap", dirt);
        terrain.mat_terrain.setVector3("region4", new Vector3f(250, 350, terrain.dirtScale));

        terrain.mat_terrain.setTexture("slopeColorMap", rock);
        terrain.mat_terrain.setFloat("slopeTileFactor", 32);

        terrain.mat_terrain.setFloat("terrainSize", 513);

        terrain.terrainDataProvider.base = new FractalSum();
        terrain.terrainDataProvider.base.setRoughness(0.7f);
        terrain.terrainDataProvider.base.setFrequency(1.0f);
        terrain.terrainDataProvider.base.setAmplitude(1.0f);
        terrain.terrainDataProvider.base.setLacunarity(2.12f);
        terrain.terrainDataProvider.base.setOctaves(8);
        terrain.terrainDataProvider.base.setScale(0.02125f);
        terrain.terrainDataProvider.base.addModulator(new NoiseModulator() {

            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(terrain.terrainDataProvider.base);

        terrain.terrainDataProvider.perturb = new PerturbFilter();
        terrain.terrainDataProvider.perturb.setMagnitude(0.119f);

        terrain.terrainDataProvider.therm = new OptimizedErode();
        terrain.terrainDataProvider.therm.setRadius(5);
        terrain.terrainDataProvider.therm.setTalus(0.011f);

        terrain.terrainDataProvider.smooth = new SmoothFilter();
        terrain.terrainDataProvider.smooth.setRadius(1);
        terrain.terrainDataProvider.smooth.setEffect(0.7f);

        terrain.terrainDataProvider.iterate = new IterativeFilter();
        terrain.terrainDataProvider.iterate.addPreFilter(terrain.terrainDataProvider.perturb);
        terrain.terrainDataProvider.iterate.addPostFilter(terrain.terrainDataProvider.smooth);
        terrain.terrainDataProvider.iterate.setFilter(terrain.terrainDataProvider.therm);
        terrain.terrainDataProvider.iterate.setIterations(2);

        ground.addPreFilter(terrain.terrainDataProvider.iterate);

        terrain.terrainGrid = new TerrainGrid("terrain", 64 + 1, 256 + 1, new FractalTileLoader(ground, 300f));

        terrain.terrainGrid.setMaterial(terrain.mat_terrain);
        terrain.terrainGrid.setLocalTranslation(0, -200, 0);
        terrain.terrainGrid.setLocalScale(2f, 1f, 2f);
        this.rootNode.attachChild(terrain.terrainGrid);

        TerrainLodControl control = new TerrainGridLodControl(terrain.terrainGrid, camera);
        control.setLodCalculator(new DistanceLodCalculator(64 + 1, 2.7f)); // patch size, and a multiplier
        terrain.terrainGrid.addControl(control);
    }
}
