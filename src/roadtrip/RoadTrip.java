package roadtrip;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import roadtrip.model.VehicleInstance;
import roadtrip.view.CompassNode;
import roadtrip.view.GameMenuNode;
import roadtrip.view.GameWorldView;
import roadtrip.view.HideControl;
import roadtrip.view.VehicleNode;
import roadtrip.view.model.CameraType;
import roadtrip.view.model.GameWorldState;
import roadtrip.view.model.Player;

/**
 *
 * @author dejvino
 */
public class RoadTrip extends GameApplication implements ActionListener, AnalogListener {

    public static void main(String[] args) {
        RoadTrip app = new RoadTrip();
        app.start();
    }

    public static boolean DEBUG = !true;

    private GameWorldState gameWorldState;
    private GameWorldView gameWorldView;

    private GameMenuNode gameMenuNode;

    private CameraType cameraType = CameraType.FIRST_PERSON;
    private ChaseCamera chaseCam;
    private float camFov = 85;

    private Player player = new Player();

    private Vector3f journeyTarget = new Vector3f(50, 0f, 50f);
    private Node targetNode;
    private CompassNode compassNode;
    
    float inputTurning;
    float inputAccel;
    
    float lookX, lookY;

    int score = 0;

    private FilterPostProcessor fpp;
    DepthOfFieldFilter dofFilter;
    
    @Override
    public void initializeGame() {
        super.initializeGame();

        bulletAppState.setDebugEnabled(DEBUG);
        if (DEBUG) {
            attachDebugStates();
        }
        
        inputManager.setCursorVisible(false);
        setupKeys();

        //audioRenderer.setEnvironment(Environment.Dungeon);
        //AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
        
        // Environment
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.LightGray);
        dl.setDirection(new Vector3f(1, -1, 1));
        rootNode.addLight(dl);
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        rootNode.addLight(al);

