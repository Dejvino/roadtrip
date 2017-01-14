/*
 */
package roadtrip.model;

/**
 *
 * @author dejvino
 */
public class VehicleInstance
{
    public static final int WEAK = 1;
    public static final int TRUCK = 2;
    public static final int SPORT = 3;

    public static int getVehicleTypesCount() {
        return SPORT;
    }

    public int carType;
    public float accelerationForce = 200.0f;
    public float brakeForce = 100.0f;
    public float steeringValue = 0;
    public float accelerationValue = 0;
    public float accelerationSmooth = 0;

    VehicleInstance(int carType, float accelerationForce, float brakeForce)
    {
        this.carType = carType;
        this.accelerationForce = accelerationForce;
        this.brakeForce = brakeForce;
    }
    
    public static class WeakVehicle extends VehicleInstance
    {
        WeakVehicle()
        {
            super(WEAK, 400.0f, 100.0f);
        }
    }
    
    public static class TruckVehicle extends VehicleInstance
    {
        TruckVehicle()
        {
            super(TRUCK, 1400.0f, 200.0f);
        }
    }
    
    public static class SportVehicle extends VehicleInstance
    {
        SportVehicle()
        {
            super(SPORT, 20000.0f, 200.0f);
        }
    }
    
    public static VehicleInstance createVehicle(int i) {
        switch (i + 1) {
            case WEAK: return new WeakVehicle();
            case TRUCK: return new TruckVehicle();
            case SPORT: return new SportVehicle();
            default: throw new RuntimeException("Unknown vehicle type " + i);
        }
    }
}