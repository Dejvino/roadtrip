package roadtrip.model;

import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;

/**
 * Created by dejvino on 14.01.2017.
 */
public class TerrainDataProvider {
    public FractalSum base;
    public PerturbFilter perturb;
    public OptimizedErode therm;
    public SmoothFilter smooth;
    public IterativeFilter iterate;
}
