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
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
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
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.terrain.geomipmap.TerrainGridListener;
import com.jme3.terrain.geomipmap.TerrainQuad;
import roadtrip.model.VehicleInstance;
import roadtrip.view.GameWorldView;
import roadtrip.view.VehicleNode;
import roadtrip.view.model.GameWorldState;
import roadtrip.view.model.Player;

/**
 *
 * @author dejvino
 */
public class RoadTrip extends SimpleApplication implements ActionListener {

    public static void main(String[] args) {
        RoadTrip app = new RoadTrip();
        app.start();
    }

    public static boolean DEBUG = false;//true;

    private BulletAppState bulletAppState;

    private GameWorldState gameWorldState;
    private GameWorldView gameWorldView;

    private ChaseCamera chaseCam;

    private Player player = new Player();

    private Vector3f journeyTarget = new Vector3f(50, 0f, 50f);
    private Node targetNode;
    private Node compassNode;
    
    float inputTurning;
    float inputAccel;
    
    BitmapText uiText;
    Node menuBook;
    
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

        gameWorldState = new GameWorldState();
        gameWorldView = GameWorldView.create(gameWorldState, assetManager, cam, rootNode);
        gameWorldView.terrain.terrainGrid.addListener(new TerrainGridListener() {

            @Override
            public void gridMoved(Vector3f newCenter) {
            }

            @Override
            public void tileAttached(Vector3f cell, TerrainQuad quad) {
                while(quad.getControl(RigidBodyControl.class)!=null){
                    quad.removeControl(RigidBodyControl.class);
                }
                quad.addControl(new RigidBodyControl(new HeightfieldCollisionShape(quad.getHeightMap(), gameWorldView.terrain.terrainGrid.getLocalScale()), 0));
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
        
        addTarget();
        addCompass();
        
        menuBook = new Node("menu");
        Geometry book = new Geometry("book", new Box(8, 8, 1));
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Brown);
        book.setMaterial(mat);
        book.setLocalTranslation(0f, 0f, -1.1f);
        menuBook.attachChild(book);
        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        uiText = new BitmapText(fnt, false);
        uiText.setBox(new Rectangle(-5, 4, 12, 12));
        uiText.setAlignment(BitmapFont.Align.Left);
        uiText.setQueueBucket(RenderQueue.Bucket.Transparent);
        uiText.setSize(1.0f);
        uiText.setText("~~~~~~~~~~~~~~~~~~~~\n   Road Trip   \n~~~~~~~~~~~~~~~~~~~~\n"
                + " New Game \n"
                + "$Load Game$\n"
                + " Settings \n"
                + " Credits \n"
                + " Exit\n");
        menuBook.attachChild(uiText);
        rootNode.attachChild(menuBook);
        
        chaseCam = new ChaseCamera(cam, player.node, inputManager);
        chaseCam.setDefaultDistance(60f);
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
        VehicleInstance vehicleInstance = VehicleInstance.createVehicle(gameWorldState.vehicles.size() % VehicleInstance.getVehicleTypesCount());
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
        
        gameWorldState.vehicles.add(vehicle);
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
        player.node = addPerson();
        player.characterControl = player.node.getControl(BetterCharacterControl.class);
    }
    
    private void addTarget()
    {
        Material matTarget = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matTarget.setColor("Color",  ColorRGBA.Red);
        
        Geometry targetGeom = new Geometry("target", new Box(new Vector3f(0.0f, 0f, 0.0f), 1.0f, 1000.0f, 1.0f));
        targetGeom.setMaterial(matTarget);
        
        targetNode = new Node("target");
        targetNode.attachChild(targetGeom);
        rootNode.attachChild(targetNode);
        
        targetNode.setLocalTranslation(journeyTarget);
    }
    
    BitmapText targetText;
    
    private void addCompass()
    {
        Material matRed = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matRed.setColor("Color",  ColorRGBA.Red);
        Material matBlack = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBlack.setColor("Color",  ColorRGBA.Black);
        
        Geometry compassGeomN = new Geometry("compass-N", new Arrow(new Vector3f(0.0f, 0.0f, 1.2f)));
        compassGeomN.setMaterial(matRed);
        Geometry compassGeomS = new Geometry("compass-S", new Arrow(new Vector3f(0.0f, 0.0f, -1.0f)));
        compassGeomS.setMaterial(matBlack);
        Geometry compassGeomW = new Geometry("compass-W", new Arrow(new Vector3f(-1.0f, 0.0f, 0.0f)));
        compassGeomW.setMaterial(matBlack);
        Geometry compassGeomE = new Geometry("compass-E", new Arrow(new Vector3f(1.0f, 0.0f, 0.0f)));
        compassGeomE.setMaterial(matBlack);
        
        compassNode = new Node("compass");
        compassNode.attachChild(compassGeomN);
        compassNode.attachChild(compassGeomS);
        compassNode.attachChild(compassGeomW);
        compassNode.attachChild(compassGeomE);
        
        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        targetText = new BitmapText(fnt, false);
        targetText.setBox(new Rectangle(-5, 4, 10, 4));
        targetText.setAlignment(BitmapFont.Align.Center);
        targetText.setQueueBucket(RenderQueue.Bucket.Transparent);
        targetText.setSize( 1.2f );
        targetText.setText("Target");
        targetText.setLocalRotation(new Quaternion().fromAngles(0, 3.1415f, 0));
        compassNode.attachChild(targetText);
        
        rootNode.attachChild(compassNode);
    }
    

    
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerLocation = player.node.getWorldTranslation();
        Vector3f newLocation = new Vector3f(playerLocation).add(new Vector3f(-1f, 1.5f, 2.4f).mult(20f));

        for (VehicleNode vehicle : gameWorldState.vehicles) {
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
       
        menuBook.setLocalTranslation(cam.getLocation().add(cam.getRotation().mult(Vector3f.UNIT_Z).mult(30.0f)));
        Quaternion textRot = new Quaternion();
        textRot.lookAt(new Vector3f(player.node.getWorldTranslation()).subtractLocal(cam.getLocation()).negate(), Vector3f.UNIT_Y);
        menuBook.setLocalRotation(textRot);
        
        if (player.vehicleNode == null) {
            player.characterControl.setViewDirection(new Quaternion().fromAngleAxis(inputTurning * tpf, Vector3f.UNIT_Y).mult(player.characterControl.getViewDirection()));
            player.characterControl.setWalkDirection(new Vector3f(player.characterControl.getViewDirection()).mult(inputAccel * tpf * 1000f));
        }
        
        Vector3f playerPos2d = new Vector3f(player.node.getWorldTranslation());
        playerPos2d.y = 0;
        Vector3f targetPos2d = new Vector3f(targetNode.getWorldTranslation());
        targetPos2d.y = 0;
        Vector3f targetDir = targetPos2d.subtract(playerPos2d);
        float targetDistance = targetDir.length();
        if (targetDistance < 5f) {
            double angle = Math.random() * 2d - 1d;
            journeyTarget = journeyTarget.add(new Quaternion().fromAngleAxis((float) angle, Vector3f.UNIT_Y).mult(Vector3f.UNIT_Z).mult(100f));
            targetNode.setLocalTranslation(journeyTarget);
        }
        
        targetText.setText(((int)targetDistance) + " m");
        
        compassNode.setLocalTranslation(new Vector3f(player.node.getWorldTranslation()).addLocal(0f, 5f, 0f));
        compassNode.setLocalRotation(new Quaternion().fromAngles(0f, (float)Math.atan2(targetDir.x, targetDir.z)/*targetDir.angleBetween(Vector3f.UNIT_Z)*/, 0f));
    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (player.vehicleNode == null) {
            float walkSpeed = 1f;
            float turnSpeed = 1f;
            if (binding.equals("Lefts")) {
                if (value) {
                    inputTurning += turnSpeed;
                } else {
                    inputTurning -= turnSpeed;
                }
            } else if (binding.equals("Rights")) {
                if (value) {
                    inputTurning -= turnSpeed;
                } else {
                    inputTurning += turnSpeed;
                }
            } else if (binding.equals("Ups")) {
                if (value) {
                    inputAccel += walkSpeed;
                } else {
                    inputAccel -= walkSpeed;
                }
            } else if (binding.equals("Downs")) {
                if (value) {
                    inputAccel -= walkSpeed;
                } else {
                    inputAccel += walkSpeed;
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset - to car");
                    Vector3f playerPos = player.node.getWorldTranslation();
                    for (VehicleNode vehicle : gameWorldState.vehicles) {
                        Vector3f vehiclePos = vehicle.getWorldTranslation();
                        float dist = playerPos.distance(vehiclePos);
                        System.out.println(" .. dist: " + dist);
                        if (dist < 5f) {
                            player.vehicleNode = vehicle;
                            player.node.removeFromParent();
                            player.node.setLocalTranslation(0f, 0f, -1f);
                            player.node.setLocalRotation(Quaternion.DIRECTION_Z);
                            player.node.removeControl(player.characterControl);
                            player.vehicleNode.attachChild(player.node);
                            VehicleInstance playerVehicle = player.vehicleNode.vehicleInstance;
                            playerVehicle.accelerationValue = 0;
                            playerVehicle.steeringValue = 0;
                            player.walkDir = new Vector3f();
                            break;
                        }
                    }
                }
            }
        } else {
            VehicleInstance playerVehicle = player.vehicleNode.vehicleInstance;
            VehicleControl playerVehicleControl = player.vehicleNode.vehicleControl;
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
                    playerVehicleControl.applyImpulse(player.jumpForce, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset - from car");
                    player.node.removeFromParent();
                    player.node.addControl(player.characterControl);
                    player.characterControl.warp(player.vehicleNode.getLocalTranslation());
                    rootNode.attachChild(player.node);
                    player.vehicleNode = null;
                    player.walkDir = new Vector3f();
                } else {
                }
            }
        }
        if (binding.equals("Esc")) {
            stop();
        }
    }
}
