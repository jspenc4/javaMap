package com.jimspencer;

import java.util.ArrayList;
import java.util.HashMap;

public class MapEdge {
	int edgeId;
	int tractId1;
	double pop1;
	double lat1;
	double lon1;
	int tractId2;
	double pop2;
	double lat2;
	double lon2;

	public MapEdge(String str) {
		// 1,136516,1756,40.856735,-73.929225,40.856735,-73.929225,136517,970,40.856673,-73.929266,40.856673,-73.929266

		String[] fields = str.split(",");
		for (int i=0; i< fields.length; i++) {
			fields[i] = fields[i].trim();
		}

		edgeId = Integer.parseInt(fields[0]);
		tractId1 = Integer.parseInt(fields[1]);
		pop1 = Double.parseDouble(fields[2]);
		lat1 = Double.parseDouble(fields[5]);
		lon1 = Double.parseDouble(fields[6]);
		tractId2 = Integer.parseInt(fields[7]);
		pop2 = Double.parseDouble(fields[8]);
		lat2 = Double.parseDouble(fields[11]);
		lon2 = Double.parseDouble(fields[12]);


	}

	public void foldIn(ArrayList<Region> regions) {
		if(regions.get(tractId1) == null) {
			regions.set(tractId1, new Region(tractId1, pop1, lat1, lon1));
		}
		if (regions.get(tractId2) == null) {
			regions.set(tractId2, new Region(tractId2, pop2, lat2, lon2));
		}
		Region r1 = regions.get(tractId1);
		Region r2 = regions.get(tractId2);
		r1.foldIn(r2);
	}
}
