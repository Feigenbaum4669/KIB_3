
import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
//import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.cert.Certificate;

import org.apache.commons.codec.binary.Base64;

public class Encryptor {
	
	private static final int GCM_NONCE_LENGTH = 12; // in bytes
	private static final int GCM_TAG_LENGTH = 16; // in bytes
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static void saveFile(byte[] data,String pathname) throws IOException{
		FileOutputStream fos;
			fos = new FileOutputStream(pathname,false);
		
		fos.write(data);
		fos.close();
	}
	
	public static byte[] loadFile(String pathname) throws IOException{
		
		RandomAccessFile f = new RandomAccessFile(pathname, "r");
		byte[] data = new byte[(int)f.length()];
		f.readFully(data);
		f.close();
		return data;
	}
	
	
	
    public static void encrypt(Key key,IvParameterSpec ivSpec,GCMParameterSpec GCMspec,String encryptionSchema, String encryptionMode,String pathName) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException {
    	
        	//IvParameterSpec iv = new IvParameterSpec(hexStringToByteArray(initVector));
    		

            Cipher cipher = Cipher.getInstance(encryptionSchema+"/"+encryptionMode+"/PKCS5PADDING");
            if(encryptionMode.equals("CBC")||encryptionMode.equals("CTR")){
        	
            cipher.init(Cipher.ENCRYPT_MODE, key,ivSpec);
            }else if(encryptionMode.equals("GCM")){
            	
            	cipher.init(Cipher.ENCRYPT_MODE, key,GCMspec);
            }
            byte[] data=loadFile(pathName);
            byte[] encrypted = cipher.doFinal(data);
            saveFile(encrypted,pathName);
    
    }

    public static void decrypt(Key key,IvParameterSpec ivSpec,GCMParameterSpec GCMspec,String encryptionSchema, String encryptionMode, String path) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException  {
       
       
        	//IvParameterSpec iv = new IvParameterSpec(hexStringToByteArray(initVector));
           
    	

    	Cipher cipher = Cipher.getInstance(encryptionSchema+"/"+encryptionMode+"/PKCS5PADDING");
        if(encryptionMode.equals("CBC")||encryptionMode.equals("CTR")){
        
        cipher.init(Cipher.DECRYPT_MODE, key,ivSpec);
        }else if(encryptionMode.equals("GCM")){
        	cipher.init(Cipher.DECRYPT_MODE, key,GCMspec);
        }
            byte[] data=loadFile(path);
            byte[] original = cipher.doFinal(data);
            saveFile(original,path);
       
    }
     public static char[] askForPassword(){
     Console console = System.console();
     console.printf("Enter password: ");
     return console.readPassword();
    	 /*
    	 String username = null;
    	 BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Enter password: ");
         username = null;
         try {
             username = reader.readLine();
         } catch (IOException e) {
             e.printStackTrace();
         } 
         return username.toCharArray();*/
     }
     
     
     /*
     public static void setKey(SecretKey key, String encryptionSchema,String keyId,String keystorePath,char[] keystorePasswordCharArray){
    	 char[] password=askForPassword();
    	 KeyStore keyStore = KeyStore.getInstance("JKS");
    	 keyStore.load(new FileInputStream(keystorePath),password);
    	 CertAndKeyGen gen = new CertAndKeyGen(encryptionSchema,"SHA1WithRSA");
    	    gen.generate(1024);
    	 
    	    key=gen.getPrivateKey();
    	    X509Certificate cert=gen.getSelfCertificate(new X500Name("CN=ROOT"), (long)365*24*3600);
    	     
    	    X509Certificate[] chain = new X509Certificate[1];
    	    chain[0]=cert;
		
	
 		 keyStore.setKeyEntry(keyId,key, password,  chain);
 		keyStore.store(new FileOutputStream(keystorePath), password);
 		
 		
 	     
 	    keyStore.setCertificateEntry("single_cert", cert);
 	     
 	    keyStore.store(new FileOutputStream(encryptionSchema), password);

     }*/
     
     public static Key getKey(char[] passwd,String keyId, String keystorePath) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException{
    	 
    	 KeyStore ks = KeyStore.getInstance("JCEKS");
    	 InputStream readStream = new FileInputStream(keystorePath);
    	 ks.load(readStream, passwd);
    	 Key key= ks.getKey(keyId, passwd);
    	 readStream.close();
    	 return key;
     }
   
    
    public static void main(String[] args) {
    
    	/*
    	 * keytool -genseckey -alias mydomain -keyalg AES -keystore /home/feigenbaum/keys/KeyStore.jks -keysize 256 -storetype JCEKS*/
        String mode=args[0];
    	String encryptionSchema=args[1];
    	String encryptionMode=args[2];
    	String pathName=args[3];
    	String keystorePath=args[4];
    	String keyId=args[5];
    	
    	Key key=null;
    	
    	//char[] keystorePasswordCharArray=ksPS.toCharArray();
    	///home/myuser/my-keystore/mykeystore.jks
    	
    	//0 AES CBC /home/feigenbaum/Desktop/input /home/feigenbaum/keys/KeyStore.jks mydomain
    	
        //String pathName="/home/feigenbaum/Desktop/input";
    	//AES CBC /home/feigenbaum/Desktop/input
        //AES/CBC
        //String encrypted="3Vc6ehj2YLk0Puiz1cjxROk8stS8R8+qwRQid2lvqcDwOrZA9s1vJzMtQBcYmVbNXZnLub1pvzA5f7Xsfb3DwLp+obikXD9fItLDD3UYksjx54fkJmr2qbNs+6ytBlRvBh6BXRa08bk4l1SyD0sRlrfqKPvl6c1F7tX0i989BTY=";
       // System.out.println(decrypt(key, initVector,encrypt(key, initVector, "")));
        try {
        	if(!(encryptionMode.equals("CBC")||encryptionMode.equals("CTR")||encryptionMode.equals("GCM"))){
        		
            		System.out.println("Error: encryption mode "+encryptionMode+" is not supported.\n (use: CBC, CTR, GCM )\n" );
            		return;
            	}
            	if(!encryptionSchema.equals("AES")){
            		System.out.println("Error: encryption schema "+encryptionSchema+" is not supported.\n (use: AES )\n" );
            		return;
            	}
            byte[] ivByte = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivSpec = new IvParameterSpec(ivByte);
            final byte[] nonce = new byte[GCM_NONCE_LENGTH];
        	GCMParameterSpec GCMspec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            
        	if(mode.equals("e")){
        		
        		key=getKey(askForPassword(),keyId,keystorePath);
        		
        		encrypt(key ,ivSpec,GCMspec,encryptionSchema,encryptionMode, pathName);}
        	//setKey(key,encryptionSchema,keyId,keystorePath,keystorePasswordCharArray);
        	
        	
           else if(mode.equals("d")){
        	 
				key=getKey(askForPassword(),keyId,keystorePath);
			
			decrypt(key ,ivSpec,GCMspec,encryptionSchema,encryptionMode, pathName);
        }else{
        	
        System.out.println("Error: wrong mode (use: e-encrypt, d-decrypt)\n");
        return;
        }
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| IOException | UnrecoverableKeyException | KeyStoreException | CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //bruteForceDecrypt(key,initVector,encrypted);
    }
}