package kvpaxos;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class KVPaxosTest {


  public void check(Client ck, String key, Integer value){
    Integer v = ck.Get(key);
    assertTrue("Get(" + key + ")=>" + v + ", expected " + value, v.equals(value));
  }

  @Test
  public void TestBasic(){
    final int npaxos = 10;
    String host = "127.0.0.1";
    String[] peers = new String[npaxos];
    int[] ports = new int[npaxos];

    Server[] kva = new Server[npaxos];
    for(int i = 0 ; i < npaxos; i++){
      ports[i] = 1100+i;
      peers[i] = host;
    }
    for(int i = 0; i < npaxos; i++){
      kva[i] = new Server(peers, ports, i);
    }

    Client[] ck = new Client[10];
    for (int i=0; i<ck.length; i++)
      ck[i] = new Client (peers, ports);

    System.out.println("Test: Basic put/get ...");
    for (int i=0; i<100; i++)
    {
      System.out.println ("i: " + i);
      for (int j=0; j<ck.length; j++)
      {
        ck[j].Put(Integer.toString (j) + "app", 6 + i + j);
        ck[j].Get(Integer.toString (j) + "app");
        ck[j].Put(Integer.toString (j) + "bla", -100 + i + j);
        ck[j].Get(Integer.toString (j) + "app");
        ck[j].Get(Integer.toString (j) + "app");
        ck[j].Put(Integer.toString (j) + "app", 5 + i + j);
        ck[j].Get(Integer.toString (j) + "app");
        ck[j].Put(Integer.toString (j) + "app", 6 + i + j);
        ck[j].Get(Integer.toString (j) + "app");
        ck[j].Get(Integer.toString (j) + "app");
        check(ck[j], Integer.toString (j) + "app", 6 + i + j);
        ck[j].Put(Integer.toString (j) + "a", 70 + i + j);
        check(ck[j], Integer.toString (j) + "a", 70 + i + j);
        check(ck[j], Integer.toString (j) + "bla", -100 + i + j);
      }
    }

    System.out.println("... Passed");

  }

}
