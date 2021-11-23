package com.yahoo.ycsb.db;

import com.xchen.LevelDB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

public class PebblesDbClient extends DB {

  private static LevelDB db = null;

  private static synchronized void getDBInstance() {
    if (db == null) {
    //   LevelDB.Options options = new LevelDB.Options();
      try {
        db = new new LevelDB("leveldb_database");
      } catch (IOException e) {
        System.out.println("Failed to open database");
        e.printStackTrace();
      }
    }
  }

  private static byte[] mapToBytes(Map<String, String> map)
      throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(byteOut);
    out.writeObject(map);
    return byteOut.toByteArray();
  }

  private static Map<String, String> bytesToMap(byte[] bytes)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    ObjectInputStream in = new ObjectInputStream(byteIn);
    @SuppressWarnings("unchecked")
    Map<String, String> map = (Map<String, String>) in.readObject();
    return map;
  }

  /**
   * Initialize any state for this DB. Called once per DB instance; there is
   * one DB instance per client thread.
   */
  @Override
  public void init() throws DBException {
    getDBInstance();
  }

  /**
   * Cleanup any state for this DB. Called once per DB instance; there is one
   * DB instance per client thread.
   */
  @Override
  public void cleanup() throws DBException {
      try {
        db.close();
      } catch (IOException e) {
        System.out.println("Failed to close db");
        e.printStackTrace();
      }
  }

  /**
   * Delete a record from the database.
   * 
   * @param table
   *            The name of the table
   * @param key
   *            The record key of the record to delete.
   * @return Zero on success, a non-zero error code on error. See this class's
   *         description for a discussion of error codes.
   */
  @Override
  public Status delete(String table, String key) {
    db.delete(key.getBytes());
    return Status.OK;
  }

  /**
   * Insert a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key.
   * 
   * @param table
   *            The name of the table
   * @param key
   *            The record key of the record to insert.
   * @param values
   *            A HashMap of field/value pairs to insert in the record
   * @return Zero on success, a non-zero error code on error. See this class's
   *         description for a discussion of error codes.
   */
  @Override
  public Status insert(String table, String key,
      Map<String, ByteIterator> values) {
    Map<String, String> stringValues = StringByteIterator.getStringMap(values);
    try {
      db.put(key.getBytes(), mapToBytes(stringValues));
    } catch (IOException e) {
      System.out.println("Failed to insert " + key);
      e.printStackTrace();
      return Status.ERROR;
    }
    return Status.OK;
  }

  /**
   * Read a record from the database. Each field/value pair from the result
   * will be stored in a HashMap.
   * 
   * @param table
   *            The name of the table
   * @param key
   *            The record key of the record to read.
   * @param fields
   *            The list of fields to read, or null for all of them
   * @param result
   *            A HashMap of field/value pairs for the result
   * @return Zero on success, a non-zero error code on error or "not found".
   */
  @Override
  public Status read(String table, String key, Set<String> fields,
      Map<String, ByteIterator> result) {
    byte[] value = db.get(key.getBytes());
    if (value == null) {
      return Status.ERROR;
    }
    Map<String, String> map;
    try {
      map = bytesToMap(value);
    } catch (IOException e) {
      System.out.println("Failed to read " + key);
      e.printStackTrace();
      return Status.ERROR;
    } catch (ClassNotFoundException e) {
      System.out.println("Failed to read " + key);
      e.printStackTrace();
      return Status.ERROR;
    }
    StringByteIterator.putAllAsByteIterators(result, map);
    return Status.OK;
  }

  /**
   * Update a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key, overwriting any existing values with the same field name.
   * 
   * @param table
   *            The name of the table
   * @param key
   *            The record key of the record to write.
   * @param values
   *            A HashMap of field/value pairs to update in the record
   * @return Zero on success, a non-zero error code on error. See this class's
   *         description for a discussion of error codes.
   */
  @Override
  public Status update(String table, String key,
      Map<String, ByteIterator> values) {
    byte[] existingBytes = db.get(key.getBytes());
    Map<String, String> existingValues;
    if (existingBytes != null) {
      try {
        existingValues = bytesToMap(existingBytes);
      } catch (IOException e) {
        System.out.println("Failed to read for update " + key);
        e.printStackTrace();
        return Status.ERROR;
      } catch (ClassNotFoundException e) {
        System.out.println("Failed to read for update " + key);
        e.printStackTrace();
        return Status.ERROR;
      }
    } else {
      existingValues = new HashMap<String, String>();
    }
    Map<String, String> newValues = StringByteIterator.getStringMap(values);
    existingValues.putAll(newValues);
    try {
      db.put(key.getBytes(), mapToBytes(existingValues));
    } catch (IOException e) {
      System.out.println("Failed to insert " + key);
      e.printStackTrace();
      return Status.ERROR;
    }
    return Status.OK;
  }

  /**
   * Perform a range scan for a set of records in the database. Each
   * field/value pair from the result will be stored in a HashMap.
   * 
   * @param table
   *            The name of the table
   * @param startkey
   *            The record key of the first record to read.
   * @param recordcount
   *            The number of records to read
   * @param fields
   *            The list of fields to read, or null for all of them
   * @param result
   *            A Vector of HashMaps, where each HashMap is a set field/value
   *            pairs for one record
   * @return Zero on success, a non-zero error code on error. See this class's
   *         description for a discussion of error codes.
   */
  @Override
  public Status scan(String table, String startkey, int recordcount,
      Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
    LevelDB.Iterator iterator = db.iterator();
    int count = 0;
    try {
      iterator.seek(startkey.getBytes());
      while (iterator.isValid() && count < recordcount) {
        String key = iterator.key().toString();
        if (fields == null || fields.contains(key)) {
          HashMap<String, String> value;
          value = (HashMap<String, String>) bytesToMap(iterator
              .value());
          HashMap<String, ByteIterator> byteValues = new HashMap<String, ByteIterator>();
          StringByteIterator.putAllAsByteIterators(byteValues, value);
          result.addElement(byteValues);
        }
        iterator.next();
        count += 1;
      }
    } catch (IOException e) {
      System.out.println("Failed to scan");
      e.printStackTrace();
      return Status.ERROR;
    } catch (ClassNotFoundException e) {
      System.out.println("Failed to scan");
      e.printStackTrace();
      return Status.ERROR;
    } finally {
      try {
        iterator.close();
      } catch (IOException e) {
        System.out.println("Failed to close iterator");
        e.printStackTrace();
      }
    }
    return Status.OK;
  }
}