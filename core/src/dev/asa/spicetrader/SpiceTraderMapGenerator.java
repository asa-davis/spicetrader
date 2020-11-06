package dev.asa.spicetrader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

public class SpiceTraderMapGenerator {

	private TextureAtlas atlas;
	
	public SpiceTraderMapGenerator(TextureAtlas atlas) {
		this.atlas = atlas;
	}
	
	public SpiceTraderMap generateMap(int numCols, int numRows, int tileWidth, int tileHeight, int smoothingIterations, int seaLevelOffset) {
		//map generation workflow:
		//	1. set map variables
		//	2. generate tile id map showing where land is
		//		a. randomize id map with values between 1-100
		//		b. smooth map n times by setting each tile to avg value of it's neighbors from previous iteration
		//		c. set all tiles with values below a certain threshold to water (id=0) and all tiles above to land (id=1)
		//	3. generate bitmask map showing which neighboring tiles are land for every tile
		//	4. generate libgdx TiledMap and MapRenderer
		
		//1
		SpiceTraderMap map = new SpiceTraderMap(numCols, numRows, tileWidth, tileHeight);
		
		//2
		int[][] randomTileIdMap = generateRandomTileIdMap(numRows, numCols, 1, 100);
		int[][] tileIdMap = smoothTileIdMap(smoothingIterations, randomTileIdMap);
		//debugMap(tileIdMap);
		thresholdTileIdMap(tileIdMap, seaLevelOffset);
		map.setTileIdMap(tileIdMap);
		
		//3
		int[][] neighborBitmaskMap = generateNeighborTileBitmaskMap(numRows, numCols, tileIdMap);
		map.setNeighborBitmaskMap(neighborBitmaskMap);
		
		//4
		TiledMap libgdxMap = generateLibgdxMap(numCols, numRows, tileWidth, tileHeight, neighborBitmaskMap, tileIdMap);
		map.setLibgdxMap(libgdxMap);
		
		return map;
	}
	
