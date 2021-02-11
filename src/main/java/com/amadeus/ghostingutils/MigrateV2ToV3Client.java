package com.amadeus.ghostingutils;

import com.amadeus.ghostingutils.migrate.MigrateV2ToV3;

public class MigrateV2ToV3Client {

	public static void main(String[] args) {
		MigrateV2ToV3 v2ToV3 = new MigrateV2ToV3(); 
		v2ToV3.migrateGhostFiles(); 
	}

}
