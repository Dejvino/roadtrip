package roadtrip.view;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.*;
import com.jme3.terrain.geomipmap.grid.FractalTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.geomipmap.lodcalc.LodCalculator;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.texture.Texture;
import java.util.Random;
import roadtrip.model.MapObjectInstance;
import roadtrip.model.ProceduralMapQuadBlock;
import roadtrip.model.TerrainDataProvider;
import roadtrip.view.model.GameWorldState;
import roadtrip.view.model.Player;

/**
 * Created by dejvino on 14.01.2017.
 */
public class GameWorldView {

    public static boolean DEBUG = false;//true;

    private final GameWorldState state;

    private final AssetManager assetManager;
    private final Camera camera;
    private final Node rootNode;
    private final PhysicsSpace physicsSpace;
    private final HideControl.TargetProvider targetProvider;
    
    public TerrainView terrain = new TerrainView(new TerrainDataProvider());

    public GameWorldView(GameWorldState gameWorldState, AssetManager assetManager,
            Camera camera, Node rootNode, PhysicsSpace physicsSpace,
            HideControl.TargetProvider targetProvider) {
        this.state = gameWorldState;
        this.assetManager = assetManager;
        this.camera = camera;
        this.rootNode = rootNode;
        this.physicsSpace = physicsSpace;
        this.targetProvider = targetProvider;
    }

    public static GameWorldView create(GameWorldState gameWorldState,
            AssetManager assetManager, Camera camera, Node rootNode,
            PhysicsSpace physicsSpace, HideControl.TargetProvider targetProvider) {
        GameWorldView gameWorldView = new GameWorldView(gameWorldState, assetManager, camera, rootNode, physicsSpace, targetProvider);
        gameWorldView.initialize();
        return gameWorldView;
    }

    private void initialize()
    {
        // Environment
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.LightGray);
        dl.setDirection(new Vector3f(1, -1, 1));
        rootNode.addLight(dl);
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        rootNode.addLight(al);
        
        // TERRAIN TEXTURE material
        terrain.mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
        if (DEBUG) {
            terrain.mat_terrain.getAdditionalRenderState().setWireframe(true);
        }
        
        float heightScale = 400f;

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
        Texture textureMid = this.assetManager.loadTexture("Textures/map-mid.png");
        textureMid.setWrap(Texture.WrapMode.Repeat);
        Texture textureLow = this.assetManager.loadTexture("Textures/map-low.png");
        textureLow.setWrap(Texture.WrapMode.Repeat);
        Texture textureHigh = this.assetManager.loadTexture("Textures/map-high.png");
        textureHigh.setWrap(Texture.WrapMode.Repeat);

        float modif = (heightScale / 100f) / 3f;
        terrain.mat_terrain.setTexture("region1ColorMap", textureLow);
        terrain.mat_terrain.setVector3("region1", new Vector3f(0, 80 * modif, terrain.texLowScale));

        terrain.mat_terrain.setTexture("region2ColorMap", textureMid);
        terrain.mat_terrain.setVector3("region2", new Vector3f(100 * modif, 160 * modif, terrain.texMidScale));

        terrain.mat_terrain.setTexture("region3ColorMap", textureHigh);
        terrain.mat_terrain.setVector3("region3", new Vector3f(190 * modif, 240 * modif, terrain.texHighScale));

        terrain.mat_terrain.setTexture("region4ColorMap", textureLow);
        terrain.mat_terrain.setVector3("region4", new Vector3f(250 * modif, 350 * modif, terrain.texLowScale));

        terrain.mat_terrain.setTexture("slopeColorMap", textureHigh);
        terrain.mat_terrain.setFloat("slopeTileFactor", 32);

        terrain.mat_terrain.setFloat("terrainSize", 513);

        terrain.terrainDataProvider.base = new FractalSum();
        terrain.terrainDataProvider.base.setRoughness(0.6f);
        terrain.terrainDataProvider.base.setFrequency(1.0f);
        terrain.terrainDataProvider.base.setAmplitude(1.0f);
        terrain.terrainDataProvider.base.setLacunarity(4.12f);
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
        terrain.terrainDataProvider.smooth.setEffect(0.3f);

        terrain.terrainDataProvider.iterate = new IterativeFilter();
        terrain.terrainDataProvider.iterate.addPreFilter(terrain.terrainDataProvider.perturb);
        terrain.terrainDataProvider.iterate.addPostFilter(terrain.terrainDataProvider.smooth);
        terrain.terrainDataProvider.iterate.setFilter(terrain.terrainDataProvider.therm);
        terrain.terrainDataProvider.iterate.setIterations(2);

