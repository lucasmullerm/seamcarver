import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SeamCarver implements ActionListener{

	private Picture pic;
	private int[][] color;
	private double[][] energy;
	private int w;
	private int h;
	private boolean ready;

	/*---------------------------------------
	Static fields and functions
	---------------------------------------*/
	private boolean transposed;
	private static final int RED = 0xFF;
	private static final int BLUE = 0xFF0000;
	private static final int GREEN = 0xFF00;

	private static int red(int rgb) {
		return rgb & RED;
	}

	private static int green(int rgb) {
		return (rgb & GREEN) >> 8;
	}

	private static int blue(int rgb) {
		return (rgb & BLUE) >> 16;
	}

	/*-------------------------------------*/

	public SeamCarver(Picture picture) { // create a seam carver object based on the given picture
		pic = new Picture(picture);
		h = picture.height();
		w = picture.width();
		color = new int[w][h];
		energy = new double[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				color[x][y] = picture.get(x, y).getRGB();
			}
		}
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				energy[x][y] = calcEnergy(x, y);
			}
		}
		ready = true;
		transposed = false;
	}

	private void checkBounds(int x, int y) {
		if (x < 0 || y < 0 || x >= width() || y >= height())
			throw new java.lang.IndexOutOfBoundsException();
	}

	private void checkSeam(int[] seam) {
		if (seam == null) throw new java.lang.NullPointerException();
		if (seam.length != w)
			throw new java.lang.IllegalArgumentException();
		for (int i = 1; i < seam.length; i++) {
			if (Math.abs(seam[i] - seam[i - 1]) > 1)
				throw new java.lang.IllegalArgumentException();
		}
	}

	private void transpose() {
		int[][] c = new int[h][w];
		double[][] e = new double[h][w];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				c[y][x] = color[x][y];
				e[y][x] = energy[x][y];
			}
		}
		int aux = w;
		w = h;
		h = aux;
		color = c;
		energy = e;
		transposed = !transposed;
	}


	public Picture picture() { // current picture
		if (ready)	return pic;
		if (transposed)	transpose();
		pic = new Picture(w, h);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				Color c = new Color(color[x][y]);
				pic.set(x, y, c);
			}
		}
		ready = true;
		return pic;
	}

	public int width() { // width of current picture
		if (transposed)	return h;
		return w;
	}

	public int height() { // height of current picture
		if (transposed)	return w;
		return h;
	}

	private double energy(int id) {
		int x = id / h;
		int y = id % h;
		return energy[x][y];
	}

	public double energy(int x, int y) { // energy of pixel at column x and row y
		checkBounds(x, y);
		if (transposed) {
			int aux = x;
			x = y;
			y = aux;
		}
		return energy[x][y];
	}

	private double calcEnergy(int x, int y) {
		if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
			return 3*(255*255);
		}
		int l = color[x - 1][y];
		int r = color[x + 1][y];
		int u = color[x][y - 1];
		int d = color[x][y + 1]; 
		double dx2 = 0;
		dx2 += (red(l) - red(r))*(red(l) - red(r));
		dx2 += (green(l) - green(r))*(green(l) - green(r));
		dx2 += (blue(l) - blue(r))*(blue(l) - blue(r));
		double dy2 = 0;
		dy2 += (red(u) - red(d))*(red(u) - red(d));
		dy2 += (green(u) - green(d))*(green(u) - green(d));
		dy2 += (blue(u) - blue(d))*(blue(u) - blue(d));
		return dx2 + dy2;
	}

	private void update(IndexMinPQ<Double> pq, int id, int current, double dist, int[] from) {
		int x = id /h;
		int y = id % h;
		double e = dist + energy[x][y];
		if (pq.contains(id)) {
			if (e < pq.keyOf(id)) {
				pq.decreaseKey(id, e);
				from[id] = current;
			}
		}
		else {
			pq.insert(id, e);
			from[id] = current;
		}
	}

	private void updateLazy(MinPQ<Node> pq, int id, int current, double dist, int[] from, double[] distSoFar) {
		int x = id /h;
		int y = id % h;
		double e = dist + energy[x][y];
		if (e < distSoFar[id]) {
			pq.insert(new Node(id, e));
			from[id] = current;
			distSoFar[id] = e;
		}
	}

	private int[] findSeam() {//generic horizontal seam
		IndexMinPQ<Double> pq = new IndexMinPQ<Double>(w*h);
		int[] from = new int[w*h];
		boolean[] visited = new boolean[w*h];
		for (int y = 0; y < h; y++) {
			pq.insert(y, energy[0][y]);
			visited[y] = true;
		}
		int it;
		while (true) {
			double curEnergy = pq.minKey();
			int current = pq.delMin();
			visited[current] = true;
			int x = current / h;
			int y = current % h;
			if (x == w - 1) {
				it = current;
				break;
			}
			if (!visited[(x+1)*h + y])
				update(pq, (x+1)*h + y, current, curEnergy, from);
			if (y > 0 && !visited[(x+1)*h + y-1]) update(pq, (x+1)*h + y-1, current, curEnergy, from);
			if (y < h - 1 && !visited[(x+1)*h + y+1]) update(pq, (x+1)*h + y+1, current, curEnergy, from);
		}
		pq = null;
		visited = null;
		int[] seam = new int[w];
		for (int x = w - 1; x >= 0; x--) {
			seam[x] = it % h;
			it = from[it];
		}
		return seam;
	}

	private class Node implements Comparable<Node>{
		double val;
		int id;
		public Node(int i, double v) {
			id = i;
			val = v;
		}
		public int compareTo(Node that) {
			return Double.compare(this.val, that.val);
		}
	}

	private int[] findSeamLazy() {
		MinPQ<Node> pq = new MinPQ<Node>(w*h);
		int[] from = new int[w*h];
		double[] distSoFar = new double[w*h];
		for (int it = 0; it < w*h; it++) {
			distSoFar[it] = Double.POSITIVE_INFINITY;
		}
		for (int y = 0; y < h; y++) {
			double e = energy[0][y];
			pq.insert(new Node(y, e));
			distSoFar[y] = e;
		}
		int it;
		while (true) {
			Node n = pq.delMin();
			double curEnergy = n.val;
			int current = n.id;
			int x = current / h;
			int y = current % h;
			if (x == w - 1) {
				it = current;
				break;
			}
			if (curEnergy > distSoFar[current])	continue;
			updateLazy(pq, (x+1)*h + y, current, curEnergy, from, distSoFar);
			if (y > 0) updateLazy(pq, (x+1)*h + y-1, current, curEnergy, from, distSoFar);
			if (y < h - 1) updateLazy(pq, (x+1)*h + y+1, current, curEnergy, from, distSoFar);
		}
		pq = null;
		distSoFar = null;
		int[] seam = new int[w];
		for (int x = w - 1; x >= 0; x--) {
			seam[x] = it % h;
			it = from[it];
		}
		return seam;
	}

	private int[] findSeamPD() {
		int[] from = new int[w*h];
		double[] dist = new double[w*h];
		// x = 0
		for (int y = 0; y < h; y++) {
			dist[y] = energy[0][y];
		}
		// x = [1 -> (w-1)]
		for (int x = 1; x < w; x++) {
			//y = 0
			dist[x*h + 0] = energy[x][0] + dist[(x-1)*h + 1];
			from[x*h + 0] = (x-1)*h + 1;
			//y = [1 -> (h-2)]
			for (int y = 1; y < h - 1; y++) {
				int current = x*h + y;
				int up   = (x-1)*h + y-1;
				int mid  = (x-1)*h + y;
				int down = (x-1)*h + y+1;
				if (dist[up] <= dist[mid] && dist[up] <= dist[down]) {
					dist[current] = energy[x][y] + dist[up];
					from[current] = up;
				}
				else if (dist[mid] <= dist[down]) {
					dist[current] = energy[x][y] + dist[mid];
					from[current] = mid;
				}
				else {
					dist[current] = energy[x][y] + dist[down];
					from[current] = down;
				}
			}
			//y = (h-1)
			dist[x*h + (h-1)] = energy[x][h-1] + dist[(x-1)*h + (h-2)];
			from[x*h + (h-1)] = (x-1)*h + (h-2);
		}
		// check last one of the seam
		int it = w*h - 1;
		for (int y = 0; y < h; y++) {
			if (dist[(w-1)*h + y] < dist[it]) {
				it = (w-1)*h + y;
			}
		}
		dist = null;
		int[] seam = new int[w];
		for (int x = w - 1; x >= 0; x--) {
			seam[x] = it % h;
			it = from[it];
		}
		return seam;
	}

	public int[] findHorizontalSeam() { // sequence of indices for horizontal seam
		if (transposed)	transpose();
		//return findSeam();
		//return findSeamLazy();
		return findSeamPD();
	}

	public int[] findVerticalSeam() { // sequence of indices for vertical seam
		if (!transposed) transpose();
		//return findSeam();
		//return findSeamLazy();
		return findSeamPD();
	}

	private void removeSeam(int[] seam) {//generic remover horizontal
		checkSeam(seam);
		int[][] newColor = new int[w][h - 1];
		double[][] newEnergy = new double[w][h - 1];
		for (int i = 0; i < w; i++) {
			System.arraycopy(color[i] , 0, newColor[i] , 0, seam[i]);
			System.arraycopy(energy[i], 0, newEnergy[i], 0, seam[i]);
			if (seam[i] < h - 1) {
				System.arraycopy(color[i] , seam[i] + 1, newColor[i] , seam[i]	, h - 1 - seam[i]);
				System.arraycopy(energy[i], seam[i] + 1, newEnergy[i], seam[i]	, h - 1 - seam[i]);
			}
		}
		for (int i = 0; i < w; i++) {
			if (seam[i] > 0)
				newEnergy[i][seam[i]-1] = calcEnergy(i, seam[i]-1);
			else
				newEnergy[i][0] = calcEnergy(i, 0);

			if(seam[i] < h - 1)
				newEnergy[i][seam[i]] = calcEnergy(i, seam[i]);
		}
		h--;
		color = newColor;
		energy = newEnergy;
		ready = false;
	}

	public void removeHorizontalSeam(int[] seam) { // remove horizontal seam from current picture
		if (transposed)	transpose();
		removeSeam(seam);
	}

	public void removeVerticalSeam(int[] seam) { // remove vertical seam from current picture
		if (!transposed) transpose();
		removeSeam(seam);
	}

	public void actionPerformed(ActionEvent e) {
		picture().actionPerformed(e);
    }

}