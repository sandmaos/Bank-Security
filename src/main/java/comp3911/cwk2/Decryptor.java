package comp3911.cwk2;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.security.AlgorithmParameters;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decryptor {
  private static final FileSystem FS = FileSystems.getDefault();
  private static final String KEY_FILE = "key";
  private static final String ENCRYPTED_FILE = "database";

  private SecretKey key;
  private byte[] iv;
  private byte[] encryptedDB;

  public Decryptor() throws Exception {
    byte[] keyBytes = Files.readAllBytes(FS.getPath(KEY_FILE));
    key = new SecretKeySpec(keyBytes, "AES");
  }

  public void readData() throws Exception {
    byte[] data = Files.readAllBytes(FS.getPath(ENCRYPTED_FILE));

    ByteBuffer buffer = ByteBuffer.wrap(data);
    iv = new byte[16];
    buffer.get(iv);

    encryptedDB = new byte[buffer.remaining()];
    buffer.get(encryptedDB);
  }

  public String decryptData() throws Exception {
    AlgorithmParameters param = AlgorithmParameters.getInstance("AES");
    param.init(new IvParameterSpec(iv));

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key, param);
    byte[] decryptedDB = cipher.doFinal(encryptedDB);

    Path path = Files.createTempFile("db", null);
    System.out.print(path);
    Files.write(path, decryptedDB);

    return path.toString();
  }
}
