package wblut.isogrid;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public abstract class WB_IsoSystem<IHG extends WB_IsoHexGrid> {
	WB_IsoHexGrid grid;
	WB_CubeGrid cubes;
	double L;
	int I, J, K, JK, IJK;
	double centerX, centerY;
	PApplet home;
	int seed;
	int[] colors;
	RestorableUniformRandomProvider randomGen;
	RandomProviderState state;
	private boolean DEFER;
	boolean GLOBALDEFER;
	boolean YFLIP;

	WB_IsoSystem() {

	}

	public WB_IsoSystem(double L, int I, int J, int K, double centerX, double centerY, int[] colors, int seed,
			WB_IsoHexGrid grid, PApplet home) {
		randomGen = RandomSource.create(RandomSource.MT);
		state = randomGen.saveState();
		this.home = home;
		this.L = L;
		this.I = Math.max(1, I);
		this.J = Math.max(1, J);
		this.K = Math.max(1, K);
		JK = this.J * this.K;
		IJK = this.I * JK;
		this.colors = colors;
		this.centerX = centerX;
		this.centerY = centerY;
		this.cubes = new WB_CubeGrid(this.I, this.J, this.K);
		this.seed = seed;
		this.grid = grid;
		set(0, 0, 0, I, J, K);
		mapVoxelsToHexGrid();
		DEFER = false;
		GLOBALDEFER = false;
		YFLIP = true;
	}

	public WB_IsoSystem(WB_IsoSystem<IHG> iso) {
		randomGen = RandomSource.create(RandomSource.MT);
		state = randomGen.saveState();
		this.home = iso.home;
		this.L = iso.L;
		this.I = iso.I;
		this.J = iso.J;
		this.K = iso.K;
		IJK = I * J * K;
		this.colors = new int[iso.colors.length];
		System.arraycopy(iso.colors, 0, this.colors, 0, iso.colors.length);
		this.centerX = iso.centerX;
		this.centerY = iso.centerY;
		this.cubes = new WB_CubeGrid(iso.cubes);
		this.seed = iso.seed;
		try {
			grid = iso.grid.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {

			e.printStackTrace();
		}
		mapVoxelsToHexGrid();
		DEFER = false;
		GLOBALDEFER = false;
		YFLIP = true;

	}
	
	abstract int getNumberOfTriangles();

	final public void setRNGSeed(long seed) {
		randomGen = RandomSource.create(RandomSource.MT, seed);
		state = randomGen.saveState();
	}

	final public void resetRNG() {
		randomGen.restoreState(state);
	}

	final void map() {
		if (!deferred()) {
			mapVoxelsToHexGrid();
		}
	}

	public abstract void mapVoxelsToHexGrid();

	public void setDeferred(boolean b) {
		GLOBALDEFER = b;
	}

	public boolean deferred() {
		return DEFER || GLOBALDEFER;
	}

	public void setYFlip(boolean b) {
		YFLIP = b;
	}

	public void blocks(int n) {
		grid.clear();
		cubes.clear();
		DEFER = true;
		for (int r = 0; r < n; r++) {
			int sj = (int) random(J);
			int ej = (int) random(sj, J);
			int si = (int) random(I);
			int ei = (int) random(si, I);
			int sk = (int) random(K);
			int ek = (int) random(sk, K);
			xor(si, sj, sk, ei - si, ej - sj, ek - sk);
		}
		DEFER = false;
		map();
	}

	public void subdivide(double chance, int di, int dj, int dk) {
		DEFER = true;
		for (int i = 0; i < I; i += di) {
			for (int j = 0; j < J; j += dj) {
				for (int k = 0; k < K; k += dk) {
					if (random(1.0) < chance) {
						clear(i, j, k, di, dj, dk);
					}
				}
			}

		}
		DEFER = false;
		map();
	}

	public void refresh() {
		mapVoxelsToHexGrid();
	}

	public void set(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.set(index, true);
					}
				}
			}
		}
		map();
	}

	public void clear(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.set(index, false);
					}
				}
			}
		}
		map();
	}

	public void and(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.and(index, true);
					}
				}
			}
		}
		map();
	}

	public void or(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.or(index, true);
					}
				}
			}
		}
		map();
	}

	public void xor(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.xor(index, true);
					}
				}
			}
		}
		map();
	}

	public void not(int i, int j, int k, int blocki, int blockj, int blockk) {
		for (int di = 0; di < blocki; di++) {
			for (int dj = 0; dj < blockj; dj++) {
				for (int dk = 0; dk < blockk; dk++) {
					int index = index(i + di, j + dj, k + dk);
					if (index > -1) {
						cubes.not(index);
					}
				}
			}
		}
		map();
	}

	final public void drawOrientation(int q, int r, double dx, double dy) {
		double[] point = getGridCoordinates(q, r);
		home.text("(" + q + "," + r + ")", (float) (point[0] + dx), (float) (point[1] + dy));
	}

	final public void drawPoint(double q, double r) {
		double[] point = getGridCoordinates(q, r);
		home.point((float) point[0], (float) point[1]);
	}

	final public void drawPoint(double rad, double q, double r) {
		double[] point = getGridCoordinates(q, r);
		home.ellipse((float) point[0], (float) point[1], 2 * (float) rad, 2 * (float) rad);
	}

	final public void drawLine(double q1, double r1, double q2, double r2) {
		double[] point1 = getGridCoordinates(q1, r1);
		double[] point2 = getGridCoordinates(q2, r2);
		home.line((float) point1[0], (float) point1[1], (float) point2[0], (float) point2[1]);
		home.point((float) point1[0], (float) point1[1]);
		home.point((float) point2[0], (float) point2[1]);
	}

	final public void drawHexGrid() {
		for (WB_IsoGridCell cell : grid.cells.values()) {
			drawHex(cell.getQ(), cell.getR());
		}
	}

	final public void drawHexGrid(double radius, int type) {
		double[] center = getGridCoordinates(1, -1);
		int limit = (int) (2.0 * radius
				/ Math.sqrt(
						(center[0] - centerX) * (center[0] - centerX) + (center[1] - centerY) * (center[1] - centerY)))
				+ 1;
		for (int q = -limit; q <= limit; q++) {
			for (int r = -limit; r <= limit; r++) {
				if (((q + r) % 3 + 3) % 3 == type) {
					center = getGridCoordinates(q, r);
					if (Math.sqrt((center[0] - centerX) * (center[0] - centerX)
							+ (center[1] - centerY) * (center[1] - centerY)) <= radius) {
						home.beginShape();
						for (int i = 0; i < 6; i++) {
							grid.hexVertex(home.g, i, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
						}
						home.endShape(PConstants.CLOSE);
					}
				}
			}
		}
	}

	final public void drawHexCenters() {
		for (WB_IsoGridCell cell : grid.cells.values()) {
			drawPoint(cell.getQ(), cell.getR());
		}
	}

	final public void drawHexCenters(double radius, int type) {
		double[] center = getGridCoordinates(1, -1);
		int limit = (int) (2.0 * radius
				/ Math.sqrt(
						(center[0] - centerX) * (center[0] - centerX) + (center[1] - centerY) * (center[1] - centerY)))
				+ 1;
		for (int q = -limit; q <= limit; q++) {
			for (int r = -limit; r <= limit; r++) {
				if (((q + r) % 3 + 3) % 3 == type) {
					center = getGridCoordinates(q, r);
					if (Math.sqrt((center[0] - centerX) * (center[0] - centerX)
							+ (center[1] - centerY) * (center[1] - centerY)) <= radius) {
						home.point((float) center[0], (float) center[1]);
					}
				}
			}
		}
	}

	final public void drawTriangleGrid() {
		for (WB_IsoGridCell cell : grid.cells.values()) {
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				drawTriangle(cell.getQ(), cell.getR(), f);
			}
		}
	}
	
	
	final public void drawTriangleGrid(double radius) {
		double[] center = getGridCoordinates(1, -1);
		int limit = (int) (2.0 * radius
				/ Math.sqrt(
						(center[0] - centerX) * (center[0] - centerX) + (center[1] - centerY) * (center[1] - centerY)))
				+ 1;
		for (int q = -limit; q <= limit; q++) {
			for (int r = -limit; r <= limit; r++) {
				if (((q + r) % 3 + 3) % 3 == 0) {
					center = getGridCoordinates(q, r);
					if (Math.sqrt((center[0] - centerX) * (center[0] - centerX)
							+ (center[1] - centerY) * (center[1] - centerY)) <= radius) {
						for(int f=0;f<getNumberOfTriangles();f++) {
							drawTriangle(center, f);
						}
					}
				}
			}
		}
	}

	final public void drawLinesSVG() {
		for (WB_IsoGridLine line : grid.lines) {
			for (WB_IsoGridSegment segment : line.getSegments()) {
				grid.line(home.g, segment.getQ1(), segment.getR1(), segment.getQ2(), segment.getR2(), centerX, centerY,
						L, (YFLIP ? -1.0 : 1.0) * L);
			}
		}
	}

	final public void drawLines() {
		drawLines(home.g);
	}

	final public void drawLines(PGraphics home) {
		for (WB_IsoGridLine line : grid.lines) {
			for (WB_IsoGridSegment segment : line.getSegments()) {
				grid.line(home, segment.getQ1(), segment.getR1(), segment.getQ2(), segment.getR2(), centerX, centerY, L,
						(YFLIP ? -1.0 : 1.0) * L);
				grid.point(home, segment.getQ1(), segment.getR1(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
				grid.point(home, segment.getQ2(), segment.getR2(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
			}
		}
	}

	final public void drawOutlines(PGraphics home) {
		for (WB_IsoGridLine line : grid.outlines) {
			for (WB_IsoGridSegment segment : line.getSegments()) {
				grid.line(home, segment.getQ1(), segment.getR1(), segment.getQ2(), segment.getR2(), centerX, centerY, L,
						(YFLIP ? -1.0 : 1.0) * L);
				grid.point(home, segment.getQ1(), segment.getR1(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
				grid.point(home, segment.getQ2(), segment.getR2(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
			}
		}
	}

	final public void drawOutlines() {
		drawOutlines(home.g);
	}

	final public void drawOutlinesSVG() {
		for (WB_IsoGridLine line : grid.outlines) {
			for (WB_IsoGridSegment segment : line.getSegments()) {
				grid.line(home.g, segment.getQ1(), segment.getR1(), segment.getQ2(), segment.getR2(), centerX, centerY,
						L, (YFLIP ? -1.0 : 1.0) * L);

			}
		}
	}

	final public void drawLines(int type, double minValue, double maxValue) {
		for (WB_IsoGridLine line : grid.lines) {
			if (line.getType() == type && line.getLineValue() >= minValue && line.getLineValue() < maxValue) {
				for (WB_IsoGridSegment segment : line.getSegments()) {
					grid.line(home.g, segment.getQ1(), segment.getR1(), segment.getQ2(), segment.getR2(), centerX,
							centerY, L, (YFLIP ? -1.0 : 1.0) * L);
					grid.point(home.g, segment.getQ1(), segment.getR1(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
					grid.point(home.g, segment.getQ2(), segment.getR2(), centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
				}
			}
		}
	}

	final public void drawHex(int q, int r) {
		double[] center = getGridCoordinates(q, r);
		home.beginShape();
		for (int i = 0; i < 6; i++) {
			grid.hexVertex(home.g, i, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
		}
		home.endShape(PConstants.CLOSE);
	}

	private void triVertices(double[] center, int f) {
		grid.triVertex(home.g, f, 0, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
		grid.triVertex(home.g, f, 1, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
		grid.triVertex(home.g, f, 2, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);

	}

	private void triVertices(PGraphics pg, double[] center, int f) {
		grid.triVertex(pg, f, 0, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
		grid.triVertex(pg, f, 1, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);
		grid.triVertex(pg, f, 2, center[0], center[1], L, (YFLIP ? -1.0 : 1.0) * L);

	}

	final public void drawTriangle(int q, int r, int f) {
		double[] center = getGridCoordinates(q, r);
		home.beginShape();
		triVertices(center, f);
		home.endShape(PConstants.CLOSE);
	}

	final public void drawTriangle(double[] center, int f) {
		home.beginShape();
		triVertices(center, f);
		home.endShape(PConstants.CLOSE);
	}

	final public void drawTriangles() {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {
					home.beginShape(PConstants.TRIANGLES);
					home.fill(colors[cell.palette[f] * ((getNumberOfTriangles()==3)?3:10) + cell.orientation[f]]);
					triVertices(center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTriangles(double I, double J, double K, PImage[] textures, float ho, float hf, float hr,
			float zo, float zf, float zo2, float zf2, float oo, float of) {
		double[] center;
		float hue;
		int offsetU, offsetV;
		double scaleU, scaleV;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {

					home.beginShape(PConstants.TRIANGLES);
					home.texture(textures[cell.orientation[f]]);
					home.colorMode(PConstants.HSB);
					hue = (hf * cell.part[f]) % hr;
					hue = ho + hue;
					if (hue < 0)
						hue += 256 * ((int) (hue / (-256)) + 1);
					home.tint(hue % 256, zo + zf * cell.z[f], (oo + of * cell.orientation[f] + zo2 + zf2 * cell.z[f]));
					home.colorMode(PConstants.RGB);

					offsetU = cell.getTriangleUOffset(f);
					offsetV = cell.getTriangleVOffset(f);

					switch (cell.getTriangleUDirection(f)) {
					case 0:
						scaleU = 1.0 / Math.max(1.0, I);
						break;

					case 1:
						scaleU = 1.0 / Math.max(1.0, J);
						break;

					case 2:
						scaleU = 1.0 / Math.max(1.0, K);
						break;

					default:
						scaleU = 1.0;
					}

					switch (cell.getTriangleVDirection(f)) {
					case 0:
						scaleV = 1.0 / Math.max(1.0, I);
						break;

					case 1:
						scaleV = 1.0 / Math.max(1.0, J);
						break;

					case 2:
						scaleV = 1.0 / Math.max(1.0, K);
						break;

					default:
						scaleV = 1.0;
					}

					grid.triVertex(home.g, f, 0, center[0], center[1], L, L,
							scaleU * (cell.getTriangleU(f, 0) + offsetU), scaleV * (cell.getTriangleV(f, 0) + offsetV));
					grid.triVertex(home.g, f, 1, center[0], center[1], L, L,
							scaleU * (cell.getTriangleU(f, 1) + offsetU), scaleV * (cell.getTriangleV(f, 1) + offsetV));
					grid.triVertex(home.g, f, 2, center[0], center[1], L, L,
							scaleU * (cell.getTriangleU(f, 2) + offsetU), scaleV * (cell.getTriangleV(f, 2) + offsetV));

					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesRegion() {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {

					home.colorMode(PConstants.HSB);
					home.fill((cell.region[f] * 37) % 256, 255, 255 * (float) cell.drop[f]);
					home.colorMode(PConstants.RGB);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesRegion(PGraphics home) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {
					home.colorMode(PConstants.HSB);
					home.fill((cell.region[f] * 37) % 256, 255, 255);
					home.colorMode(PConstants.RGB);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);
					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesIJK(PGraphics home, float L, float M, float N) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {

					home.fill(cell.getCube(f)[0] * 256.0f / L, cell.getCube(f)[1] * 256.0f / M,
							cell.getCube(f)[2] * 256.0f / N);

					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesZ(float hf, int counter) {
		drawTrianglesZ(home.g, hf, counter);
	}

	final public void drawTrianglesZ(PGraphics home, float hf, int counter) {
		double[] center;
		float hue;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {
					home.colorMode(PConstants.HSB);
					hue = hf * cell.z[f] + counter;
					if (hue < 0)
						hue += 256 * ((int) (hue / (-256)) + 1);
					home.fill(hue % 256, 255, 255);
					home.colorMode(PConstants.RGB);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);
					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesZRegion(float hf, int counter) {
		drawTrianglesZRegion(home.g, hf, counter);
	}

	final public void drawTrianglesZRegion(PGraphics home, float hf, int counter) {
		double[] center;
		float hue;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {
					home.colorMode(PConstants.HSB);
					hue = hf * cell.z[f] + counter + 37 * cell.region[f];
					if (hue < 0)
						hue += 256 * ((int) (hue / (-256)) + 1);
					home.fill(hue % 256, 255, 255);
					home.colorMode(PConstants.RGB);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesPart(PGraphics home, float ho, float hf, float hr, float zo, float zf, float zo2,
			float zf2, float oo, float of) {
		double[] center;
		float hue;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {
					home.colorMode(PConstants.HSB);
					hue = (hf * cell.part[f]) % hr;
					hue = ho + hue;
					if (hue < 0)
						hue += 256 * ((int) (hue / (-256)) + 1);
					home.fill(hue % 256, zo + zf * cell.z[f], oo + of * cell.orientation[f] + zo2 + zf2 * cell.z[f]);
					home.colorMode(PConstants.RGB);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTrianglesPart(float ho, float hf, float hr, float zo, float zf, float zo2, float zf2,
			float oo, float of) {
		drawTrianglesPart(home.g, ho, hf, hr, zo, zf, zo2, zf2, oo, of);

	}

	final public void drawTrianglesWithPart(PGraphics home, int part) {
		double[] center;

		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1 && cell.part[f] == part) {

					home.beginShape(PConstants.TRIANGLES);
					triVertices(home, center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTriangles(int[] colors) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1) {

					home.fill(colors[10 * cell.palette[f] + cell.orientation[f]]);
					home.stroke(colors[10 * cell.palette[f] + cell.orientation[f]]);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(center, f);

					home.endShape();
				}
			}
		}
	}

	final public void drawTriangles(int zmin, int zmax) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1 && cell.z[f] >= zmin && cell.z[f] < zmax) {
					home.beginShape(PConstants.TRIANGLES);
					triVertices(center, f);
					home.endShape();
				}
			}
		}
	}

	final public void drawTriangles(int[] colors, int zmin, int zmax) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1 && cell.z[f] >= zmin && cell.z[f] < zmax) {

					home.fill(colors[10 * cell.palette[f] + cell.orientation[f]]);
					home.noStroke();// stroke(colors[10 * cell.colorIndices[f] + cell.orientations[f]]);
					home.beginShape(PConstants.TRIANGLES);
					triVertices(center, f);
					home.endShape();
				}
			}
		}
	}

	final public void drawTriangles(int[] colors, int zmin, int zmax, int znear, int zfar, int mini, int maxi, int minj,
			int maxj, int mink, int maxk) {
		double[] center;
		for (WB_IsoGridCell cell : grid.cells.values()) {
			center = getGridCoordinates(cell.getQ(), cell.getR());
			for (int f = 0; f < cell.getNumberOfTriangles(); f++) {
				if (cell.orientation[f] > -1 && cell.z[f] >= zmin && cell.z[f] < zmax && cell.cubei[f] >= mini
						&& cell.cubei[f] < maxi && cell.cubej[f] >= minj && cell.cubej[f] < maxj
						&& cell.cubek[f] >= mink && cell.cubek[f] < maxk) {
					home.fill(color(colors[10 * cell.palette[f] + cell.orientation[f]], cell.z[f], zfar, znear));
					home.stroke(color(colors[10 * cell.palette[f] + cell.orientation[f]], cell.z[f], zfar, znear));
					home.beginShape(PConstants.TRIANGLES);
					triVertices(center, f);

					home.endShape();
				}
			}
		}
	}

	final public double[] getGridCoordinates(double q, double r) {
		return grid.getGridCoordinates(q, r, centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
	}

	final public int[] getTriangleAtGridCoordinates(double x, double y) {
		return grid.getTriangleAtGridCoordinates(x, y, centerX, centerY, L, (YFLIP ? -1.0 : 1.0) * L);
	}

	private int index(final int i, final int j, final int k) {

		if (i > -1 && j > -1 && k > -1 && i < I && j < J && k < K) {
			return k + j * K + i * JK;
		} else {
			return -1;
		}
	}

	final static int color(final int color, int z, int zmin, int zmax) {
		int r = (color >> 16) & 0xff;
		int g = (color >> 8) & 0xff;
		int b = (color) & 0xff;
		double f = (z - zmin) / (double) (zmax - zmin);
		f = Math.min(Math.max(f, 0.0), 1.0);
		return 255 << 24 | ((int) Math.round(f * r)) << 16 | ((int) Math.round(f * g)) << 8 | ((int) Math.round(f * b));
	}

	final double random(double v) {
		return randomGen.nextDouble() * v;
	}

	final double random(double v, double w) {
		return v + randomGen.nextDouble() * (w - v);
	}

}