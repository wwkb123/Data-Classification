import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Arrays;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
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



public class MyMapReduce {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
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
/*
    KeyPairGenerator kpg;
    kpg = KeyPairGenerator.getInstance("EC","SunEC");
    ECGenParameterSpec ecsp;

    ecsp = new ECGenParameterSpec("secp192k1");
    kpg.initialize(ecsp);

    KeyPair kpU = kpg.genKeyPair();
    PrivateKey privKeyU = kpU.getPrivate();
    PublicKey pubKeyU = kpU.getPublic();
    System.out.println("User U: " + privKeyU.toString());
    System.out.println("User U: " + pubKeyU.toString());

    KeyPair kpV = kpg.genKeyPair();
    PrivateKey privKeyV = kpV.getPrivate();
    PublicKey pubKeyV = kpV.getPublic();
    System.out.println("User V: " + privKeyV.toString());
    System.out.println("User V: " + pubKeyV.toString());

    KeyAgreement ecdhU = KeyAgreement.getInstance("ECDH");
    ecdhU.init(privKeyU);
    ecdhU.doPhase(pubKeyV,true);

    KeyAgreement ecdhV = KeyAgreement.getInstance("ECDH");
    ecdhV.init(privKeyV);
    ecdhV.doPhase(pubKeyU,true);

    System.out.println("Secret computed by U: 0x" + 
                       (new BigInteger(1, ecdhU.generateSecret()).toString(16)).toUpperCase());
    System.out.println("Secret computed by V: 0x" + 
                       (new BigInteger(1, ecdhV.generateSecret()).toString(16)).toUpperCase());

*/
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

    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(MyMapReduce.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}