	//sets the tile id map to random values of min - max
	private static int[][] generateRandomTileIdMap(int numRows, int numCols, int min, int max) {
		int[][] tileIdMap = new int[numRows][numCols];
		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				tileIdMap[y][x] = MainGame.genRandomInt(min, max);
			}
		}
		return tileIdMap;
	}
	
	//smoothes land masses into clumps representing islands
	private static int[][] smoothTileIdMap(int iterations, int[][] tileIdMap) {
		int numRows = tileIdMap.length;
		int numCols = tileIdMap[0].length;
		int[][] smoothedMap = new int[numRows][numCols];
		
		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				//sum neighbors
				//yShift, xShift used to get position of neighbor relative to current tile
				//start in top left, work way across row then down. checking for out of bounds coordinates and skipping yShift = 0, xShift = 0 (this is curr tile)
				int neighborY;
				int neighborX;
				int neighborSum = 0;
				int neighborCount = 0;
				for(int yShift = 1; yShift >= -1; yShift--) {
					for(int xShift = -1; xShift <= 1; xShift++) {
						//skip current tile
						if(!(yShift == 0 && xShift == 0)) {
							neighborY = y + yShift;
							neighborX = x + xShift;
							//check for out of bounds
							if(neighborY >= 0 && neighborY < numRows && neighborX >= 0 && neighborX < numCols) {
								neighborSum += tileIdMap[neighborY][neighborX];
								neighborCount++;
							}
						}
					}
				}
				int avg = neighborSum / neighborCount;
				smoothedMap[y][x] = avg;
			}
		}
		
		//recursive
		if(iterations == 1)
			return smoothedMap;
		else
			return smoothTileIdMap(iterations - 1, smoothedMap);
	}
	
	//takes avg value of all cells and divides in half. the threshold is equal to this value + offset
	//cells above the threshold are land (id=1), cells below are water(id=0)
	private static void thresholdTileIdMap(int[][] tileIdMap, int offset) {
		int numRows = tileIdMap.length;
		int numCols = tileIdMap[0].length;
		int sum = 0;
		
		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				sum += tileIdMap[y][x];
			}
		}
		
		int threshold = sum / (numRows * numCols);
		threshold += offset;
		
		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				if(tileIdMap[y][x] <= threshold)
					tileIdMap[y][x] = 0;
				else
					tileIdMap[y][x] = 1;
			}
		}
	}
	
	//Generate a map of bitmasked values representing the presence of land on all 8 neighboring tiles for a given tile. 
	
	//		2^0|2^1|2^2
	//		-----------
	//		2^3| x |2^4
	//		-----------
	//	  	2^5|2^6|2^7
	
	//A bitmask for tile x would be calculate by adding up the values in all neighboring squares where land was present
	
	//Note: diagonal neighboring land will only be counted if it's two surrounding neighbors are also land. 
	//For example, the top right neighbor is only counted if both the top and the right neighbors are land too.
	//This is to avoid redundant values.
	
	private static int[][] generateNeighborTileBitmaskMap(int numRows, int numCols, int[][] tileIdMap) {
		int[][]	neighborBitmaskMap = new int[numRows][numCols];

		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				int bitmask = 0;
				int power = 0;
				
				//iterate through all neighbors, starting in top left
				//skipping, the center tile, increment power, check if in bounds, and add 2^power if land is there
				for(int yShift = 1; yShift >= -1; yShift--) {
					for(int xShift = -1; xShift <= 1; xShift++) {
						//skip current tile
						if(!(yShift == 0 && xShift == 0)) {
							int neighborY = y + yShift;
							int neighborX = x + xShift;
							//check for out of bounds
							if(neighborY >= 0 && neighborY < numRows && neighborX >= 0 && neighborX < numCols) {
								//check for land - if so add to bitmask
								if(tileIdMap[neighborY][neighborX] == 1) 
									bitmask += Math.pow(2, power);
							}
							power++;
						}
					}
				}
				
				//redundancy check:
				//top left
				if((bitmask & 1) > 0) {
					if((bitmask & 8) == 0 || (bitmask & 2) == 0)
						bitmask -= 1;
				}
				//top right
				if((bitmask & 4) > 0) {
					if((bitmask & 16) == 0 || (bitmask & 2) == 0)
						bitmask -= 4;
				}
				//bottom left
				if((bitmask & 32) > 0) {
					if((bitmask & 8) == 0 || (bitmask & 64) == 0)
						bitmask -= 32;
				}
				//bottom right
				if((bitmask & 128) > 0) {
					if((bitmask & 16) == 0 || (bitmask & 64) == 0)
						bitmask -= 128;
				}
				
				
				neighborBitmaskMap[y][x] = bitmask;
			}
		}
		
		return neighborBitmaskMap;
	}
	
	//generate the libgdx TiledMap object from the neighbor bitmask map and tileId map
	private TiledMap generateLibgdxMap(int numCols, int numRows, int tileWidth, int tileHeight, int[][] neighborBitmaskMap, int[][] tileIdMap) {
		TiledMap libgdxMap = new TiledMap();
		TiledMapTileLayer mapLayer = new TiledMapTileLayer(numCols, numRows, tileWidth, tileHeight);
		
		HashMap<Integer, Integer> beachBitmaskConverter = getBeachBitmaskConverter();
		for(int row = 0; row < numRows; row++) {
			for(int col = 0; col < numCols; col++) {
				TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
				StaticTiledMapTile tile;
				
				if(tileIdMap[row][col] == 0) 
					tile = new StaticTiledMapTile(this.atlas.findRegions("tile/water").get(0));
				else {
					int bitmask = neighborBitmaskMap[row][col];
					int tileNum = beachBitmaskConverter.get(bitmask);
					tile = new StaticTiledMapTile(this.atlas.findRegions("tile/grass").get(tileNum));
				}
				
				cell.setTile(tile);
				mapLayer.setCell(col, row, cell);
			}
		}
		libgdxMap.getLayers().add(mapLayer);
		
		return libgdxMap;
	}
	
	public void addVillagesToMap(List<VillageLocation> villageLocations) {
		//when villages are to be added to game:
		//	1. pass list of village locations to map
		//	2. set dock tile to 100 + village id for all locations
		//	3. regenerate TiledMap and MapRenderer
	}
	
	public List<VillageLocation> getValidVillageLocations() {
		return new ArrayList<VillageLocation>();
	}
	
	//this hashmap takes a bitmask value as a key and returns the correct grass tile number
	private static HashMap<Integer, Integer> getBeachBitmaskConverter() {
		HashMap<Integer, Integer> converter = new HashMap<Integer, Integer>();
		int[] values = {2, 1, 8, 2, 10, 3, 11, 4, 16, 5, 18, 6, 22, 7, 24, 8, 26, 9, 27, 10, 30, 11, 31, 12, 64, 13, 66, 14, 72, 15, 74, 16, 75, 17, 80, 18, 82, 19, 86, 20, 88, 21, 90, 22, 91, 23, 94, 24, 95, 25, 104, 26, 106, 27, 107, 28, 120, 29, 122, 30, 123, 31, 126, 32, 127, 33, 208, 34, 210, 35, 214, 36, 216, 37, 218, 38, 219, 39, 222, 40, 223, 41, 248, 42, 250, 43, 251, 44, 254, 45, 255, 46, 0, 47};
		for(int i = 0; i < values.length; i += 2) {
			converter.put(values[i], values[i + 1]);
		}
		return converter;
	}
	
	private static void debugMap(int[][] map) {
		int numRows = map.length;
		int numCols = map[0].length;
		
		for(int y = 0; y < numRows; y++) {
			for(int x = 0; x < numCols; x++) {
				System.out.print(" " + map[y][x]);
			}
			System.out.println('\n');
		}
	}
}