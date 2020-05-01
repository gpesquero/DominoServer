package org.dominoserver;

import java.util.ArrayList;

public class DominoTile {
	
	public int mNumber1;
	public int mNumber2;
	
	public DominoTile(int number1, int number2) {
		
		mNumber1 = number1;
		mNumber2 = number2;		
	}
		
	public static ArrayList<DominoTile> createAllTiles() {
		
		ArrayList<DominoTile> tiles = new ArrayList<DominoTile>();
		
		for(int i=0; i<=6; i++) {
			
			for(int j=i; j<=6; j++) {
				
				//Log.info("Create tile "+i+"-"+j);
				
				DominoTile tile = new DominoTile(i, j);
				
				tiles.add(tile);				
			}
		}
		
		Log.info("Created "+tiles.size()+" domino tiles");
		
		return tiles;
	}
	
	public void swapNumbers() {
		
		int aux = mNumber1;
		
		mNumber1 = mNumber2;
		
		mNumber2 = aux;
	}
}
