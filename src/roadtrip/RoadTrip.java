package roadtrip;

import com.jme3.app.SimpleApplication;
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

/**
 *
 * @author dejvino
 */
public class RoadTrip extends SimpleApplication implements ActionListener {

    public static boolean DEBUG = false;//true;
    final int WEAK = 1;
    final int TRUCK = 2;
    final int SPORT = 3;
    final int FOOT = 4;
    
    final int carType = FOOT;
    
    private BulletAppState bulletAppState;
    private VehicleControl vehicle;
    private float accelerationForce = 200.0f;
    private float brakeForce = 100.0f;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private Vector3f jumpForce = new Vector3f(0, 3000, 0);
    private Vector3f walkDir = new Vector3f();

    public static void main(String[] args) {
        RoadTrip app = new RoadTrip();
        app.start();
    }

    Node playerNode;
    
    Spatial map;
    Spatial car;
    private BetterCharacterControl playerPersonControl;
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        if (DEBUG) bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        setupKeys();
        
        map = assetManager.loadModel("Scenes/TestMap.j3o");
        rootNode.attachChild(map);
        getPhysicsSpace().addAll(map);
        
        if (carType == FOOT) {
            addPlayerPerson();
        } else {
            addPlayerCar();
        }
        
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

    private void addPlayerCar() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Black);

        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.4f, 0.5f, 3.6f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));
        
        if (carType == TRUCK) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.4f, 0.8f, 1.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, 2f));
        } else if (carType == SPORT) {
            BoxCollisionShape boxCabin = new BoxCollisionShape(new Vector3f(1.2f, 0.6f, 2.0f));
            compoundShape.addChildShape(boxCabin, new Vector3f(0, 2, -1f));
        }

        //create vehicle node
        Node vehicleNode=new Node("vehicleNode");
        vehicle = new VehicleControl(compoundShape, 500);
        vehicleNode.addControl(vehicle);

        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 30.0f;//200=f1 car
        float compValue = .1f; //(should be lower than damp)
        float dampValue = .2f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);

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
        vehicleNode.attachChild(carBody);
        
        Cylinder wheelMesh = new Cylinder(16, 16, radius, radius * 0.2f, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        vehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        vehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);
        
        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);
        
        if (carType == TRUCK) {
            Node node5 = new Node("wheel 5 node");
            Geometry wheels5 = new Geometry("wheel 5", wheelMesh);
            node5.attachChild(wheels5);
            wheels5.rotate(0, FastMath.HALF_PI, 0);
            wheels5.setMaterial(mat);
            vehicle.addWheel(node5, new Vector3f(-xOff, yOff, 2.1f* -zOff),
                    wheelDirection, wheelAxle, restLength, radius, false);

            Node node6 = new Node("wheel 6 node");
            Geometry wheels6 = new Geometry("wheel 6", wheelMesh);
            node6.attachChild(wheels6);
            wheels6.rotate(0, FastMath.HALF_PI, 0);
            wheels6.setMaterial(mat);
            vehicle.addWheel(node6, new Vector3f(xOff, yOff, 2.1f* -zOff),
                    wheelDirection, wheelAxle, restLength, radius, false);
            
            vehicleNode.attachChild(node5);
            vehicleNode.attachChild(node6);
        }
        
        rootNode.attachChild(vehicleNode);

        getPhysicsSpace().add(vehicle);
        vehicle.setPhysicsLocation(new Vector3f(5f, 30f, 5f));
        
        vehicle.getWheel(0).setFrictionSlip(0.8f);
        vehicle.getWheel(1).setFrictionSlip(0.8f);
        vehicle.getWheel(2).setFrictionSlip(0.6f);
        vehicle.getWheel(3).setFrictionSlip(0.6f);
            
        if (carType == TRUCK) {
            vehicle.getWheel(4).setFrictionSlip(0.6f);
            vehicle.getWheel(5).setFrictionSlip(0.6f);
        
            accelerationForce = 1400f;
            brakeForce = 200f;
        } else if (carType == SPORT) {
            accelerationForce = 20000f;
            brakeForce = 200f;
        
        }
        vehicle.setPhysicsLocation(new Vector3f(5f, 30f, 5f));
        
        playerNode = vehicleNode;
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerLocation = playerNode.getLocalTranslation();
        Vector3f newLocation = new Vector3f(playerLocation).add(new Vector3f(-1f, 1.5f, 2.4f).mult(20f));
        cam.setLocation(new Vector3f(cam.getLocation()).interpolate(newLocation, Math.min(tpf, 1f)));
        cam.lookAt(playerLocation, Vector3f.UNIT_Y);
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (carType == FOOT) {
            float walkSpeed = 3f;
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
            if (carType == TRUCK) {
                steerMax = 0.7f;
            }
            if (binding.equals("Lefts")) {
                if (value) {
                    steeringValue += steerMax;
                } else {
                    steeringValue += -steerMax;
                }
                vehicle.steer(steeringValue);
            } else if (binding.equals("Rights")) {
                if (value) {
                    steeringValue += -steerMax;
                } else {
                    steeringValue += steerMax;
                }
                vehicle.steer(steeringValue);
            } else if (binding.equals("Ups")) {
                if (value) {
                    accelerationValue += accelerationForce;
                } else {
                    accelerationValue -= accelerationForce;
                }
                vehicle.accelerate(2, accelerationValue);
                vehicle.accelerate(3, accelerationValue);
                if (carType == TRUCK) {
                    vehicle.accelerate(4, accelerationValue);
                    vehicle.accelerate(5, accelerationValue);
                }
            } else if (binding.equals("Downs")) {
                float b;
                if (value) {
                    b = brakeForce;
                } else {
                    b = 0f;
                }
                vehicle.brake(0, b);
                vehicle.brake(1, b);
            } else if (binding.equals("Revs")) {
                if (value) {
                    accelerationValue += accelerationForce;
                } else {
                    accelerationValue -= accelerationForce;
                }
                vehicle.accelerate(2, -accelerationValue);
                vehicle.accelerate(3, -accelerationValue);
                if (carType == TRUCK) {
                    vehicle.accelerate(4, -accelerationValue);
                    vehicle.accelerate(5, -accelerationValue);
                }
            } else if (binding.equals("Space")) {
                if (value) {
                    vehicle.applyImpulse(jumpForce, Vector3f.ZERO);
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    System.out.println("Reset");
                    vehicle.setPhysicsLocation(Vector3f.ZERO);
                    vehicle.setPhysicsRotation(new Matrix3f());
                    vehicle.setLinearVelocity(Vector3f.ZERO);
                    vehicle.setAngularVelocity(Vector3f.ZERO);
                    vehicle.resetSuspension();
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
        personControl.warp(new Vector3f(10f + (float)Math.random() * 10f, 30f, 12f + (float)Math.random() * 10f));/**/
        //personControl.setPhysicsLocation(new Vector3f(10f, 30f, 12f));
        getPhysicsSpace().add(personControl);
        getPhysicsSpace().addAll(person);
        rootNode.attachChild(person);
        Vector3f dir = new Vector3f((float)Math.random() * 2f - 1f, 0f, (float)Math.random() * 2f - 1f);
        personControl.setViewDirection(dir);
        personControl.setWalkDirection(dir);
        
        return person;
    }

    private void addPlayerPerson()
    {
        playerNode = addPerson();
        playerPersonControl = playerNode.getControl(BetterCharacterControl.class);
    }
}
