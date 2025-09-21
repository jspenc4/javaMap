package com.jimspencer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class SpiderMap {
	static HashMap<Integer, HashMap<Integer, Double>> cache = new HashMap<>();
	static public HashMap<Long, Double> cosMap = new HashMap<>();

	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();

		for (long i = 0; i < 90; i++) {
			double iRad = i * Math.PI / 180;
			cosMap.put(i, Math.cos(iRad));
		}


		try (PrintWriter out = new PrintWriter(
				"res/spiderOut.csv")) {

			File infile = new File(
//					"res/testMapEdge.csv");
//			"res/censusBlockGroupResults.csv");
			"res/test2.csv");

			ArrayList<MapEdge> mapEdges = new ArrayList<>();
			int maxEdges = 300000;
			int maxTractId = 0;
			try (Scanner s = new Scanner(infile)) {
				@SuppressWarnings("unused")
				String header = s.nextLine();
				while (s.hasNextLine() && mapEdges.size() < maxEdges) {
					String str = s.nextLine();
					MapEdge edge = new MapEdge(str);
					mapEdges.add(edge);
					if(edge.tractId1 > maxTractId) maxTractId = edge.tractId1;
					if(edge.tractId2 > maxTractId) maxTractId = edge.tractId2;
				}
			}

			ArrayList<Region>  regions = new ArrayList<>(maxTractId);
			for(int i=0; i<=maxTractId; i++) {
				regions.add(i,null);
			}
			for(MapEdge edge : mapEdges) {

				edge.foldIn(regions);
			}
		}
	}
}