        gameWorldState = new GameWorldState(1L);
        gameWorldView = GameWorldView.create(gameWorldState, assetManager, cam, rootNode, getPhysicsSpace(), new HideControl.TargetProvider() {

            @Override
            public Vector3f getTarget() {
                return player.node.getWorldTranslation();
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
	addGameMenu();
        
        cam.setFrustumPerspective(camFov, settings.getWidth() / ((float)settings.getHeight()), 0.5f, 150f);
        
        chaseCam = new ChaseCamera(cam, player.node, inputManager);
        chaseCam.setDefaultDistance(60f);
        chaseCam.setSmoothMotion(true);
        
        fpp = new FilterPostProcessor(assetManager);
        //fpp.setNumSamples(4);
        
        FogFilter fog=new FogFilter();
        fog.setFogColor(new ColorRGBA(0.02f, 0.02f, 0.05f, 1.0f));
        fog.setFogDistance(1000);
        fog.setFogDensity(3.0f);
        fpp.addFilter(fog);
        
        dofFilter = new DepthOfFieldFilter();
        dofFilter.setFocusRange(5f);
        dofFilter.setFocusDistance(6f);
        dofFilter.setBlurScale(0.6f);
        fpp.addFilter(dofFilter);
        
        viewPort.addProcessor(fpp);
        
        viewPort.addProcessor(fpp);
    }

	protected void addGameMenu()
	{
		gameMenuNode = new GameMenuNode();
		gameMenuNode.initialize(getAssetManager());
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
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("View", new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping("LookX-", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("LookX+", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("LookY-", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("LookY+", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addListener(this, new String[] {
                "Lefts", "Rights", "Ups", "Downs", "Revs", "Space", "Reset", "Esc", "Pause", "View",
                "LookX-", "LookX+", "LookY-", "LookY+"
            });
    }

    private void addCar()
    {
	    VehicleInstance vehicleInstance = VehicleInstance.createVehicle(gameWorldState.vehicles.size() % VehicleInstance.getVehicleTypesCount());
	    vehicleInstance.brakeForce = vehicleInstance.accelerationForce;

        VehicleNode vehicle = new VehicleNode("Car " + vehicleInstance.toString(), vehicleInstance);
        vehicle.initialize(assetManager);

        vehicle.vehicleControl.setPhysicsLocation(new Vector3f(10f + (float)Math.random() * 40f, 228f, 12f + (float)Math.random() * 40f));

        gameWorldState.vehicles.add(vehicle);
	    getPhysicsSpace().addAll(vehicle);
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
        personControl.warp(new Vector3f(10f + (float)Math.random() * 20f, 230f, 12f + (float)Math.random() * 20f));/**/
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
        player.headNode = new Node("head");
        player.headNode.setLocalTranslation(0f, 2.5f, 0f);
        player.node.attachChild(player.headNode);
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

    private void addCompass()
    {
        compassNode = new CompassNode();
        compassNode.initialize(getAssetManager());
        rootNode.attachChild(compassNode);
    }
    
    @Override
    public void updateGame(float tpf) {
        float focusDist = 20f;
        chaseCam.setEnabled(cameraType == CameraType.CHASE);
        if (cameraType == CameraType.CHASE) {
            focusDist = cam.getLocation().distance(player.node.getWorldTranslation()) / 10f;
        }
        dofFilter.setFocusDistance(focusDist * 1.1f);
        dofFilter.setFocusRange(focusDist * 0.9f);
        
        for (VehicleNode vehicle : gameWorldState.vehicles) {
            vehicle.update(tpf);
        }
        
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
       
        gameMenuNode.moveToCamera(cam, player.node);
        
        if (player.vehicleNode == null) {
            player.characterControl.setViewDirection(new Quaternion().fromAngleAxis(inputTurning * tpf, Vector3f.UNIT_Y).mult(player.characterControl.getViewDirection()));
            player.characterControl.setWalkDirection(new Vector3f(player.characterControl.getViewDirection()).mult(inputAccel * tpf * 1000f).multLocal(1f, 0f, 1f));
        }
        
        Vector3f playerPos2d = new Vector3f(player.node.getWorldTranslation());
        playerPos2d.y = 0;
        Vector3f targetPos2d = new Vector3f(targetNode.getWorldTranslation());
        targetPos2d.y = 0;
        Vector3f targetDir = targetPos2d.subtract(playerPos2d);
        float targetDistance = targetDir.length();
        if (targetDistance < 5f) {
	        onReachedTarget();
        }
        
        compassNode.setTargetText(((int)targetDistance) + " m");
        compassNode.setLocalTranslation(new Vector3f(player.node.getWorldTranslation()).addLocal(0f, 5f, 0f));
        compassNode.setLocalRotation(new Quaternion().fromAngles(0f, (float)Math.atan2(targetDir.x, targetDir.z)/*targetDir.angleBetween(Vector3f.UNIT_Z)*/, 0f));
    }

    @Override
    protected void updatePreRender(float tpf) {
        super.updatePreRender(tpf);
        
        Vector3f playerLocation = player.headNode.getWorldTranslation();
        Vector3f newLocation = new Vector3f(playerLocation).add(new Vector3f(-1f, 1.5f, 2.4f).mult(20f));

        if (cameraType == CameraType.FIRST_PERSON) {
            cam.setLocation(new Vector3f().addLocal(playerLocation));
            Vector3f lookAtDir = new Vector3f((float)Math.cos(lookX), (float)Math.tan(lookY - 1f), (float)Math.sin(lookX)).normalizeLocal();
            Quaternion camRot = new Quaternion();
            camRot.lookAt(lookAtDir, Vector3f.UNIT_Y);
            cam.setRotation(camRot);
            player.characterControl.setViewDirection(lookAtDir);
        }
    }
    
	protected void onReachedTarget()
	{
		score++;
		gameMenuNode.setMenuEntriesPrefix("SCORE: " + score + "\n\n");

		double angle = Math.random() * 2d - 1d;
		journeyTarget = journeyTarget.add(new Quaternion().fromAngleAxis((float) angle, Vector3f.UNIT_Y).mult(Vector3f.UNIT_Z).mult(100f));
		targetNode.setLocalTranslation(journeyTarget);
	}

	@Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("View")) {
            if (value) {
                cameraType = cameraType == CameraType.CHASE ? CameraType.FIRST_PERSON : CameraType.CHASE;
            }
        }
        if (gamePaused) {
            if (binding.equals("Lefts")) {
                // meh
            } else if (binding.equals("Rights")) {
                // meh
            } else if (binding.equals("Ups")) {
                if (value) {
                    gameMenuNode.nextMenuEntry();
                }
            } else if (binding.equals("Downs")) {
                if (value) {
                    gameMenuNode.prevMenuEntry();
                }
            } else if (binding.equals("Reset")) {
                if (value) {
                    switch (gameMenuNode.getMenuEntryIndex()) {
                        case 0: // New
                            break;
                        case 1: // Load
                            break;
                        case 2: // Settings
                            break;
                        case 3: // Credits
                            break;
                        case 4: // Exit
                            stop();
                            break;
                        default:
                            throw new RuntimeException("Unrecognized menu entry: " + gameMenuNode.getMenuEntryIndex());
                    }
                }
            } else if (binding.equals("Esc")) {
                // TODO: hide menu
                if (value) {
                    setGamePaused(false);
                }
            }
        } else {
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
                // TODO: open menu
                if (value) {
                    setGamePaused(true);
                }
            } else if (binding.equals("Pause")) {
                if (value) {
                    setGamePaused(!gamePaused);
                }
            }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (gamePaused) { return; }
        float speedX = 100f;
        float speedY = 100f;
        if ("LookX-".equals(name)) {
            lookX += -value * tpf * speedX;
        }
        if ("LookX+".equals(name)) {
            lookX += value * tpf * speedX;
        }
        if ("LookY-".equals(name)) {
            lookY += -value * tpf * speedY;
        }
        if ("LookY+".equals(name)) {
            lookY += value * tpf * speedY;
        }
        lookY = (float)Math.max(0, Math.min(lookY, 2f));
    }
 
    @Override
    protected void onGamePause(boolean paused) {
        super.onGamePause(paused);
        
        if (paused) {
            rootNode.attachChild(gameMenuNode);
        } else {
            gameMenuNode.removeFromParent();
        }
    }
}
