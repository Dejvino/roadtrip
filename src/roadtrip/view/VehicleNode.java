/*
 */
package roadtrip.view;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import roadtrip.model.VehicleInstance;

/**
 *
 * @author dejvino
 */
public class VehicleNode extends Node
{
    public VehicleInstance vehicleInstance;

    public VehicleControl vehicleControl;

    public Node vehicleModel;

    public AudioNode engineAudio;
    public AudioNode wheelsAudio;
    public AudioNode wheelSlipAudio;

    public VehicleNode(String name, VehicleInstance vehicleInstance)
    {
        super(name);
        this.vehicleInstance = vehicleInstance;
    }

	public void initialize(AssetManager assetManager)
	{
		vehicleModel = new Node("VehicleModel");
		
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
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

		Material matBody = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		matBody.setFloat("Shininess", 32f);
		matBody.setBoolean("UseMaterialColors", true);
		matBody.setColor("Ambient",  ColorRGBA.Black);
		matBody.setColor("Diffuse",  ColorRGBA.Red);
		matBody.setColor("Specular", ColorRGBA.White);


		if (vehicleInstance.carType == VehicleInstance.WEAK) {
			Spatial carBody = assetManager.loadModel("Models/rivercrossing.j3o");
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

		vehicleControl = new VehicleControl(compoundShape, 500);

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

		VehicleNode vehicle = this;
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
		vehicleControl.setPhysicsLocation(new Vector3f(10f + (float)Math.random() * 40f, 28f, 12f + (float)Math.random() * 40f));
	}

	public void update(float tpf)
	{
		VehicleNode vehicle = this;
		vehicle.vehicleInstance.accelerationSmooth = (vehicle.vehicleInstance.accelerationSmooth + vehicle.vehicleInstance.accelerationValue * (tpf * 10f)) / (1 + tpf * 10f);
		vehicle.engineAudio.setVelocity(new Vector3f(0, 0, 0));
		vehicle.engineAudio.updateGeometricState();
		vehicle.engineAudio.setPitch(Math.max(0.5f, Math.min(vehicle.vehicleInstance.accelerationSmooth / vehicle.vehicleInstance.accelerationForce * 2f, 2.0f)));
		boolean engineRunning = (vehicle.vehicleInstance.accelerationValue > 0.01f || vehicle.vehicleInstance.accelerationValue < -0.01f);
		if ((vehicle.engineAudio.getStatus() == AudioSource.Status.Playing) && !engineRunning) {
			vehicle.engineAudio.stop();
		}
		if ((vehicle.engineAudio.getStatus() != AudioSource.Status.Playing) && engineRunning) {
			vehicle.engineAudio.play();
		}

		vehicle.wheelsAudio.updateGeometricState();
		float wheelRot = Math.abs(vehicle.vehicleControl.getWheel(0).getDeltaRotation() + vehicle.vehicleControl.getWheel(1).getDeltaRotation()) / tpf / 100f;
		// TODO: pitch
		//System.out.println("wheel rot: " + wheelRot);
		//wheelsAudio.setPitch(Math.max(0.5f, Math.min(wheelRot * 4f, 2.0f)));
		vehicle.wheelsAudio.setVolume(Math.max(0.0001f, Math.min(wheelRot, 1.0f)) - 0.0001f);
		if ((vehicle.wheelsAudio.getStatus() == AudioSource.Status.Playing) && wheelRot < 10f) {
			vehicle.wheelsAudio.stop();
		}
		if ((vehicle.wheelsAudio.getStatus() != AudioSource.Status.Playing) && wheelRot > 10f) {
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
}