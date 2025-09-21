package com.jimspencer;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Region {
	int id;
	double pop;
	double lat;
	double lon;
	ArrayList<Region> members= new ArrayList<>();
	Region parent;

	public Region(int tractId, double pop, double lat, double lon) {
		id = tractId;
		this.pop = pop;
		this.lat = lat;
		this.lon = lon;
		members.add(this);
		parent = null;
	}


	public void foldIn(Region r2) {
		pop += r2.pop;
		// find edge with smallest distance between regions and write out edge!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		double minD = 6.0220943E+23;
		Region bestI = null, bestJ = null;
		for(Region i : members) {
			for (Region j : r2.members) {
				double d2 = Tracts.d2(i.lon, i.lat, j.lon, j.lat);
				if (d2 < minD) {
					minD = d2;
					bestI = i;
					bestJ = j;
				}
			}
		}
		drawEdge(bestI, bestJ);
		for(Region r : r2.members) {
			r.parent = this;
		}

		members.addAll(r2.members);
//		r2.members = null;      // do we need to free memory here?  todo jim
	}

	private void drawEdge(Region i, Region j) {
		PrintStream pw = System.out;
//		[[-73.929225,40.856735],[-73.929266,40.856673]],
		pw.print("[[");
		pw.print(i.lon);
		pw.print(",");
		pw.print(i.lat);
		pw.print("],[");
		pw.print(j.lon);
		pw.print(",");
		pw.print(j.lat);
		pw.println("]],");
	}
}
