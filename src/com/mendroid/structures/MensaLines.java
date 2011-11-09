// Updated: 10.10.2011 14:35

package com.mendroid.structures;

import java.io.Serializable;

public enum MensaLines implements Serializable {
	L1("Linie 1"), L2("Linie 2"), L3("Linie 3"), L4_5("Linie 4/5"), L6U(
			"Linie 6 Update", "L6 Update"), SB("Schnitzelbar"), A("Abend"), HT(
			"Heiﬂe Theke", "Cafeteria Heiﬂe Theke"), CQ("Curry Queen"), CT(
			"Cafeteria", "Cafeteria ab 14:30");

	private final String name;
	private final String ident;

	private MensaLines(String namestring, String identstring) {
		name = namestring;
		ident = identstring;
	}

	private MensaLines(String s) {
		this(s, s);
	}

	public String getName() {
		return name;
	}

	public String getIdent() {
		return ident;
	}

}