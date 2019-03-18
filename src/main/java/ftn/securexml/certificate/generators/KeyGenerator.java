package ftn.securexml.certificate.generators;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class KeyGenerator {
	
	public KeyGenerator(){

	}

	public KeyPair generateKeys() {
        try {
			//Generator para kljuceva
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); 
			//Za kreiranje kljuceva neophodno je definisati generator pseudoslucajnih brojeva
			//Ovaj generator mora biti bezbedan (nije jednostavno predvideti koje brojeve ce RNG generisati)
			//U ovom primeru se koristi generator zasnovan na SHA1 algoritmu, gde je SUN provajder
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			//inicijalizacija generatora, 2048 bitni kljuc
			keyGen.initialize(2048, random);

			//generise par kljuceva koji se sastoji od javnog i privatnog kljuca
			return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		}
        return null;
	}
}
