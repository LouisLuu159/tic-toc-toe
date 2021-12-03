package model;

import java.io.Serializable;

public enum Shapes implements Serializable{
	 RECTANGLE,
	 TICK,
	 LINE,
	 POLYGON,
	 CIRCLE;
	 
	 public static Shapes getShape(String shape) {
		 Shapes[] shapes = Shapes.values();
		 for(Shapes s : shapes) {
			 if(s.toString().equals(shape))
				 return s;
		 }
		return null;
	 }
	 
	 public static boolean equals(Shapes shape1, Shapes shape2) {
		 if(shape1.toString().equals(shape2.toString()))
			 return true;
		 return false;
	 }
}
