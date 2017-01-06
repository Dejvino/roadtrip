package roadtrip;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource.Status;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
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
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author dejvino
 */
public class RoadTrip extends SimpleApplication implements ActionListener {

    public static void main(String[] args)
    {
        RoadTrip app = new RoadTrip();
        app.start();
    }
    
    public static boolean DEBUG = false;//true;
    
    private BulletAppState bulletAppState;
    
    private ChaseCamera chaseCam;
   
    // START Terrain
    private Material mat_terrain;
    private TerrainGrid terrain;
    private float grassScale = 64;
    private float dirtScale = 64;
    private float rockScale = 64;

    private FractalSum base;
    private PerturbFilter perturb;
    private OptimizedErode therm;
    private SmoothFilter smooth;
    private IterativeFilter iterate;
    // END Terrain
    
    private List<VehicleNode> vehicles = new LinkedList<>();
    
    // START Player
    private Node playerNode;
    private BetterCharacterControl playerPersonControl;
    private Vector3f jumpForce = new Vector3f(0, 3000, 0);
    private Vector3f walkDir = new Vector3f();
    private VehicleNode playerVehicleNode;
    // END Player
    
    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        if (DEBUG) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        
        setupKeys();
        
        //audioRenderer.setEnvironment(Environment.Dungeon);
        //AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
        
