package org.infinispan.online.service.endpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.MemcachedClient;
import org.infinispan.online.service.utils.TestObjectCreator;

import static org.infinispan.online.service.utils.TestObjectCreator.generateConstBytes;
import static org.junit.Assert.*;

public class MemcachedTester implements EndpointTester {

   private String key = "memcachedKey";

   @Override
   public void testBasicEndpointCapabilities(URL urlToService) {
      //given
      MemcachedClient client = getClient(urlToService);
      //when
      client.set("memcachedKey", 0, "value");
      //then
      assertEquals("value", client.get("memcachedKey"));
   }

   @Override
   public void testPutPerformance(URL urlToService, long timeout, TimeUnit timeUnit) {
      //given
      MemcachedClient client = getClient(urlToService);
      long endTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
      TestObjectCreator testObjectCreator = new TestObjectCreator();

      //when
      while (System.currentTimeMillis() - endTime < 0) {
         String key = testObjectCreator.getRandomString(1000);
         String value = testObjectCreator.getRandomString(1000);
         client.set(key, 0, value);
      }
   }

   @Override
   public void testIfEndpointIsProtected(URL urlToService) {
      throw new UnsupportedOperationException("Authentication is not supported on Memcache");
   }

   public void putGetRemoveTest(URL urlToService) {
      MemcachedClient client = getClient(urlToService);

      stringPutGetTest(client);
      deleteTest(client);

      intPutGetTest(client);
      byteArrayPutGetTest(client);
   }

   private void byteArrayPutGetTest(MemcachedClient client) {
      //given
      byte[] byteArrayValue = generateConstBytes(4096);
      //when
      client.set(key, 0, byteArrayValue);
      //then
      assertArrayEquals(byteArrayValue, (byte[]) client.get(key));
   }

   private void intPutGetTest(MemcachedClient client) {
      //given
      Integer intValue = 5;
      //when
      client.set(key, 0, intValue);
      //then
      assertEquals(intValue, client.get(key));
   }

   private void deleteTest(MemcachedClient client) {
      //given
      client.set(key, 0, "value");
      //when
      client.delete(key);
      //then
      assertNull(client.get(key));
   }

   private void stringPutGetTest(MemcachedClient client) {
      //when
      client.set(key, 0, "value");
      //then
      assertEquals("value", client.get(key));
      //when
      client.set(key, 0, "newValue");
      //then
      assertEquals("newValue", client.get(key));
   }

   private MemcachedClient getClient(URL urlToService) {
      try {
         return new MemcachedClient(new InetSocketAddress(urlToService.getHost(), urlToService.getPort()));
      } catch (IOException e) {
         throw new RuntimeException("Unable to create a Memcached client", e);
      }
   }
}

