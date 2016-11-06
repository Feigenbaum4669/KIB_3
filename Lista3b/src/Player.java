
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;






public class Player {
	


private static String encryptionSchema="AES";
private static String encryptionMode="CBC";
private static String keyStr="5f954ea03d17347b3a02886782e1cb611f53bb2a5e0c09f8f206be7ea4446348";
private static String ivStr="031953ff4a33e80e56f87d6f802fa14f";
private static byte[] iv = hexStringToByteArray(ivStr);
private static IvParameterSpec ivSpec = new IvParameterSpec(iv);
private static byte[] keyBin=hexStringToByteArray(keyStr);
//private static Key key =new SecretKeySpec(hexStringToByteArray(keyStr),encryptionSchema);
private static Key key = new SecretKeySpec(keyBin,0,keyBin.length, encryptionSchema);

private static String cfPathname="config";
private static String PIN="";
private static String keystorePath="";
private static String keyId="";
private static String keyPassword="";
private static Key userKey=null;
private static String userIvStr =ivStr;
private static byte[] userIv = hexStringToByteArray(userIvStr);
private static IvParameterSpec userIvSpec = new IvParameterSpec(userIv);



public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
}

private static boolean authenticate(){
Scanner scan = new Scanner(System.in);
System.out.println("Enter PIN number:\n");
String res= scan.next();
//scan.close();
if(res.equals(PIN)){
	return true;
}else{
	return false;
}
}


	
private static byte[] readConfigFile(String pathname) throws IOException{
		
	RandomAccessFile f = new RandomAccessFile(pathname, "r");
	byte[] data = new byte[(int)f.length()];
	f.readFully(data);
	f.close();
	return data;
	}

private static void loadConfigFile(String pathname) throws IOException{
	byte[] configFileByteEncr=readConfigFile(pathname);
	try{
	byte[] configFileByteDecr=Encryptor.decrypt(configFileByteEncr,key,ivSpec,null, encryptionSchema, encryptionMode);
	//String configFileStr=configFileByteDecr.toString();
	String configFileStr = new String(configFileByteDecr, StandardCharsets.UTF_8);

	String configFileStrSplitted[] = configFileStr.split("\\r\\n|\\n|\\r");
	PIN=configFileStrSplitted[0];
	keystorePath=configFileStrSplitted[1];
	keyId=configFileStrSplitted[2];
	keyPassword=configFileStrSplitted[3];
	userKey=Encryptor.getKey(keyPassword.toCharArray(),keyId, keystorePath);
	System.out.println("config file loaded successfully.\n");
	
	}catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
			| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
			| IOException | UnrecoverableKeyException | KeyStoreException | CertificateException e) {
		
		e.printStackTrace();
	}

}

public static void saveConfigFile(byte[] data,String pathname) throws IOException{
	FileOutputStream fos;
		fos = new FileOutputStream(pathname,false);
	
	fos.write(data);
	fos.close();
}


private static void install(){
	//Console console = System.console();
	Scanner scan = new Scanner(System.in);
	System.out.println("-----INSTALLATION-----\n");
	System.out.println("Enter full path to keystore:\n");
	keystorePath = scan.next();
	System.out.println("Enter full path key id:\n");
	keyId = scan.next();
	System.out.println("Enter key password:\n");
	keyPassword = scan.next();
	System.out.println("Choose PIN number:\n");
	PIN= scan.next();
	//scan.close();
	String configFileStr=PIN+"\n"+keystorePath+"\n"+keyId+"\n"+keyPassword;
	byte[] configFileByte=configFileStr.getBytes();
	try{
	byte[] configFileByteEncrypted=Encryptor.encrypt(configFileByte,key,ivSpec,null, encryptionSchema, encryptionMode);
	saveConfigFile(configFileByteEncrypted,cfPathname);
	}catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
			| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
			| IOException e) {
		e.printStackTrace();
		System.out.println("Error: installation has failed.\n");
		return;
	}
}

public static boolean ask(){
	Scanner scan = new Scanner(System.in);
	String out = scan.next();
	//scan.y();
	if(out.equals("y")){
		return true;
	}else{
		return false;
	}
}

public static void encrypt(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{

byte[] data=Encryptor.loadFile(path);
byte[] dataEncr=Encryptor.encrypt(data, userKey, userIvSpec, null, encryptionSchema, encryptionMode);
Encryptor.saveFile(dataEncr,path);
System.out.println("File: "+path+" has been encrypted.\n");
}

public static MediaPlayer buildMediaPlayer(String path) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
byte[] dataEncr=Encryptor.loadFile(path);
byte[] dataDecr=Encryptor.decrypt(dataEncr, userKey, userIvSpec, null, encryptionSchema, encryptionMode);
Encryptor.saveFile(dataDecr,path);
System.out.println("Playing file: "+path+".\n");
String[] args = {path};
BootJavaFX.main(args);
Media hit = new Media(Paths.get(path).toUri().toString());
MediaPlayer mediaPlayer = new MediaPlayer(hit);
return mediaPlayer;
}

public static void stop(String path){
	/*TODO*/
	System.out.println("Playing file: "+path+" stopped.\n");
}

public static void menu() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException{
	Scanner scan = new Scanner(System.in);
	MediaPlayer mp=null;
	String out="0";
	String path=null;
	String path2=null;
	Boolean decr=false;
	while(!out.equals("e")){
	System.out.println("-----MENU-----");
	System.out.println("options: 0 - encrypt file, 1 - play file, s - stop, e - exit");
	out = scan.next();
	switch(out){
	case "0":
		if(mp!=null){
			mp.stop();
			BootJavaFX.shutdown();}
	if(decr){
		encrypt(path2);
		decr=false;
	}
	System.out.println("Enter path to file: ");
	path = scan.next();
	encrypt(path);
	break;
	case "1":
		if(mp!=null){
			mp.stop();
			BootJavaFX.shutdown();}
		if(decr){
			encrypt(path2);
			decr=false;
		}
	System.out.println("Enter path to file: ");
	decr=true;
	path2 = scan.next();
	mp=buildMediaPlayer(path2);
	mp.play();
	break;
	case "s":
		
	if(mp!=null){
	mp.stop();
	}else{
		System.out.println("Nothing to be stopped.\n");	
	}
	if(decr){
		encrypt(path2);
		decr=false;
	}
	break;
	default:
	break;
	}
	}
	encrypt(path2);
	BootJavaFX.shutdown();
	
}
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException{
		try {
			loadConfigFile(cfPathname);
			while(!authenticate()){
				System.out.println("Wrong PIN. Try again y/n?\n");
				if(!ask()){
					return;
				}
			}
			
		} catch (IOException e) {
			System.out.println("config file not found. Installation required. Continue y/n?\n");
			if(ask()){
				install();
				try {
					loadConfigFile(cfPathname);
				} catch (IOException g) {
					System.out.println("Error: config file not found. Installation failed.\n");
				}
			}else{
				return;
			}
			
		}
		menu();
		
			
		
		
			
		
	}


}
