package roadtrip.view;

import com.jme3.material.Material;
import com.jme3.terrain.geomipmap.TerrainGrid;
import roadtrip.model.TerrainDataProvider;

/**
 * Created by dejvino on 14.01.2017.
 */
public class TerrainView {
    public Material mat_terrain;
    public FineTerrainGrid terrainGrid;
    public float texMidScale = 64;
    public float texLowScale = 64;
    public float texHighScale = 64;
    public TerrainDataProvider terrainDataProvider;

    public TerrainView(TerrainDataProvider terrainDataProvider) {
        this.terrainDataProvider = terrainDataProvider;
    }
}
