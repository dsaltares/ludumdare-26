package com.siondream.ld26;

import java.util.Iterator;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class PathFinder {
	
	public enum ResultCode {
		PathFound,
		OriginOutOfMap,
		DestinationOutOfMap,
		OriginNonWalkable,
		DestinationNonWalkable,
		NoPath,
	}
	
	public interface Callback {
		public void onPathRequest(long id, ResultCode code, Array<Vector2> path);
	}
	
	private static long nextRequestID = 1;
	private static final String TAG = "PathFinder";
	
	private Logger logger;
	private ObjectMap<TiledMapTile, Boolean> walkability;
	private TiledMapTileLayer tiles;
	private NavCell[][] navCells;
	private Array<Request> requests;
	private RequestPool requestPool;
	
	private NavCell initialNavCell;
	private NavCell destinationNavCell;
	private Array<NavCell> open;
	private Array<NavCell> closed;
	private Array<NavCell> neighbours;
	private ObjectMap<NavCell, NavCell> paths;
	private Array<Vector2> path;
	
	public PathFinder() {
		logger = new Logger(TAG, Globals.debugLevel);
		walkability = new ObjectMap<TiledMapTile, Boolean>();
		requests = new Array<Request>();
		requestPool = new RequestPool();
		open = new Array<NavCell>();
		closed = new Array<NavCell>();
		neighbours = new Array<NavCell>();
		path = new Array<Vector2>();
		paths = new ObjectMap<NavCell, NavCell>();
	}
	
	public void init(TiledMap map) {
		walkability.clear();
		
		tiles = (TiledMapTileLayer)map.getLayers().get(Globals.backgroundLayer);
		
		Iterator<TiledMapTile> it = map.getTileSets().getTileSet(Globals.tileset).iterator();
		while(it.hasNext()) {
			TiledMapTile tile = it.next();
			boolean walkable = Boolean.parseBoolean(tile.getProperties().get("walkable", "false", String.class));
			walkability.put(tile, walkable);
		}
		
		navCells = new NavCell[tiles.getWidth()][tiles.getHeight()];
		
		for (int x = 0; x < tiles.getWidth(); ++x) {
			for (int y = 0; y < tiles.getHeight(); ++y) {
				navCells[x][y] = new NavCell(tiles.getCell(x, y), x, y);
			}
		}
	}
	
	public void update() {
		long beginTime = System.currentTimeMillis();
		long endTime = beginTime + Globals.pathFinderBudgetMs;
		int pathsProcessed= 0;
		
		while (requests.size > 0 && System.currentTimeMillis() < endTime) {
			processRequest();
			requestPool.free(requests.get(0));
			requests.removeIndex(0);
			++pathsProcessed;
		}
		
		if (pathsProcessed > 0) {
			int total = requests.size + pathsProcessed;
			logger.info("" + pathsProcessed + " / " + total + " paths processed");
		}
		
	}
	
	public long requestPath(float x0, float y0, float x1, float y1, Callback callback) {
		Request request = requestPool.obtain();
		request.id = getNextRequestID();
		request.x0 = x0;
		request.y0 = y0;
		request.x1 = x1;
		request.y1 = y1;
		request.callback = callback;
		requests.add(request);
		return request.id;
	}
	
	private Cell getCellAt(float x, float y) {
		float pixelX = x * Globals.metresToPixels;
		float pixelY = y * Globals.metresToPixels;
		
		int cellX = (int)pixelX / (int)tiles.getTileWidth();
		int cellY = (int)pixelY / (int)tiles.getTileHeight();
		
		return tiles.getCell(cellX, cellY);
	}
	
	private NavCell getNavCellAt(float x, float y) {
		float pixelX = x * Globals.metresToPixels;
		float pixelY = y * Globals.metresToPixels;
		
		int cellx = (int)pixelX / (int)tiles.getTileWidth();
		int celly = (int)pixelY / (int)tiles.getTileHeight();
		return getNavCellAt(cellx, celly);
	}
	
	private NavCell getNavCellAt(int x, int y) {
		if (x < 0 || x >= tiles.getWidth()) return null;
		if (y < 0 || y >= tiles.getHeight()) return null;
		return navCells[x][y];
	}
	
	private TiledMapTile getTileAt(float x, float y) {
		Cell cell = getCellAt(x, y);
		return cell != null? cell.getTile() : null;
	}
	
	private void processRequest() {
		Request request = requests.get(0);
		
		TiledMapTile initialTile = getTileAt(request.x0, request.y0);
		
		if (initialTile == null) {
			request.callback.onPathRequest(request.id, ResultCode.OriginOutOfMap, null);
			return;
		}
		
		TiledMapTile destinationTile = getTileAt(request.x1, request.y1);
		
		if (destinationTile == null) {
			request.callback.onPathRequest(request.id, ResultCode.DestinationOutOfMap, null);
			return;
		}
		
		if (!walkability.get(initialTile)) {
			request.callback.onPathRequest(request.id, ResultCode.OriginNonWalkable, null);
			return;
		}
		
		if (!walkability.get(destinationTile)) {
			request.callback.onPathRequest(request.id, ResultCode.DestinationNonWalkable, null);
			return;
		}
		
		initialNavCell = getNavCellAt(request.x0, request.y0);
		destinationNavCell = getNavCellAt(request.x1, request.y1);
		
		open.clear();
		open.add(initialNavCell);
		closed.clear();
		paths.clear();
		
		initialNavCell.g = 0;
		initialNavCell.h = h(initialNavCell);
		
		while (open.size > 0) {
			NavCell current = getBestCandidate();
			
			if (isGoal(current)) {
				path.clear();
				recoverPath(current);
				request.callback.onPathRequest(request.id, ResultCode.PathFound, path);
				return;
			}
			
			open.removeValue(current, false);
			closed.add(current);
			
			generateNeighbours(current);
			
			for (NavCell neighbour : neighbours) {
				float nextG = current.g + 1;
				
				if (closed.contains(neighbour, false) && nextG >= neighbour.g) {
					continue;
				}
				
				int indexInOpen = open.indexOf(neighbour, false);
				
				if (indexInOpen == -1 || nextG < neighbour.g) {
					paths.put(neighbour, current);
					neighbour.g = nextG;
					neighbour.h = h(neighbour);
					
					if (indexInOpen == -1) {
						open.add(neighbour);
					}
				}
			}
		}
		
		request.callback.onPathRequest(request.id, ResultCode.NoPath, null);
	}
	
	private void recoverPath(NavCell current) {
		NavCell cameFrom = paths.get(current);
		
		if (cameFrom != null) {
			recoverPath(cameFrom);
		}
		
		float width = tiles.getTileWidth();
		float height = tiles.getTileHeight();
		float x = (current.x * width + width * 0.5f) * Globals.pixelsToMetres;
		float y = (current.y * height + height * 0.5f) * Globals.pixelsToMetres;
		path.add(new Vector2(x, y));
	}
	
	private void generateNeighbours(NavCell current) {
		neighbours.clear();
		NavCell neighbourA = getNavCellAt(current.x - 1, current.y - 1);
		NavCell neighbourB = getNavCellAt(current.x, current.y - 1);
		NavCell neighbourC = getNavCellAt(current.x + 1, current.y - 1);
		NavCell neighbourD = getNavCellAt(current.x - 1, current.y);
		NavCell neighbourE = getNavCellAt(current.x + 1, current.y);
		NavCell neighbourF = getNavCellAt(current.x - 1, current.y + 1);
		NavCell neighbourG = getNavCellAt(current.x, current.y + 1);
		NavCell neighbourH = getNavCellAt(current.x + 1, current.y + 1);
		
		if (neighbourA != null && walkability.get(neighbourA.cell.getTile())) neighbours.add(neighbourA);
		if (neighbourB != null && walkability.get(neighbourB.cell.getTile())) neighbours.add(neighbourB);
		if (neighbourC != null && walkability.get(neighbourC.cell.getTile())) neighbours.add(neighbourC);
		if (neighbourD != null && walkability.get(neighbourD.cell.getTile())) neighbours.add(neighbourD);
		if (neighbourE != null && walkability.get(neighbourE.cell.getTile())) neighbours.add(neighbourE);
		if (neighbourF != null && walkability.get(neighbourF.cell.getTile())) neighbours.add(neighbourF);
		if (neighbourG != null && walkability.get(neighbourG.cell.getTile())) neighbours.add(neighbourG);
		if (neighbourH != null && walkability.get(neighbourH.cell.getTile())) neighbours.add(neighbourH);
	}
	
	private NavCell getBestCandidate() {
		NavCell bestCandidate = null;
		float lowestF = Float.POSITIVE_INFINITY;
		
		for (NavCell candidate : open) {
			if (candidate.g + candidate.h < lowestF) {
				bestCandidate = candidate;
				lowestF = candidate.g + candidate.h;
			}
		}
		
		return bestCandidate;
	}
	
	private boolean isGoal(NavCell node) {
		return destinationNavCell.x == node.x && destinationNavCell.y == node.y;
	}
	
	private float h(NavCell node) {
		return (destinationNavCell.x - node.x) * (destinationNavCell.x - node.x) +
			   (destinationNavCell.y - node.y) * (destinationNavCell.y - node.y);
	}
	
	private float g(NavCell node) {
		return 0.0f;
	}
	
	private class NavCell extends BinaryHeap.Node {
		public Cell cell;
		public int x;
		public int y;
		public float g;
		public float h;
		
		public NavCell(Cell cell, int x, int y) {
			super(0.0f);
			this.cell = cell;
			this.x = x;
			this.y = y;
			this.g = Float.POSITIVE_INFINITY;
			this.h = Float.POSITIVE_INFINITY;
		}
	}
	
	private class Request implements Poolable {
		public long id = 0;
		public float x0 = 0.0f;
		public float y0 = 0.0f;
		public float x1 = 0.0f;;
		public float y1 = 0.0f;
		public Callback callback = null;
		
		@Override
		public void reset() {
			x0 = x1 = y0 = y1 = 0.0f;
			id = 0;
			callback = null;
		}
	}
	
	private class RequestPool extends Pool<Request> {

		@Override
		protected Request newObject() {
			return new Request();
		}
	}
	
	private static long getNextRequestID() {
		return nextRequestID++;
	}
}
