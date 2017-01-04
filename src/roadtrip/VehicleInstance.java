/*
 */
package roadtrip;

/**
 *
 * @author dejvino
 */
public class VehicleInstance
{
    final static int WEAK = 1;
    final static int TRUCK = 2;
    final static int SPORT = 3;

    static int getVehicleTypesCount() {
        return SPORT;
    }

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
    
    public static class WeakVehicle extends VehicleInstance
    {
        WeakVehicle()
        {
            super(WEAK, 200.0f, 100.0f);
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
    
    static VehicleInstance createVehicle(int i) {
        switch (i + 1) {
            case WEAK: return new WeakVehicle();
            case TRUCK: return new TruckVehicle();
            case SPORT: return new SportVehicle();
            default: throw new RuntimeException("Unknown vehicle type " + i);
        }
    }
}