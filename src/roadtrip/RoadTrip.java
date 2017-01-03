package roadtrip;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.audio.AudioSource.Status;
import com.jme3.audio.Environment;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import java.util.LinkedList;
import java.util.List;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

/**
 *
 * @author dejvino
 */
public class RoadTrip extends SimpleApplication implements ActionListener {

    public static boolean DEBUG = false;//true;
    
    private BulletAppState bulletAppState;
    
    private VehicleNode playerVehicleNode;
    
    private List<VehicleNode> vehicles = new LinkedList<>();
    
    private static class WeakVehicle extends VehicleInstance
    {
        WeakVehicle()
        {
            super(WEAK, 200.0f, 100.0f);
        }
    }
    
    private static class TruckVehicle extends VehicleInstance
    {
        TruckVehicle()
        {
            super(TRUCK, 1400.0f, 200.0f);
        }
    }
    
    private static class SportVehicle extends VehicleInstance
    {
        SportVehicle()
        {
            super(SPORT, 20000.0f, 200.0f);
        }
    }
    
    private static class VehicleInstance
    {
        final static int WEAK = 1;
        final static int TRUCK = 2;
        final static int SPORT = 3;
        final static int FOOT = 4;
    
        int carType;
        float accelerationForce = 200.0f;
        float brakeForce = 100.0f;
        float steeringValue = 0;
        float accelerationValue = 0;
        float accelerationSmooth = 0;
        
        VehicleInstance(int carType, float accelerationForce, float brakeForce)
        {
            this.carType = carType;
            this.accelerationForce = accelerationForce;
            this.brakeForce = brakeForce;
        }
    }
    
    private static class VehicleNode extends Node
    {
        VehicleInstance vehicleInstance;
        
        VehicleControl vehicleControl;
        
        Spatial vehicleModel;
        
        AudioNode engineAudio;
        AudioNode wheelsAudio;
        AudioNode wheelSlipAudio;

        public VehicleNode(String name, VehicleInstance vehicleInstance,
                VehicleControl vehicleControl, Spatial vehicleModel)
        {
            super(name);
            this.vehicleInstance = vehicleInstance;
            this.vehicleControl = vehicleControl;
            this.vehicleModel = vehicleModel;
        }
    }
    
    private Node playerNode;
    private BetterCharacterControl playerPersonControl;
    private Vector3f jumpForce = new Vector3f(0, 3000, 0);
    private Vector3f walkDir = new Vector3f();

    public static void main(String[] args)
    {
        RoadTrip app = new RoadTrip();
        app.start();
    }
    
    Spatial map;
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        if (DEBUG) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        setupKeys();
        
        //audioRenderer.setEnvironment(Environment.Dungeon);
        //AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
        
        addMap();
        
        addPlayer();
        
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
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Revs", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Revs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }

    private void addCar()
    {
        Node vehicleModel = new Node("VehicleModel");
        VehicleInstance vehicleInstance = new WeakVehicle();
        
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Black);

        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.4f, 0.5f, 3.6f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
        