        ground.addPreFilter(terrain.terrainDataProvider.iterate);

        int patchSize = 16;
        Vector3f terrainScale = new Vector3f(8f, 1f, 8f);
        //terrain.terrainGrid = new TerrainGrid("terrain", 16 + 1, 512 + 1, new FractalTileLoader(ground, 300f));
        terrain.terrainGrid = new FineTerrainGrid("terrain", patchSize + 1, 128 + 1, terrainScale, new FractalTileLoader(ground, heightScale));

        terrain.terrainGrid.setMaterial(terrain.mat_terrain);
        //terrain.terrainGrid.setLocalTranslation(0, -200, 0);
        terrain.terrainGrid.setLocalScale(terrainScale);
        this.rootNode.attachChild(terrain.terrainGrid);

        final TerrainLodControl lodControl = new FineTerrainGridLodControl(terrain.terrainGrid, camera);
        lodControl.setLodCalculator(new DistanceLodCalculator(patchSize + 1, 3.7f)); // patch size, and a multiplier
        terrain.terrainGrid.addControl(lodControl);
        
        final Node treeModel = createTree();
        final Node blockModel = createBlock();
        final Node rockModel = createRock();
        
        final FineTerrainGrid terrainGrid = terrain.terrainGrid;
        terrainGrid.addListener(new TerrainGridListener() {

            @Override
            public void gridMoved(Vector3f newCenter) {   
            }

            @Override
            public void tileAttached(Vector3f cell, TerrainQuad quad) {
                lodControl.forceUpdate();
                while(quad.getControl(RigidBodyControl.class)!=null){
                    quad.removeControl(RigidBodyControl.class);
                }
                quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), terrainGrid.getLocalScale()), 0));
                physicsSpace.add(quad);

                removeQuadObjectsNode(quad);

                String quadObjectsNodeKey = getQuadObjectsNodeKey(quad);
                Node objects = new Node(quadObjectsNodeKey);
                populateQuadObjectsNode(quad, objects);
                rootNode.attachChild(objects);
            }

            protected void populateQuadObjectsNode(TerrainQuad quad, Node objects)
            {
                ProceduralMapQuadBlock mapQuadBlock = state.proceduralMap.getMapQuadBlock(quad);

                /*/ DEBUG pole in the middle of a quad
                Spatial m = treeModel.clone();
                m.setLocalTranslation(quad.getWorldTranslation().add(new Vector3f(0f, getHeight(quad, quad.getWorldTranslation()), 0f)));
                m.setLocalScale(new Vector3f(1f, 20f, 1f));
                objects.attachChild(m);
                /**/
                
                // Add map objects
                Random rand = mapQuadBlock.getBlockRandom();
                for (MapObjectInstance mapObject : mapQuadBlock.getMapObjects()) {
                    Vector3f pos = mapObject.getPosition();
                    Vector3f scale = Vector3f.UNIT_XYZ;
                    Vector3f boxHalf = Vector3f.UNIT_XYZ;
                    float rotation = 0f;
                    Spatial modelInstance;
                    RigidBodyControl modelPhysics;
                    switch (mapObject.getType()) {
                        case "tree":
                            case "house":
                            modelInstance = treeModel.clone();
                            float s = 0.2f + rand.nextFloat() * 5f;
                            scale = new Vector3f(s, s, s);
                            rotation = rand.nextFloat() * 2f * 3.14f;
                            boxHalf = new Vector3f(s * 0.2f, s * 3f, s * 0.2f);
                            modelPhysics = new RigidBodyControl(new BoxCollisionShape(boxHalf), 0f);
                            break;
                        case "rock":
                            modelInstance = blockModel.clone();
                            boxHalf = new Vector3f(0.5f, 0.5f, 0.5f);
                            pos.y += 0.2f;
                            modelPhysics = new RigidBodyControl(new BoxCollisionShape(boxHalf), 0f);
                            break;
                        case "wall":
                            modelInstance = blockModel.clone();
                            scale = new Vector3f(1f, 2f, 1f);
                            boxHalf = new Vector3f(0.5f, 1f, 0.5f);
                            pos.y += 0.5f;
                            modelPhysics = new RigidBodyControl(new BoxCollisionShape(boxHalf), 0f);
                            break;
                        default:
                            throw new RuntimeException("Unhandled object type: " + mapObject.getType());
                    }
                    modelInstance.setLocalTranslation(pos);
                    modelInstance.setLocalScale(scale);
                    modelInstance.setLocalRotation(new Quaternion().fromAngles(0f, rotation, 0f));
                    // TODO: physics from the model and not hard-coded
                    //RigidBodyControl control = treeInstance.getControl(RigidBodyControl.class);
                    if (modelPhysics != null) {
                        modelPhysics.isActive();
                        modelInstance.addControl(modelPhysics);
                        modelPhysics.setPhysicsLocation(pos);
                        physicsSpace.add(modelPhysics);
                    }
                    objects.attachChild(modelInstance);
                }
                
                int w = 128;
                for (int i = 0; i < w*w; i++) {
                    int x = i % w;
                    int z = i / w;
                    if (((x % 8) + (z % 8)) != 0) continue;
                    Vector3f pos = new Vector3f(x - w/2f + 0.25f*terrainGrid.getLocalScale().x, 0f, z - w/2f + 0.25f*terrainGrid.getLocalScale().z).addLocal(quad.getWorldTranslation());
                    pos.addLocal(0f, getHeight(quad, pos), 0f);
                    Vector3f scale = Vector3f.UNIT_XYZ;
                    float rotation = 0f;
                    Spatial modelInstance;
                    float s = 0.1f;
                    if (i == 0) {
                        s = 0.4f;
                    } else if ((x % 16) + (z % 16) == 0) {
                        s = 0.2f;
                    }
                    switch ("grass") {
                        case "grass":
                            modelInstance = rockModel.clone();
                            scale = new Vector3f(s, s, s);
                            rotation = i * 5.2f;
                            break;
                        default:
                            throw new RuntimeException("Unhandled object type");
                    }
                    modelInstance.setLocalTranslation(pos);
                    modelInstance.setLocalScale(scale);
                    modelInstance.setLocalRotation(new Quaternion().fromAngles(0f, rotation, 0f));
                    modelInstance.addControl(new HideControl(20f + 140f * s, targetProvider));
                    objects.attachChild(modelInstance);
                }
            }

            @Override
            public void tileDetached(Vector3f cell, TerrainQuad quad) {
                if (quad.getControl(RigidBodyControl.class) != null) {
                    physicsSpace.remove(quad);
                    quad.removeControl(RigidBodyControl.class);
                }
                removeQuadObjectsNode(quad);
            }

            protected void removeQuadObjectsNode(TerrainQuad quad)
            {
                Spatial quadObjectsNodeOld = rootNode.getChild(getQuadObjectsNodeKey(quad));
                if (quadObjectsNodeOld != null) {
                    physicsSpace.removeAll(quadObjectsNodeOld);
                    quadObjectsNodeOld.removeFromParent();
                }
            }

            private String getQuadObjectsNodeKey(TerrainQuad quad)
            {
                return "Objects-" + quad.getName();
            }

            private float getHeight(TerrainQuad quad, Vector3f pos)
            {
                return quad.getHeight(new Vector2f(pos.x, pos.z));
            }
        });
        /**/

    }

    private Node createTree() {
        Material trunkMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        trunkMat.setColor("Color",  ColorRGBA.Brown);
        
        Material branchesMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        branchesMat.setColor("Color", new ColorRGBA(0.3f, 0.7f, 0.3f, 1f));
        branchesMat.getAdditionalRenderState().setWireframe(true);
        
        Geometry treeTrunk = new Geometry("tree-trunk", new Box(0.1f, 1f, 0.1f));
        treeTrunk.setLocalTranslation(0f, 0.5f, 0f);
        treeTrunk.setMaterial(trunkMat);
        
        Geometry treeBranches = new Geometry("tree-branches", new Sphere(6, 5, 1f));
        treeBranches.setLocalTranslation(0f, 1.5f, 0f);
        treeBranches.setMaterial(branchesMat);
        
        Node treeModel = new Node("tree");
        treeModel.attachChild(treeTrunk);
        treeModel.attachChild(treeBranches);
        
        return treeModel;
    }

    private Node createRock() {
        Material rockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rockMat.setColor("Color",  ColorRGBA.Gray);
        rockMat.getAdditionalRenderState().setWireframe(true);
        
        Geometry rockGeom = new Geometry("rock", new Sphere(3, 3, 1f));
        rockGeom.setMaterial(rockMat);
        
        Node rockModel = new Node("rockNode");
        rockModel.attachChild(rockGeom);
        
        return rockModel;
    }
    
    private Node createBlock() {
        Material rockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rockMat.setColor("Color",  ColorRGBA.Gray);
        rockMat.getAdditionalRenderState().setWireframe(true);
        
        Geometry rockGeom = new Geometry("rock", new Box(0.5f, 0.5f, 0.5f));
        rockGeom.setMaterial(rockMat);
        
        Node rockModel = new Node("rockNode");
        rockModel.attachChild(rockGeom);
        
        return rockModel;
    }

}
