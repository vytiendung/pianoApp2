package com.vtd.pianoapp.common;

import java.util.Arrays;
import java.util.List;

public class NoteLabelManager {
	public List<String> labelList;
	private static NoteLabelManager instance;
	
	public static NoteLabelManager getInstance(){
		if(instance==null){
			instance=new NoteLabelManager();
		}
		
		return instance;
	}
	
	public void initLabelList(){
		labelList = Arrays.asList("A0", "A0m", "B0", "C1", "C1m", // 1111
				"D1", "D1m", "E1", "F1", "F1m", "G1", "G1m", "A1",// 22222222
				"A1m", "B1", "C2", "C2m", "D2", "D2m", "E2", "F2",// 333333333
				"F2m", "G2", "G2m", "A2", "A2m", "B2", "C3", "C3m",// 444444444
				"D3", "D3m", "E3", "F3", "F3m", "G3", "G3m", "A3",// 555555
				"A3m", "B3", "C4", "C4m", "D4", "D4m", "E4", "F4",// 66666666
				"F4m", "G4", "G4m", "A4", "A4m", "B4", "C5", "C5m",// 777777777
				"D5", "D5m", "E5", "F5", "F5m", "G5", "G5m", "A5",// 888888888
				"A5m", "B5", "C6", "C6m", "D6", "D6m", "E6", "F6",// 999999999
				"F6m", "G6", "G6m", "A6", "A6m", "B6", "C7", "C7m", "D7", "D7m", "E7", "F7", "F7m", "G7", "G7m", "A7", "A7m", "B7", "C8");
	}
	
}
