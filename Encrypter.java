import java.io.IOException;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

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


import de.flexiprovider.common.ies.IESParameterSpec;
import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry.BrainpoolP160r1;



public class Encrypter {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      

	//***get public key here
		
        StringTokenizer itr = new StringTokenizer(value.toString());
    
	//initialize keys
	KeyPairGenerator kpg;
	CurveParams ecParams;
	KeyPair keyPair;
	PublicKey pubKey = null;
	PrivateKey privKey = null;
	Cipher cipher;
	IESParameterSpec iesParams;

	try{
	Security.addProvider(new FlexiCoreProvider());
    	Security.addProvider(new FlexiECProvider());
    	kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");
	
    	ecParams = new BrainpoolP160r1();

    	kpg.initialize(ecParams, new SecureRandom());
   	keyPair = kpg.generateKeyPair();
    	pubKey = keyPair.getPublic();
    	privKey = keyPair.getPrivate();

    	
	}
	catch(Exception e){

	}

        while (itr.hasMoreTokens()) {
      		word.set(itr.nextToken());
	
		String currWord = word.toString();
		
		//if matches PatientID or SSN
		if(currWord.matches("^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$") || currWord.matches("^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$")){
    			try{
    	
				//encrypting
    				cipher = Cipher.getInstance("ECIES", "FlexiEC");
    				iesParams = new IESParameterSpec("AES128_CBC","HmacSHA1", null, null);	
				cipher.init(Cipher.ENCRYPT_MODE, pubKey, iesParams);

    				byte[] input = currWord.getBytes(StandardCharsets.UTF_8);
    				byte[] encrypted = cipher.doFinal(input);
    				String output = new String(encrypted,StandardCharsets.UTF_8);
	
				//System.out.println(currWord+" The encrypted word is "+output);
				word = new Text(output);
			}
			catch(Exception e){

			}
	}
        	context.write(word, one);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {

/*  debugging encrypting and decrypting

    Security.addProvider(new FlexiCoreProvider());
    Security.addProvider(new FlexiECProvider());
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECIES", "FlexiEC");

    CurveParams ecParams = new BrainpoolP160r1();

    kpg.initialize(ecParams, new SecureRandom());
    KeyPair keyPair = kpg.generateKeyPair();
    PublicKey pubKey = keyPair.getPublic();
    PrivateKey privKey = keyPair.getPrivate();

    Cipher cipher = Cipher.getInstance("ECIES", "FlexiEC");

    IESParameterSpec iesParams = new IESParameterSpec("AES128_CBC","HmacSHA1", null, null);

    cipher.init(Cipher.ENCRYPT_MODE, pubKey, iesParams);

    String test = "tommy";
    byte[] input = test.getBytes(StandardCharsets.UTF_8);
    byte[] encrypted = cipher.doFinal(input);
    String output = new String(encrypted,StandardCharsets.UTF_8);
    System.out.println("\n\n\n"+output);

    cipher.init(Cipher.DECRYPT_MODE, privKey, iesParams);
    byte[] decrypted = cipher.doFinal(encrypted);
    output = new String(decrypted,StandardCharsets.UTF_8);
    System.out.println(output+"\n\n\n\n\n");
*/

    long startTime = System.nanoTime();  

    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Encrypter");
    job.setJarByClass(Encrypter.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    boolean isCompleted = job.waitForCompletion(true);
    long estimatedTime = System.nanoTime() - startTime;
    double seconds = (double)estimatedTime / 1000000000.0;
    System.out.println("\n\nThe elapsed time is: "+ seconds+"\n\n");

    System.exit(isCompleted ? 0 : 1);
  }
}

