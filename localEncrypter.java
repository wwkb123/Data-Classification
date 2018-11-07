
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import de.flexiprovider.common.ies.IESParameterSpec;
import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry.BrainpoolP160r1;
import de.flexiprovider.ec.ECIES;

import java.io.IOException;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;


import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import javax.crypto.KeyAgreement;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;

public class localEncrypter{


	public static void main(String[] args){

		Security.addProvider(new FlexiCoreProvider());
    		Security.addProvider(new FlexiECProvider());

/*
    		KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");

    		CurveParams ecParams = new BrainpoolP160r1();

    		kpg.initialize(ecParams, new SecureRandom());
    		KeyPair keyPair = kpg.generateKeyPair();
    		PublicKey pubKey = keyPair.getPublic();
    		PrivateKey privKey = keyPair.getPrivate();

		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKey.getEncoded());
  		FileOutputStream fos = new FileOutputStream("/home/hduser/myfiles/public.key");
  		fos.write(x509EncodedKeySpec.getEncoded());
  		fos.close();

		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privKey.getEncoded());
		fos = new FileOutputStream("/home/hduser/myfiles/private.key");
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
*/

try{
		//read public
		File filePublicKey = new File("/home/hduser/myfiles/public.key");
  		FileInputStream fis = new FileInputStream("/home/hduser/myfiles/public.key");
  		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
  		fis.read(encodedPublicKey);
  		fis.close();



  		// Read Private Key.
  		File filePrivateKey = new File("/home/hduser/myfiles/private.key");
  		fis = new FileInputStream("/home/hduser/myfiles/private.key");
  		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
  		fis.read(encodedPrivateKey);
  		fis.close();

		//test
		KeyFactory keyFactory = KeyFactory.getInstance("ECIES");
  		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
  		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

 
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
  		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);


		//read input file
		BufferedReader br = new BufferedReader(new FileReader("/home/hduser/input.txt"));
		PrintWriter pw = new PrintWriter(new FileWriter("/home/hduser/output.txt"));
		char c = (char) br.read();
		String currWord = "";
		long startTime = System.nanoTime();  
    		

		while (c > 0 && c < 65535) {

      			if(c == '\t'){ //delimiter
				//if matches PatientID or SSN
				if(currWord.matches("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$") || currWord.matches("^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$")){

    					Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");

    					IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC","HmacSHA1", null, null);

    					cipher.init(Cipher.ENCRYPT_MODE, publicKey, iesParams);

    					//String test = "tommy";
   					byte[] input = currWord.getBytes(StandardCharsets.UTF_8);
    					byte[] encrypted = cipher.doFinal(input);
   					String output = new String(encrypted,StandardCharsets.UTF_8);
					pw.println(output);
					pw.flush();
    					//System.out.println("\n\n\n encrypted string is "+output);
/*
					//decipher
    					cipher.init(Cipher.DECRYPT_MODE, privateKey, iesParams);
    					byte[] decrypted = cipher.doFinal(encrypted);
    					output = new String(decrypted,StandardCharsets.UTF_8);
    					System.out.println("decrypted string is "+output+"\n\n\n\n\n");
*/
				}
				currWord = ""; //reset
			}else{
				currWord = currWord + c;
			}
		
		c = (char) br.read();
		}
	pw.flush();
	pw.close();
	long estimatedTime = System.nanoTime() - startTime;
    	double seconds = (double)estimatedTime / 1000000000.0;
	System.out.println("Finished");
    	System.out.println("\n\nThe elapsed time is: "+ seconds+"\n\n");
}catch(Exception e){
System.out.println("Error");
}

 	}









}