        if (vehicleInstance.carType == VehicleInstance.TRUCK) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.4f, 0.8f, 1.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, 2f));
        } else if (vehicleInstance.carType == VehicleInstance.SPORT) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.2f, 0.6f, 2.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, -1f));
        }

        VehicleControl vehicleControl = new VehicleControl(compoundShape, 500);
        vehicleModel.addControl(vehicleControl);
        
        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 30.0f;//200=f1 car
        float compValue = .1f; //(should be lower than damp)
        float dampValue = .2f;
        vehicleControl.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicleControl.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicleControl.setSuspensionStiffness(stiffness);
        vehicleControl.setMaxSuspensionForce(10000.0f);

        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 1.0f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1.6f;
        float zOff = 2f;

        Geometry carBody = new Geometry("car body", new Box(new Vector3f(0.0f, 1f, 0.0f), 1.4f, 0.5f, 3.6f));
        Material matBody = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        matBody.setColor("Color", ColorRGBA.Red);
        carBody.setMaterial(matBody);
        vehicleModel.attachChild(carBody);
        
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
        
        rootNode.attachChild(vehicleModel);

        getPhysicsSpace().add(vehicleControl);
        vehicleControl.setPhysicsLocation(new Vector3f(5f, 30f, 5f));
        
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
                
        vehicle.engineAudio  = new AudioNode(assetManager, "Sounds/engine.ogg", false);
        vehicle.engineAudio.setPositional(true);
        vehicle.engineAudio.setLooping(true);
        vehicle.engineAudio.setReverbEnabled(true);
        vehicle.engineAudio.setRefDistance(10);
        vehicle.engineAudio.setMaxDistance(100000000);
        vehicle.attachChild(vehicle.engineAudio);
        
        vehicle.wheelsAudio  = new AudioNode(assetManager, "Sounds/wheels.ogg", false);
        vehicle.wheelsAudio.setPositional(true);
        vehicle.wheelsAudio.setLooping(true);
        //wheelsAudio.setReverbEnabled(true);
        vehicle.wheelsAudio.setRefDistance(10);
        vehicle.wheelsAudio.setMaxDistance(100000000);
        vehicle.wheelsAudio.play();
        vehicle.attachChild(vehicle.wheelsAudio);
        
        vehicle.wheelSlipAudio  = new AudioNode(assetManager, "Sounds/wheel-slip.ogg", false);
        vehicle.wheelSlipAudio.setPositional(true);
        vehicle.wheelSlipAudio.setLooping(true);
        //wheelsAudio.setReverbEnabled(true);
        vehicle.wheelSlipAudio.setRefDistance(10);
        vehicle.wheelSlipAudio.setMaxDistance(100000000);
        vehicle.wheelSlipAudio.play();
        vehicle.attachChild(vehicle.wheelSlipAudio);
        
        vehicle.setLocalTranslation(new Vector3f(10f + (float)Math.random() * 20f, 30f, 12f + (float)Math.random() * 20f));
        
        vehicles.add(vehicle);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerLocation = playerNode.getLocalTranslation();
        Vector3f newLocation = new Vector3f(playerLocation).add(new Vector3f(-1f, 1.5f, 2.4f).mult(20f));
        cam.setLocation(new Vector3f(cam.getLocation()).interpolate(newLocation, Math.min(tpf, 1f)));
        cam.lookAt(playerLocation, Vector3f.UNIT_Y);
        
        for (VehicleNode vehicle : vehicles) {
            vehicle.vehicleInstance.accelerationSmooth = (vehicle.vehicleInstance.accelerationSmooth + vehicle.vehicleInstance.accelerationValue * (tpf * 10f)) / (1 + tpf * 10f);
            //engineAudio.setVelocity(new Vector3f(0, 0, 0));
            //engineAudio.setLocalTranslation(x, 0, z);
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
            float wheelRot = Math.abs(vehicle.vehicleControl.getWheel(0).getDeltaRotation() + vehicle.vehicleControl.getWheel(1).getDeltaRotation()) / tpf / 40f;
            // TODO: pitch
            //System.out.println("wheel rot: " + wheelRot);
            //wheelsAudio.setPitch(Math.max(0.5f, Math.min(wheelRot * 4f, 2.0f)));
            vehicle.wheelsAudio.setVolume(Math.max(0.0001f, Math.min(wheelRot, 1.0f)) - 0.0001f);

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
            }
            playerPersonControl.setWalkDirection(walkDir);
            playerPersonControl.setViewDirection(walkDir);
        } else {
            float steerMax = 0.5f;
            if (playerVehicleNode.vehicleInstance.carType == VehicleInstance.TRUCK) {
                steerMax = 0.7f;
            }
            if (binding.equals("Lefts")) {
                if (value) {
                    playerVehicleNode.vehicleInstance.steeringValue += steerMax;
                } else {
                    playerVehicleNode.vehicleInstance.steeringValue += -steerMax;
                }
                playerVehicleNode.vehicleControl.steer(playerVehicleNode.vehicleInstance.steeringValue);
            } else if (binding.equals("Rights")) {
                if (value) {
                    playerVehicleNode.vehicleInstance.steeringValue += -steerMax;
                } else {
                    playerVehicleNode.vehicleInstance.steeringValue += steerMax;
                }
                playerVehicleNode.vehicleControl.steer(playerVehicleNode.vehicleInstance.steeringValue);
            } else if (binding.equals("Ups")) {
                if (value) {
                    playerVehicleNode.vehicleInstance.accelerationValue += playerVehicleNode.vehicleInstance.accelerationForce;
                } else {
                    playerVehicleNode.vehicleInstance.accelerationValue -= playerVehicleNode.vehicleInstance.accelerationForce;
                }
                playerVehicleNode.vehicleControl.accelerate(2, playerVehicleNode.vehicleInstance.accelerationValue);
                playerVehicleNode.vehicleControl.accelerate(3, playerVehicleNode.vehicleInstance.accelerationValue);
                if (playerVehicleNode.vehicleInstance.carType == VehicleInstance.TRUCK) {
                    playerVehicleNode.vehicleControl.accelerate(4, playerVehicleNode.vehicleInstance.accelerationValue);
                    playerVehicleNode.vehicleControl.accelerate(5, playerVehicleNode.vehicleInstance.accelerationValue);
                }
            } else if (binding.equals("Downs")) {
                float b;
                if (value) {
                    b = playerVehicleNode.vehicleInstance.brakeForce;
                } else {
                    b = 0f;
                }
                playerVehicleNode.vehicleControl.brake(0, b);
                playerVehicleNode.vehicleControl.brake(1, b);
            } else if (binding.equals("Revs")) {
                if (value) {
                    playerVehicleNode.vehicleInstance.accelerationValue += playerVehicleNode.vehicleInstance.accelerationForce;
                } else {
                    playerVehicleNode.vehicleInstance.accelerationValue -= playerVehicleNode.vehicleInstance.accelerationForce;
                }
                playerVehicleNode.vehicleControl.accelerate(2, -playerVehicleNode.vehicleInstance.accelerationValue);
                playerVehicleNode.vehicleControl.accelerate(3, -playerVehicleNode.vehicleInstance.accelerationValue);
                if (playerVehicleNode.vehicleInstance.carType == VehicleInstance.TRUCK) {
                    playerVehicleNode.vehicleControl.accelerate(4, -playerVehicleNode.vehicleInstance.accelerationValue);
                    playerVehicleNode.vehicleControl.accelerate(5, -playerVehicleNode.vehicleInstance.accelerationValue);
                }
            } else if (binding.equals("Space")) {
                if (value) {
                    playerVehicleNode.vehicleControl.applyImpulse(jumpForce, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset");
                    playerVehicleNode.vehicleControl.setPhysicsLocation(Vector3f.ZERO);
                    playerVehicleNode.vehicleControl.setPhysicsRotation(new Matrix3f());
                    playerVehicleNode.vehicleControl.setLinearVelocity(Vector3f.ZERO);
                    playerVehicleNode.vehicleControl.setAngularVelocity(Vector3f.ZERO);
                    playerVehicleNode.vehicleControl.resetSuspension();
                } else {
                }
            }
        }
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
        getPhysicsSpace().addAll(person);
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
        map = assetManager.loadModel("Scenes/TestMap.j3o");
        rootNode.attachChild(map);
        getPhysicsSpace().addAll(map);
    }
}
