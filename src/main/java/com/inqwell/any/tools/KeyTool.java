package com.inqwell.any.tools;

import java.io.*;
import java.security.*;
import java.security.spec.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.util.CommandArgs;
import com.inqwell.any.util.SendMailConstants;

/**
 * Utility class for encrypting/decrypting files.
 * 
 * @author Michael Lones
 */
public class KeyTool
{

	public static final int	AES_Key_Size = 128;

	public static final String PATH_SEP = System.getProperty("file.separator");

	/**
	 * String to hold the name of the private key file.
	 */
	public static final String PRIVATE_KEY_FILE	= System.getProperty("user.home")
			                                            + PATH_SEP
			                                            + ".ssh"
			                                            + PATH_SEP
			                                            + "private.der";

	/**
	 * String to hold name of the public key file.
	 */
	public static final String PUBLIC_KEY_FILE	= System.getProperty("user.home")
                                                  + PATH_SEP
                                                  + ".ssh"
                                                  + PATH_SEP
                                                  + "public.der";

	/**
	 * String to hold name of the public key file.
	 */
	public static final String AES_KEY_FILE	= PUBLIC_KEY_FILE + ".aes";

	public static final String TST_OP	= System.getProperty("user.home")
                                                  + PATH_SEP
                                                  + "tom.txt";

	Cipher									pkCipher, aesCipher;
	byte[]									aesKey;
	SecretKeySpec						aeskeySpec;

	static void usage ()
	{
		System.out.println ("KeyTool: Usage");
		System.out.println ("      -genkey <file>");
		System.out.println ("      -private <file>");
		System.exit(1);
	}

	/**
	 * Constructor: creates ciphers
	 */
	public KeyTool() throws GeneralSecurityException
	{
		// create RSA public key cipher
		pkCipher = Cipher.getInstance("RSA");
		// create AES shared key cipher
		aesCipher = Cipher.getInstance("AES");
	}

	/**
	 * Creates a new AES key
	 */
	public void makeKey() throws NoSuchAlgorithmException
	{
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(AES_Key_Size);
		SecretKey key = kgen.generateKey();
		aesKey = key.getEncoded();
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}

	/**
	 * Decrypts an AES key from a file using an RSA private key
	 */
	public void loadKey(File in, File publicKeyFile)
			throws GeneralSecurityException, IOException
	{
		// read private key to be used to decrypt the AES key
		byte[] encodedKey = new byte[(int) publicKeyFile.length()];
		new FileInputStream(publicKeyFile).read(encodedKey);

		// create private key
//		PKCS8EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pk = kf.generatePublic(publicKeySpec);

		// read AES key
		pkCipher.init(Cipher.DECRYPT_MODE, pk);
		aesKey = new byte[AES_Key_Size / 8];
		CipherInputStream is = new CipherInputStream(new FileInputStream(in),
				pkCipher);
		is.read(aesKey);
		aeskeySpec = new SecretKeySpec(aesKey, "AES");
	}

	/**
	 * Encrypts the AES key to a file using an RSA public key
	 */
	public void saveKey(File out, File privateKeyFile)
			throws IOException, GeneralSecurityException
	{
		// read public key to be used to encrypt the AES key
		byte[] encodedKey = new byte[(int) privateKeyFile.length()];
		new FileInputStream(privateKeyFile).read(encodedKey);

		// create public key
//		X509EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(encodedKey);
  	PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey pk = kf.generatePrivate(privateKeySpec);

		// write AES key
		pkCipher.init(Cipher.ENCRYPT_MODE, pk);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out),
				pkCipher);
		os.write(aesKey);
		os.close();
	}

	/**
	 * Encrypts and then copies the contents of a given file.
	 */
	public void encrypt(File in, File out) throws IOException, InvalidKeyException
	{
		aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);

		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out),
				aesCipher);

		copy(is, os);

		os.close();
	}

	/**
	 * Decrypts and then copies the contents of a given file.
	 */
	public void decrypt(File in, File out) throws IOException, InvalidKeyException
	{
		aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);

		CipherInputStream is = new CipherInputStream(new FileInputStream(in),
				aesCipher);
		FileOutputStream os = new FileOutputStream(out);

		copy(is, os);

		is.close();
		os.close();
	}

	/**
	 * Copies a stream.
	 */
	private void copy(InputStream is, OutputStream os) throws IOException
	{
		int i;
		byte[] b = new byte[1024];
		while ((i = is.read(b)) != -1)
		{
			os.write(b, 0, i);
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		CommandArgs cArgs = new CommandArgs(args);
		
		// Generate an encrypted AES key and save it to the specified file
		AnyString genkey = new AnyString();
		genkey.setNull();
		if (!cArgs.arg("-genkey", genkey))
			usage();

		// private key must be supplied
		AnyString privateKey = new AnyString();
		if (!cArgs.arg("-private", privateKey))
			usage();

		KeyTool secure = new KeyTool();

  	// to encrypt a file
  	secure.makeKey();
  	secure.saveKey(new File(genkey.toString()), new File(privateKey.toString()));
	}
}
