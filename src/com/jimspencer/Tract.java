package com.jimspencer;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Tract {

	double origLong=0.0;
	double origLat=0.0;
	double bestPot;
	private ArrayList<Tract> contents = new ArrayList<>();;
	double x;
	double y;
	double n;
	Integer origId =-1;
//	String debugId;
	Tract fav = null;
	Tract left = null;
	Tract right = null;


	public Tract(String str, Integer id) {
		String[] fields = str.split(",");
		for (int i=0; i< fields.length; i++) {
			fields[i] = fields[i].trim();
		}
		origLong = Double.parseDouble(fields[0]);
		origLat = Double.parseDouble(fields[1]);
		n = Integer.parseInt(fields[2]);
		x = origLong;
		y = origLat;
		this.origId = id;
//		this.debugId = origId.toString();
		bestPot = 0.0;
		contents.add(this);

	}


	// jim todo: To draw a line across the 180th meridian,
// if the longitude of the second point minus
// the longitude of original (or previous) point is >= 180,
// subtract 360 from the longitude of the second point.
// If it is less than 180, add 360 to the second point.
	
	public Tract(PrintWriter out, int count, Tract bestI, Tract bestJ) {
		if (bestJ.n > 0) {
			out.print(count);
			out.print(" ");
			out.print(bestI.origId);
			out.print(" ");
			out.print(bestI.n);
			out.print(" ");
			out.print(bestI.y);
			out.print(" ");
			out.print(bestI.x);
			out.print(" ");
			out.print(bestI.origLat);
			out.print(" ");
			out.print(bestI.origLong);
			out.print(" ");
			out.print(bestJ.origId);
			out.print(" ");
			out.print(bestJ.n);
			out.print(" ");
			out.print(bestJ.y);
			out.print(" ");
			out.print(bestJ.x);
			out.print(" ");
			out.print(bestJ.origLat);
			out.print(" ");
			out.println(bestJ.origLong);
			out.flush();
		}

		this.origId = bestI.origId;
//		debugId = bestI.debugId +  ":" + bestJ.debugId;
		this.x = (bestI.x * bestI.n + bestJ.x * bestJ.n) / (bestI.n + bestJ.n);
		this.y = (bestI.y * bestI.n + bestJ.y * bestJ.n) / (bestI.n + bestJ.n);
		this.n = bestI.n + bestJ.n;
		this.bestPot = 0.0;
//			node.left = bestI;
//			node.right = bestJ;
		if(bestJ.contentsNull()) {
			System.out.println("found a null");
		}
		contentsAddAll(bestI);
		contentsAddAll(bestJ);
		this.origLat = bestI.origLat;
		this.origLong = bestI.origLong;
	}

	public int contentsSize() {
		return contents.size();
	}

	public boolean contentsNull() {
		if(contents == null) {
			System.out.println("got a null");
		}
		return contents == null;
	}

	public Tract contentsGet(int a) {
		return contents.get(a);
	}

	public void contentsSetNull() {
		contents = null;    // release memory
	}

	public void contentsAddAll(Tract t) {
		try {
			if(t==null || t.contents == null) {
				System.out.println("jim adding null wont work.");
			}
			contents.addAll(t.contents);
		} catch (Error e) {
			System.out.println("out of memory");
		}

	}
}
