package comp2402a5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IslandProvinces {
	/**
	 * @param r the reader to read from
	 * @param w the writer to write to
	 * @throws IOException
	 */
	public static void doIt(BufferedReader r, PrintWriter w) throws IOException {
		String line;
		int width = -1;
		String firstLine = null;
		while ((line = r.readLine()) != null) {
			if (line.length() == 0) continue;
			int cnt = countDigits01(line);
			if (cnt == 0) continue;
			width = cnt;
			firstLine = line;
			break;
		}
		if (width <= 0) return;

		UF uf = new UF();
		int[] prev = new int[width];
		if (firstLine != null) {
			processLine(firstLine, width, prev, uf);
		}
		while ((line = r.readLine()) != null) {
			if (line.length() == 0) continue;
			int cnt = countDigits01(line);
			if (cnt == 0) continue;
			if (cnt != width) throw new IOException("All lines must have the same number of digits");
			processLineWithPrev(line, width, prev, uf);
		}

		Map<Integer, Long> provinceSums = new HashMap<>();
		for (int i = 1; i < uf.nextLabel; i++) {
			if (uf.parent[i] == i && uf.size[i] > 0) {
				int s = sumDigits(uf.size[i]);
				provinceSums.put(s, provinceSums.getOrDefault(s, 0L) + (long)uf.size[i]);
			}
		}
		List<Long> provinces = new ArrayList<>(provinceSums.values());
		Collections.sort(provinces);
		for (long ps : provinces) w.println(ps);
	}

	private static void processLine(String line, int width, int[] outLabels, UF uf) throws IOException {
		int c = 0;
		int prevLeft = 0; 
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch != '0' && ch != '1') continue;
			if (c >= width) throw new IOException("Too many digits in a row");
			if (ch == '1') {
				int base = prevLeft;
				if (base == 0) base = uf.makeSet();
				int root = uf.find(base);
				uf.size[root] += 1;
				outLabels[c] = root;
				prevLeft = root;
			} else {
				outLabels[c] = 0;
				prevLeft = 0;
			}
			c++;
		}
		if (c != width) throw new IOException("Row has fewer digits than expected");
	}

	private static void processLineWithPrev(String line, int width, int[] prev, UF uf) throws IOException {
		int[] cur = new int[width];
		int c = 0;
		int left = 0;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch != '0' && ch != '1') continue;
			if (c >= width) throw new IOException("Too many digits in a row");
			if (ch == '1') {
				int upLeft = (c > 0) ? prev[c-1] : 0;
				int up = prev[c];
				int upRight = (c+1 < width) ? prev[c+1] : 0;
				int base = left != 0 ? left : (upLeft != 0 ? upLeft : (up != 0 ? up : upRight));
				if (base == 0) base = uf.makeSet();
				if (left != 0) base = uf.union(base, left);
				if (upLeft != 0) base = uf.union(base, upLeft);
				if (up != 0) base = uf.union(base, up);
				if (upRight != 0) base = uf.union(base, upRight);
				int root = uf.find(base);
				uf.size[root] += 1;
				cur[c] = root;
				left = root;
			} else {
				cur[c] = 0;
				left = 0;
			}
			c++;
		}
		if (c != width) throw new IOException("Row has fewer digits than expected");
		System.arraycopy(cur, 0, prev, 0, width);
	}

	private static int countDigits01(String line) {
		int n = 0;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '0' || ch == '1') n++;
		}
		return n;
	}

	private static int sumDigits(int x) {
		int s = 0;
		if (x == 0) return 0;
		while (x > 0) {
			s += x % 10;
			x /= 10;
		}
		return s;
	}

	private static class UF {
		int[] parent;
		int[] rank;
		int[] size;
		int nextLabel;
		UF() {
			parent = new int[1024];
			rank = new int[1024];
			size = new int[1024];
			nextLabel = 1; 
		}
		private void ensureCapacity(int need) {
			if (need < parent.length) return;
			int newCap = parent.length;
			while (newCap <= need) newCap *= 2;
			int[] np = new int[newCap];
			int[] nr = new int[newCap];
			int[] ns = new int[newCap];
			System.arraycopy(parent, 0, np, 0, parent.length);
			System.arraycopy(rank, 0, nr, 0, rank.length);
			System.arraycopy(size, 0, ns, 0, size.length);
			parent = np;
			rank = nr;
			size = ns;
		}
		int makeSet() {
			int id = nextLabel++;
			ensureCapacity(id + 1);
			parent[id] = id;
			rank[id] = 0;
			size[id] = 0;
			return id;
		}
		int find(int x) {
			while (x != parent[x]) {
				parent[x] = parent[parent[x]];
				x = parent[x];
			}
			return x;
		}
		int union(int a, int b) {
			int ra = find(a);
			int rb = find(b);
			if (ra == rb) return ra;
			if (rank[ra] < rank[rb]) {
				parent[ra] = rb;
				size[rb] += size[ra];
				return rb;
			} else if (rank[ra] > rank[rb]) {
				parent[rb] = ra;
				size[ra] += size[rb];
				return ra;
			} else {
				parent[rb] = ra;
				rank[ra]++;
				size[ra] += size[rb];
				return ra;
			}
		}
	}

	/**
	 * The driver.  Open a BufferedReader and a PrintWriter, either from System.in
	 * and System.out or from filenames specified on the command line, then call doIt.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader r;
			PrintWriter w;
			if (args.length == 0) {
				r = new BufferedReader(new InputStreamReader(System.in));
				w = new PrintWriter(System.out);
			} else if (args.length == 1) {
				r = new BufferedReader(new FileReader(args[0]));
				w = new PrintWriter(System.out);
			} else {
				r = new BufferedReader(new FileReader(args[0]));
				w = new PrintWriter(new FileWriter(args[1]));
			}
			long start = System.nanoTime();
			doIt(r, w);
			w.flush();
			long stop = System.nanoTime();
			System.out.println("Execution time: " + 1e-9 * (stop-start));
		} catch (IOException e) {
			System.err.println(e);
			System.exit(-1);
		}
	}
}