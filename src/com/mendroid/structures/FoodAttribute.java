package com.mendroid.structures;
import java.io.Serializable;


public enum FoodAttribute implements Serializable {
		
	AT_1, AT_2, AT_3 , AT_4, AT_5, AT_6, AT_7, AT_8, AT_9, AT_10, AT_11, AT_12, AT_13, AT_R, AT_S, AT_VEG,
	AT_VG, AT_MSC, AT_BIO, AT_NULL;
	
	private static final String[] DESC_AR = 	{	"mit Farbstoff",
													"mit Konservierungsstoff",
													"mit Antioxidationsmittel",
													"mit Geschmacksverstärker",
													"mit Phosphat",
													"Oberfläche gewachst",
													"geschwefelt",
													"Oliven geschwärzt",
													"mit Süßungsmittel",
													"kann bei übermäßigem Verzehr abführend wirken",
													"enthält eine Phenylalaninquelle",
													"kann Restalkohol enthalten",
													"Kennzeichnung siehe Tageskarte",
													"enthält Rindfleisch",
													"enthält Schweinefleisch ",
													"vegetarisches Gericht (ohne Fleischzusatz)",
													"veganes Gericht ",
													"MSC-zertifizierter Fisch",
													"kontrolliert biologischer Anbau mit EU Bio-Siegel",
													" - UNKNOWN -"};
	
	public String getDesc() {
		return DESC_AR[this.ordinal()];
	}
	
	public static FoodAttribute fromString(String s) {
		if (s.equals("1")) {
			return AT_1;
		} else if (s.equals("2")) {
			return AT_2;
		} else if (s.equals("3")) {
			return AT_3;
		} else if (s.equals("4")) {
			return AT_4;
		} else if (s.equals("5")) {
			return AT_5;
		} else if (s.equals("6")) {
			return AT_6;
		} else if (s.equals("7")) {
			return AT_7;
		} else if (s.equals("8")) {
			return AT_8;
		} else if (s.equals("9")) {
			return AT_9;
		} else if (s.equals("10")) {
			return AT_10;
		} else if (s.equals("11")) {
			return AT_11;
		} else if (s.equals("12")) {
			return AT_12;
		} else if (s.equals("13")) {
			return AT_13;
		} else if (s.equals("R")) {
			return AT_R;
		} else if (s.equals("S")) {
			return AT_S;
		} else if (s.equals("VEG")) {
			return AT_VEG;
		} else if (s.equals("VG")) {
			return AT_VG;
		} else if (s.equals("MSC")) {
			return AT_MSC;
		} else if (s.equals("Bio")) {
			return AT_BIO;
		}
		return AT_NULL;
	}
	
}
