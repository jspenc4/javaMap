package com.jimspencer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public class Tracts {

    static HashMap<Integer, HashMap<Integer, Double>> cache = new HashMap<>();
    static HashMap<Long, Double> cosMap = new HashMap<>();

    /**
     * Calculates the potential between two tracts, using cached values when available.
     *
     * @param i     The first tract (contains merged bestI and bestJ)
     * @param j     The second tract
     * @param bestI One of the original tracts merged into i
     * @param bestJ The other original tract merged into i
     * @return The calculated potential between the tracts
     */
    public static double getPot(Tract i, Tract j, Tract bestI, Tract bestJ) {
        // Validate inputs
        if (i == null || j == null || bestI == null || bestJ == null) {
            return 0.0; // Return default value for invalid inputs
        }

        double pot = 0.0;

        // Case 1: No cache entry for j exists
        if (cache.get(j.origId) == null) {
            pot = getPotPointByPoint(i, j);

            // Don't update cache here - we'll do it later if needed
        }
        // Case 2: Cache entry for j exists
        else {
            // Calculate potential between j and bestI
            if (cache.get(j.origId).get(bestI.origId) == null) {
                double jBestIPot = getPotPointByPoint(j, bestI);
                pot += jBestIPot;
            } else {
                pot += cache.get(j.origId).get(bestI.origId);
            }

            // Calculate potential between j and bestJ
            if (cache.get(j.origId).get(bestJ.origId) == null) {
                double jBestJPot = getPotPointByPoint(j, bestJ);
                pot += jBestJPot;
            } else {
                pot += cache.get(j.origId).get(bestJ.origId);
            }
        }

        // Cache update logic based on tract size
        if (i.contentsSize() > 100) {
            // For large tracts, update the cache with the new potential
            ensureCacheEntryExists(i.origId);
            ensureCacheEntryExists(j.origId);

            // Store the potential in both directions
            cache.get(i.origId).put(j.origId, pot);
            cache.get(j.origId).put(i.origId, pot);
        } else {
            // For smaller tracts, invalidate cache entries related to merged tracts
            invalidateCacheForMergedTracts(bestI, bestJ);
        }

        return pot;
    }

    /**
     * Ensures a cache entry exists for the given tract ID
     */
    private static void ensureCacheEntryExists(int tractId) {
        cache.computeIfAbsent(tractId, k -> new HashMap<>());
    }

    /**
     * Invalidates cache entries for merged tracts
     */
    private static void invalidateCacheForMergedTracts(Tract bestI, Tract bestJ) {
        // Invalidate cache entries for bestI
        HashMap<Integer, Double> bestICache = cache.get(bestI.origId);
        if (bestICache != null) {
            for (Integer key : bestICache.keySet()) {
                // Mark entries as invalid instead of removing them
                cache.get(bestI.origId).put(key, null);

                // Also invalidate the reverse mapping if it exists
                HashMap<Integer, Double> keyCache = cache.get(key);
                if (keyCache != null) {
                    keyCache.put(bestI.origId, null);
                }
            }
        }

        // Remove bestJ from cache as it no longer exists
        cache.remove(bestJ.origId);
    }

    public static double d2(double x1, double y1, double x2, double y2) {
        if (cosMap.isEmpty()) {
            for (long i = 0; i < 90; i++) {
                double iRad = i * Math.PI / 180;
                cosMap.put(i, Math.cos(iRad));
            }
        }
        double averageLat = (Math.abs(y1) + Math.abs(y2)) / 2.0;
        long rounded = Math.round(averageLat);
        Double cosLat = cosMap.get(Math.abs(rounded));
//		double cosLat = Math.cos(averageLatRadians);
        double deltaLon = Math.abs(x2 - x1);
        if (deltaLon > 180) {
            deltaLon = 360 - deltaLon;
        }
        double xMiles = deltaLon * 69.0 * cosLat;
        double yMiles = (y2 - y1) * 69.172;     // try this, to make a little bigger to try to deal with rectangular gridded pop points better at equator.
        return xMiles * xMiles + yMiles * yMiles;
    }

    public static double d4(double x1, double y1, double x2, double y2) {
        double d2 = d2(x1, y1, x2, y2);
        return d2 * d2;
    }

    /*
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
            return distance*distance*distance*distance;
        }
    */
    public static double getPotPointByPoint(Tract ti, Tract tj) {
        Objects.requireNonNull(ti, "First tract (ti) cannot be null");
        Objects.requireNonNull(tj, "Second tract (tj) cannot be null");

        if (ti.contentsNull()) {
            throw new IllegalStateException("First tract (ti) has null contents");
        }
        if (tj.contentsNull()) {
            throw new IllegalStateException("Second tract (tj) has null contents");
        }
        double pot = 0;
        for (int a = 0; a < ti.contentsSize(); a++) {
            for (int b = 0; b < Objects.requireNonNull(tj).contentsSize(); b++) {
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
        ArrayList<Tract> tracts = new ArrayList<>();
        try (PrintWriter out = new PrintWriter(
                "res/treeOutput.csv")) {

            File infile = new File(
//					"res/test.csv");
//					"res/15sec.csv");
//			"res/30sec.csv");
//					"res/censusBlockGroups.csv");
//					"res/104split.csv");
                    "res/censusTracts.csv");
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
                assert bestI != null;
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
                if (!bestIFound || !bestJFound) {
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



