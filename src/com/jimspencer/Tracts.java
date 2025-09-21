package com.jimspencer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;


public class Tracts {

	static HashMap<Integer, HashMap<Integer, Double>> cache = new HashMap<>();
	static HashMap<Long, Double> cosMap = new HashMap<>();

	public static double getPot(Tract i, Tract j, Tract bestI, Tract bestJ) {    // bestI and bestJ combined into tract i.
		double pot = 0;
		double jBestJPot = 0;
		if (cache.get(j.origId) == null)
			pot += getPotPointByPoint(i, j);
		else {
			if (cache.get(j.origId).get(bestI.origId) == null) {
				pot += getPotPointByPoint(j, bestI);
			} else {
				pot += cache.get(j.origId).get(bestI.origId);
			}
			if (cache.get(j.origId).get(bestJ.origId) == null) {
				jBestJPot = getPotPointByPoint(j, bestJ);
				pot += jBestJPot;
			} else {
				jBestJPot = cache.get(j.origId).get(bestJ.origId);
				pot += jBestJPot;
			}
		}

		if (i.contentsSize() > 100) {        // jim todo if this is higher, then a j of say size 2 might have been cached at size 1 against ny
			// and then when it becomes size 2 it doesnt get cache fixed.
			if (cache.get(i.origId) == null) cache.put(i.origId, new HashMap<>());
			if (cache.get(j.origId) == null) cache.put(j.origId, new HashMap<>());
			cache.get(i.origId).put(j.origId, pot);    // jim todo dont need both
			cache.get(j.origId).put(i.origId, pot);
		} else {
			// have to update any caches involving bestI.
			HashMap<Integer, Double> bestICache = cache.get(bestI.origId);
			if (bestICache != null) {
				for (Integer key : bestICache.keySet()) {
					cache.get(bestI.origId).put(key, null);  // jim instead of remove, anything here can be the old i plus the new j.
					cache.get(key).put(bestI.origId, null);
				}
				cache.remove(bestJ.origId);    // bestJ no longer exists, it was swallowed up.
			}
		}
		// var rightPot = getPotPointByPoint(i, j);
		// var ratio = pot/rightPot;
		// if(ratio>1.01 || ratio<.99) {
		// 	throw new Error('asdf')
		// }
		return pot;
	}


	public static double d2(double x1, double y1, double x2, double y2) {
		if (cosMap.size() == 0) {
			for(long i=0; i<90; i++) {
				double iRad = i*Math.PI/180;
				cosMap.put(i, Math.cos(iRad));
			}
		}
		double averageLat = (Math.abs(y1)+Math.abs(y2))/2.0;
		long rounded = Math.round(averageLat);
		Double cosLat = cosMap.get(Math.abs(rounded));
//		double cosLat = Math.cos(averageLatRadians);
		double deltaLon = Math.abs(x2 - x1);
		if (deltaLon > 180) {
			deltaLon = 360 - deltaLon;
		}
		double xMiles = deltaLon * 69.0 * cosLat;
		double yMiles = (y2 - y1) * 69.172;     // try this, to make a little bigger to try to deal with rectangular gridded pop points better at equator.
		double d2 = xMiles * xMiles + yMiles * yMiles;
		return d2;
	}

	public static double d4(double x1, double y1, double x2, double y2) {
		double d2 = d2(x1, y1, x2, y2);
//		return d2*d2;
		return d2*d2*d2;
	}

	static double distanceHav4(double lon1, double lat1,
	                           double lon2, double lat2)
	{
		// distance between latitudes and longitudes
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		// convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2) +
				Math.pow(Math.sin(dLon / 2), 2) *
						Math.cos(lat1) *
						Math.cos(lat2);
		double rad = 3960;
		double c = 2 * Math.asin(Math.sqrt(a));
		double distance = rad * c;
//		System.out.println(distance);
		return distance*distance*distance*distance;
	}

	public static double getPotPointByPoint(Tract ti, Tract tj) {
		if(tj == null || tj.contentsNull()) {
			System.out.println("got it");
		}
		double pot = 0;
		for (int a = 0; a < ti.contentsSize(); a++) {
			for (int b = 0; b < tj.contentsSize(); b++) {
				Tract anode = ti.contentsGet(a);
				Tract bnode = tj.contentsGet(b);
				pot += anode.n * bnode.n / d4(anode.x, anode.y, bnode.x, bnode.y);
			}
		}
		return pot;
	}