        // Environment
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.LightGray);
        dl.setDirection(new Vector3f(1, -1, 1));
        rootNode.addLight(dl);
        
        addMap();
        
        addCar();
        addCar();
        addCar();
        addCar();
        addCar();
        addPerson();
        addPerson();
        addPerson();
        addPerson();
        addPerson();
        addPerson();
        addPerson();
        
        addPlayer();
        
        chaseCam = new ChaseCamera(cam, playerNode, inputManager);
        chaseCam.setSmoothMotion(true);
    }

    private void setupKeys() {
        inputManager.clearMappings();
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Revs", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Esc", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Revs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
        inputManager.addListener(this, "Esc");
    }

    private void addCar()
    {
        Node vehicleModel = new Node("VehicleModel");
        VehicleInstance vehicleInstance = VehicleInstance.createVehicle(vehicles.size() % VehicleInstance.getVehicleTypesCount());
        vehicleInstance.brakeForce = vehicleInstance.accelerationForce;
        
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Black);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 1.0f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1.6f;
        float zOff = 2f;

        Material matBody = new Material(getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        matBody.setFloat("Shininess", 32f);
        matBody.setBoolean("UseMaterialColors", true);
        matBody.setColor("Ambient",  ColorRGBA.Black);
        matBody.setColor("Diffuse",  ColorRGBA.Red);
        matBody.setColor("Specular", ColorRGBA.White);
        
        
        if (vehicleInstance.carType == VehicleInstance.WEAK) {
            Spatial carBody = getAssetManager().loadModel("Models/rivercrossing.j3o");
            carBody.setLocalScale(1.1f, 0.8f, 0.8f);
            carBody.setLocalTranslation(0f, -1f, 0f);
            carBody.rotate(0f, 3.1415f, 0f);
            vehicleModel.attachChild(carBody);
        } else {
            Geometry carBody = new Geometry("car body", new Box(new Vector3f(0.0f, 1f, 0.0f), 1.4f, 0.5f, 3.6f));
            carBody.setMaterial(matBody);
            vehicleModel.attachChild(carBody);
        }

        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.4f, 0.5f, 3.6f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
        
        if (vehicleInstance.carType == VehicleInstance.TRUCK) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.4f, 0.8f, 1.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, 2f));
            
            Geometry carCabin = new Geometry("car cabin", new Box(new Vector3f(0, 2, 2f), 1.4f, 0.8f, 1.0f));
            carCabin.setMaterial(matBody);
            vehicleModel.attachChild(carCabin);
        } else if (vehicleInstance.carType == VehicleInstance.SPORT) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.2f, 0.6f, 2.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, -1f));
            
            Geometry carCabin = new Geometry("car cabin", new Box(new Vector3f(0, 2, -1f), 1.2f, 0.6f, 2.0f));
            carCabin.setMaterial(matBody);
            vehicleModel.attachChild(carCabin);
        }
        
        VehicleControl vehicleControl = new VehicleControl(compoundShape, 500);
        
        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 30.0f;//200=f1 car
        float compValue = .1f; //(should be lower than damp)
        float dampValue = .2f;
        vehicleControl.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicleControl.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicleControl.setSuspensionStiffness(stiffness);
        vehicleControl.setMaxSuspensionForce(10000.0f);
        
        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.2f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        vehicleControl.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        vehicleControl.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        vehicleControl.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        vehicleControl.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);
        
        vehicleModel.attachChild(node1);
        vehicleModel.attachChild(node2);
        vehicleModel.attachChild(node3);
        vehicleModel.attachChild(node4);
        
        if (vehicleInstance.carType == VehicleInstance.TRUCK) {
            Node node5 = new Node("wheel 5 node");
            Geometry wheels5 = new Geometry("wheel 5", wheelMesh);
            node5.attachChild(wheels5);
            wheels5.rotate(0, FastMath.HALF_PI, 0);
            wheels5.setMaterial(mat);
            vehicleControl.addWheel(node5, new Vector3f(-xOff, yOff, 2.1f* -zOff),
                    wheelDirection, wheelAxle, restLength, radius, false);

            Node node6 = new Node("wheel 6 node");
            Geometry wheels6 = new Geometry("wheel 6", wheelMesh);
            node6.attachChild(wheels6);
            wheels6.rotate(0, FastMath.HALF_PI, 0);
            wheels6.setMaterial(mat);
            vehicleControl.addWheel(node6, new Vector3f(xOff, yOff, 2.1f* -zOff),
                    wheelDirection, wheelAxle, restLength, radius, false);
            
            vehicleModel.attachChild(node5);
            vehicleModel.attachChild(node6);
        }
        
        vehicleControl.getWheel(0).setFrictionSlip(0.8f);
        vehicleControl.getWheel(1).setFrictionSlip(0.8f);
        vehicleControl.getWheel(2).setFrictionSlip(0.6f);
        vehicleControl.getWheel(3).setFrictionSlip(0.6f);
            
        if (vehicleInstance.carType == VehicleInstance.TRUCK) {
            vehicleControl.getWheel(4).setFrictionSlip(0.6f);
            vehicleControl.getWheel(5).setFrictionSlip(0.6f);
        }
        vehicleControl.setPhysicsLocation(new Vector3f(5f, 30f, 5f));
        
        VehicleNode vehicle = new VehicleNode("VehicleNode",
                vehicleInstance, vehicleControl, vehicleModel);
        vehicle.attachChild(vehicleModel);
                
        vehicle.engineAudio  = new AudioNode(assetManager, "Sounds/engine.ogg", false);
        vehicle.engineAudio.setPositional(true);
        vehicle.engineAudio.setLooping(true);
        vehicle.engineAudio.setReverbEnabled(true);
        vehicle.engineAudio.setRefDistance(5);
        vehicle.engineAudio.setMaxDistance(1000000);
        vehicle.attachChild(vehicle.engineAudio);
        
        vehicle.wheelsAudio  = new AudioNode(assetManager, "Sounds/wheels.ogg", false);
        vehicle.wheelsAudio.setPositional(true);
        vehicle.wheelsAudio.setLooping(true);
        //wheelsAudio.setReverbEnabled(true);
        vehicle.wheelsAudio.setRefDistance(1f);
        vehicle.wheelsAudio.setMaxDistance(1000000f);
        vehicle.wheelsAudio.play();
        vehicle.attachChild(vehicle.wheelsAudio);
        
        vehicle.wheelSlipAudio  = new AudioNode(assetManager, "Sounds/wheel-slip.ogg", false);
        vehicle.wheelSlipAudio.setPositional(true);
        vehicle.wheelSlipAudio.setLooping(true);
        //wheelsAudio.setReverbEnabled(true);
        vehicle.wheelSlipAudio.setRefDistance(5);
        vehicle.wheelSlipAudio.setMaxDistance(1000000);
        vehicle.wheelSlipAudio.play();
        vehicle.attachChild(vehicle.wheelSlipAudio);
        
        vehicle.addControl(vehicleControl);
        getPhysicsSpace().add(vehicleControl);
        vehicleControl.setPhysicsLocation(new Vector3f(10f + (float)Math.random() * 40f, 28f, 12f + (float)Math.random() * 40f));
        
        vehicles.add(vehicle);
        rootNode.attachChild(vehicle);
    }

    private Node addPerson() {
        Spatial personModel = assetManager.loadModel("Models/person.j3o");
        Node person = new Node("person");
        person.attachChild(personModel);
        BetterCharacterControl personControl = new BetterCharacterControl(1f, 4f, 10f);
        /*personModel.setLocalTranslation(0f, -1f, 0f);
        BoxCollisionShape personShape = new BoxCollisionShape(new Vector3f(0.5f, 2f, 0.5f));
        RigidBodyControl personControl = new RigidBodyControl(personShape, 80f);/**/
        person.addControl(personControl);
        /**/personControl.setJumpForce(new Vector3f(0,5f,0));
        personControl.setGravity(new Vector3f(0,1f,0));
        personControl.warp(new Vector3f(10f + (float)Math.random() * 20f, 30f, 12f + (float)Math.random() * 20f));/**/
        //personControl.setPhysicsLocation(new Vector3f(10f, 30f, 12f));
        getPhysicsSpace().add(personControl);
        //getPhysicsSpace().addAll(person);
        rootNode.attachChild(person);
        
        Vector3f dir = new Vector3f((float)Math.random() * 2f - 1f, 0f, (float)Math.random() * 2f - 1f);
        personControl.setViewDirection(dir);
        personControl.setWalkDirection(dir);
        
        return person;
    }

    private void addPlayer()
    {
        playerNode = addPerson();
        playerPersonControl = playerNode.getControl(BetterCharacterControl.class);
    }
    
    private void addMap() {
        // TERRAIN TEXTURE material
        this.mat_terrain = new Material(this.assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

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
        
        this.mat_terrain.setTexture("region1ColorMap", dirt);
        this.mat_terrain.setVector3("region1", new Vector3f(0, 80, this.dirtScale));
        
        this.mat_terrain.setTexture("region2ColorMap", grass);
        this.mat_terrain.setVector3("region2", new Vector3f(100, 160, this.grassScale));

        this.mat_terrain.setTexture("region3ColorMap", rock);
        this.mat_terrain.setVector3("region3", new Vector3f(190, 240, this.rockScale));

        this.mat_terrain.setTexture("region4ColorMap", dirt);
        this.mat_terrain.setVector3("region4", new Vector3f(250, 350, this.dirtScale));

        this.mat_terrain.setTexture("slopeColorMap", rock);
        this.mat_terrain.setFloat("slopeTileFactor", 32);

        this.mat_terrain.setFloat("terrainSize", 513);

        this.base = new FractalSum();
        this.base.setRoughness(0.7f);
        this.base.setFrequency(1.0f);
        this.base.setAmplitude(1.0f);
        this.base.setLacunarity(2.12f);
        this.base.setOctaves(8);
        this.base.setScale(0.02125f);
        this.base.addModulator(new NoiseModulator() {

            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(this.base);

        this.perturb = new PerturbFilter();
        this.perturb.setMagnitude(0.119f);

        this.therm = new OptimizedErode();
        this.therm.setRadius(5);
        this.therm.setTalus(0.011f);

        this.smooth = new SmoothFilter();
        this.smooth.setRadius(1);
        this.smooth.setEffect(0.7f);

        this.iterate = new IterativeFilter();
        this.iterate.addPreFilter(this.perturb);
        this.iterate.addPostFilter(this.smooth);
        this.iterate.setFilter(this.therm);
        this.iterate.setIterations(2);

        ground.addPreFilter(this.iterate);

        this.terrain = new TerrainGrid("terrain", 64 + 1, 256 + 1, new FractalTileLoader(ground, 300f));

        this.terrain.setMaterial(this.mat_terrain);
        this.terrain.setLocalTranslation(0, -200, 0);
        this.terrain.setLocalScale(2f, 1f, 2f);
        this.rootNode.attachChild(this.terrain);

        TerrainLodControl control = new TerrainGridLodControl(this.terrain, this.getCamera());
        control.setLodCalculator(new DistanceLodCalculator(64 + 1, 2.7f)); // patch size, and a multiplier
        this.terrain.addControl(control);
        
        terrain.addListener(new TerrainGridListener() {

                @Override
                public void gridMoved(Vector3f newCenter) {
                }

                @Override
                public void tileAttached(Vector3f cell, TerrainQuad quad) {
                    while(quad.getControl(RigidBodyControl.class)!=null){
                        quad.removeControl(RigidBodyControl.class);
                    }
                    quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), terrain.getLocalScale()), 0));
                    bulletAppState.getPhysicsSpace().add(quad);
                }

                @Override
                public void tileDetached(Vector3f cell, TerrainQuad quad) {
                    if (quad.getControl(RigidBodyControl.class) != null) {
                        bulletAppState.getPhysicsSpace().remove(quad);
                        quad.removeControl(RigidBodyControl.class);
                    }
                }

            });
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerLocation = playerNode.getWorldTranslation();
        Vector3f newLocation = new Vector3f(playerLocation).add(new Vector3f(-1f, 1.5f, 2.4f).mult(20f));
        /*cam.setLocation(new Vector3f(cam.getLocation()).interpolate(newLocation, Math.min(tpf, 1f)));
        cam.lookAt(playerLocation, Vector3f.UNIT_Y);*/
        
        for (VehicleNode vehicle : vehicles) {
            vehicle.vehicleInstance.accelerationSmooth = (vehicle.vehicleInstance.accelerationSmooth + vehicle.vehicleInstance.accelerationValue * (tpf * 10f)) / (1 + tpf * 10f);
            vehicle.engineAudio.setVelocity(new Vector3f(0, 0, 0));
            vehicle.engineAudio.updateGeometricState();
            vehicle.engineAudio.setPitch(Math.max(0.5f, Math.min(vehicle.vehicleInstance.accelerationSmooth / vehicle.vehicleInstance.accelerationForce * 2f, 2.0f)));
            boolean engineRunning = (vehicle.vehicleInstance.accelerationValue > 0.01f || vehicle.vehicleInstance.accelerationValue < -0.01f);
            if ((vehicle.engineAudio.getStatus() == Status.Playing) && !engineRunning) {
                vehicle.engineAudio.stop();
            }
            if ((vehicle.engineAudio.getStatus() != Status.Playing) && engineRunning) {
                vehicle.engineAudio.play();
            }

            vehicle.wheelsAudio.updateGeometricState();
            float wheelRot = Math.abs(vehicle.vehicleControl.getWheel(0).getDeltaRotation() + vehicle.vehicleControl.getWheel(1).getDeltaRotation()) / tpf / 100f;
            // TODO: pitch
            //System.out.println("wheel rot: " + wheelRot);
            //wheelsAudio.setPitch(Math.max(0.5f, Math.min(wheelRot * 4f, 2.0f)));
            vehicle.wheelsAudio.setVolume(Math.max(0.0001f, Math.min(wheelRot, 1.0f)) - 0.0001f);
            if ((vehicle.wheelsAudio.getStatus() == Status.Playing) && wheelRot < 10f) {
                vehicle.wheelsAudio.stop();
            }
            if ((vehicle.wheelsAudio.getStatus() != Status.Playing) && wheelRot > 10f) {
                vehicle.wheelsAudio.play();
            }

            vehicle.wheelSlipAudio.updateGeometricState();
            float slipAll = 0f;
            for (int i = 0; i < vehicle.vehicleControl.getNumWheels(); i++) {
                slipAll += vehicle.vehicleControl.getWheel(i).getSkidInfo();
            }
            float slip = 1f - (slipAll) / vehicle.vehicleControl.getNumWheels();
            float wheelSlip = (slip * slip * slip * slip * slip * slip * slip) / tpf / 40f;
            // TODO: pitch
            //wheelsAudio.setPitch(Math.max(0.5f, Math.min(wheelRot * 4f, 2.0f)));
            vehicle.wheelSlipAudio.setVolume(Math.max(0.0001f, Math.min(wheelSlip, 1.0f)) - 0.0001f);
        }
        
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (playerVehicleNode == null) {
            float walkSpeed = 6f;
            if (binding.equals("Lefts")) {
                if (value) {
                    walkDir.x -= walkSpeed;
                } else {
                    walkDir.x += walkSpeed;
                }
            } else if (binding.equals("Rights")) {
                if (value) {
                    walkDir.x += walkSpeed;
                } else {
                    walkDir.x -= walkSpeed;
                }
            } else if (binding.equals("Ups")) {
                if (value) {
                    walkDir.z -= walkSpeed;
                } else {
                    walkDir.z += walkSpeed;
                }
            } else if (binding.equals("Downs")) {
                if (value) {
                    walkDir.z += walkSpeed;
                } else {
                    walkDir.z -= walkSpeed;
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset - to car");
                    Vector3f playerPos = playerNode.getWorldTranslation();
                    for (VehicleNode vehicle : vehicles) {
                        Vector3f vehiclePos = vehicle.getWorldTranslation();
                        float dist = playerPos.distance(vehiclePos);
                        System.out.println(" .. dist: " + dist);
                        if (dist < 5f) {
                            playerVehicleNode = vehicle;
                            playerNode.removeFromParent();
                            playerNode.setLocalTranslation(0f, 0f, -1f);
                            playerNode.setLocalRotation(Quaternion.DIRECTION_Z);
                            playerNode.removeControl(playerPersonControl);
                            playerVehicleNode.attachChild(playerNode);
                            VehicleInstance playerVehicle = playerVehicleNode.vehicleInstance;
                            playerVehicle.accelerationValue = 0;
                            playerVehicle.steeringValue = 0;
                            walkDir = new Vector3f();
                            break;
                        }
                    }
                }
            }
            playerPersonControl.setWalkDirection(walkDir);
            playerPersonControl.setViewDirection(walkDir);
        } else {
            VehicleInstance playerVehicle = playerVehicleNode.vehicleInstance;
            VehicleControl playerVehicleControl = playerVehicleNode.vehicleControl;
            int playerCarType = playerVehicle.carType;
            float steerMax = 0.5f;
            if (playerCarType == VehicleInstance.TRUCK) {
                steerMax = 0.7f;
            }
            if (binding.equals("Lefts")) {
                if (value) {
                    playerVehicle.steeringValue += steerMax;
                } else {
                    playerVehicle.steeringValue += -steerMax;
                }
                playerVehicleControl.steer(playerVehicle.steeringValue);
            } else if (binding.equals("Rights")) {
                if (value) {
                    playerVehicle.steeringValue += -steerMax;
                } else {
                    playerVehicle.steeringValue += steerMax;
                }
                playerVehicleControl.steer(playerVehicle.steeringValue);
            } else if (binding.equals("Ups")) {
                if (value) {
                    playerVehicle.accelerationValue += playerVehicle.accelerationForce;
                } else {
                    playerVehicle.accelerationValue -= playerVehicle.accelerationForce;
                }
                playerVehicleControl.accelerate(2, playerVehicle.accelerationValue);
                playerVehicleControl.accelerate(3, playerVehicle.accelerationValue);
                if (playerCarType == VehicleInstance.TRUCK) {
                    playerVehicleControl.accelerate(4, playerVehicle.accelerationValue);
                    playerVehicleControl.accelerate(5, playerVehicle.accelerationValue);
                }
            } else if (binding.equals("Downs")) {
                float b;
                if (value) {
                    playerVehicle.brakeForce = playerVehicle.accelerationForce;
                } else {
                    playerVehicle.brakeForce = 0f;
                }
                playerVehicleControl.brake(0, playerVehicle.brakeForce);
                playerVehicleControl.brake(1, playerVehicle.brakeForce);
            } else if (binding.equals("Revs")) {
                if (value) {
                    playerVehicle.accelerationValue += playerVehicle.accelerationForce;
                } else {
                    playerVehicle.accelerationValue -= playerVehicle.accelerationForce;
                }
                playerVehicleControl.accelerate(2, -playerVehicle.accelerationValue);
                playerVehicleControl.accelerate(3, -playerVehicle.accelerationValue);
                if (playerCarType == VehicleInstance.TRUCK) {
                    playerVehicleControl.accelerate(4, -playerVehicle.accelerationValue);
                    playerVehicleControl.accelerate(5, -playerVehicle.accelerationValue);
                }
            } else if (binding.equals("Space")) {
                if (value) {
                    playerVehicleControl.applyImpulse(jumpForce, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset - from car");
                    playerNode.removeFromParent();
                    playerNode.addControl(playerPersonControl);
                    playerPersonControl.warp(playerVehicleNode.getLocalTranslation());
                    rootNode.attachChild(playerNode);
                    playerVehicleNode = null;
                    walkDir = new Vector3f();
                    /*playerVehicleControl.setPhysicsLocation(Vector3f.ZERO);
                    playerVehicleControl.setPhysicsRotation(new Matrix3f());
                    playerVehicleControl.setLinearVelocity(Vector3f.ZERO);
                    playerVehicleControl.setAngularVelocity(Vector3f.ZERO);
                    playerVehicleControl.resetSuspension();*/
                } else {
                }
            }
        }
        if (binding.equals("Esc")) {
            stop();
        }
    }
}