	public static void main(String[] args) throws IOException {
		System.out.println(Math.sqrt(d2(-117.16185539389278, 32.7170853999103, -74.00406935167295, 40.70864890457172)));
		System.out.println(Math.sqrt(d2(139.65278823290197, 35.67593068615613, -74.00406935167295, 40.70864890457172)));
		System.out.println(Math.sqrt(d2(-50.17097960990593, -21.253516326131926, -74.00406935167295, 40.70864890457172)));
		System.out.println(Math.sqrt(d2(-179, 0, 179, 0)));
		System.out.println(Math.sqrt(d2(-1, 0, 179, 0)));
		System.out.println(Math.sqrt(d2(0, 89, 0, -89)));



		long startTime = System.nanoTime();
		LinkedList<Tract> tracts = new LinkedList<>();
		try (PrintWriter out = new PrintWriter(
				"res/treeOutput.csv")) {

			File infile = new File(
//					"res/test.csv");
					"res/15sec.csv");
//			"res/30sec.csv");
//					"res/censusBlockGroups.csv");
//					"res/104split.csv");
//			"res/censusTracts.csv");
//			"res/usCongress.csv");
//			"res/caCongress2.csv");

			int max = 15644000;
			int numCities = 0;
			try (Scanner s = new Scanner(infile)) {
				@SuppressWarnings("unused")
				String header = s.nextLine();
				while (s.hasNextLine()
						&& numCities < max
				) {
					String str = s.nextLine();
//						System.out.println(str);
					Tract tract = new Tract(str, numCities);
					if (tract.n > 0) tracts.add(tract);
					numCities++;
				}
			}
//				System.out.println(regions.size());


// when calcBest, cache anything with size(i) + size(j) > 9.
// then when we combine two areas, the new one can either recalc
// or if both combinations are cached then we can just add and cache.
//
// anything that had one of the two that got combined as its fave,
// then its fave is the new one.  we can either recalc, or if both
// combinations are cached then add and cache.

			int ii = 0;
			ListIterator<Tract> iter = tracts.listIterator(0);
			while (iter.hasNext()) {
				if (ii % 1000 == 0) System.out.println(ii);

				Tract ti = iter.next();
				ListIterator<Tract> j = tracts.listIterator(ii + 1);
				while (j.hasNext()) {
					Tract tj = j.next();
					double pot = getPotPointByPoint(ti, tj);
					if (pot > ti.bestPot) {
						ti.bestPot = pot;
						ti.fav = tj;
					}
					if (pot > tj.bestPot) {
						tj.bestPot = pot;
						tj.fav = ti;
					}
				}
				ii++;
			}

			int count = 0;
			while (tracts.size() > 1) {
				if (count % 1000 == 0) System.out.println(count);
				count++;
				double maxPot = 0.0;
				Tract bestI = null;
				for (Tract i : tracts) {
					if (i.bestPot > maxPot) {
						maxPot = i.bestPot;
						bestI = i;
					}
				}
				Tract bestJ = bestI.fav;
				if (bestI.n < bestJ.n) {    // make the bigger one the first one.
					Tract swap = bestI;
					bestI = bestJ;
					bestJ = swap;
				}

				Tract node = new Tract(out, count, bestI, bestJ);

				// can bestPot and favs be determined from cache?? jim todo
				// we need to calculate best for the new node.
				// and any that had one of these 2 old nodes as best, we need to recalculate also.

				tracts.addFirst(node);
				boolean bestIFound = tracts.remove(bestI);
				boolean bestJFound = tracts.remove(bestJ);
				if(!bestIFound || !bestJFound) {
					System.out.println("oops couldnt remove");
				}

				for (Tract t : tracts) {
					Tract head = tracts.getFirst();
					if (t == head) continue;
					double pot = getPot(head, t, bestI, bestJ);

					if (pot > head.bestPot) {
						head.bestPot = pot;
						head.fav = t;
					}
					if (pot > t.bestPot) {
						t.bestPot = pot;
						t.fav = head;
					}

				}
				bestI.contentsSetNull();  // must release this memory.
				bestJ.contentsSetNull();  // must release this memory.

			}
			System.out.println(tracts.getFirst().n);
			System.out.println(System.nanoTime() - startTime);
		}
	}

}



