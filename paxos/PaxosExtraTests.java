package paxos;

import org.junit.Test;
import static org.junit.Assert.*;
public class PaxosExtraTests {

    private Paxos[] initPaxos(int npaxos){
        String host = "127.0.0.1";
        String[] peers = new String[npaxos];
        int[] ports = new int[npaxos];
        Paxos[] pxa = new Paxos[npaxos];
        for(int i = 0 ; i < npaxos; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < npaxos; i++){
            pxa[i] = new Paxos(i, peers, ports);
        }
        return pxa;
    }

  public  int ndecided(Paxos[] pax, int seq){
    Object value = new Object();
    State s = null;
    int count = 0;
    for(int i = 0; i < pax.length; i++){
      if(pax[i] != null){
        Paxos.retStatus stat = pax[i].Status(seq);
        if(stat != null){
          s = stat.state;
          Object dVal = stat.v;
          if(s == State.Decided){
            if(count > 0 && !value.equals(dVal))
              return -1;
            count++;
            value = dVal;
          }
        }
      }
    }
    return count;
  }

  //wait paxos to decide
  public boolean waitn(Paxos[] pax, int seq, int wanted){
    int waitInterval = 1000;
    int iter = 30;
    int num = -1;

    while(iter-- > 0){
      num = ndecided(pax, seq);
      if(num >= wanted) return true;
      try{
        Thread.sleep(waitInterval);
      } catch(InterruptedException e){
        e.printStackTrace();
      }
    }
    return false;
  }

  //clean up
  public void cleanup(Paxos[] pax){
    for(int i = 0;i < pax.length; i++){
      if(pax[i] != null) pax[i].Kill();
    }
  }

  //test single paxos server
  @Test
  public void test1(){
    int n = 1;
    Paxos[] pax = initPaxos(n);
    pax[0].Start(0,1000);
    assertTrue(waitn(pax, 0, n));
    cleanup(pax);
  }

  //multiple proposer
  @Test
  public void test2(){
    int n = 2;
    Paxos[] pax = initPaxos(n);
    pax[0].Start(0, 1000);
    pax[1].Start(0, 2000);
    assertTrue(waitn(pax, 0, n));
    cleanup(pax);
  }

  @Test
  public void test3(){
    int n = 5;
    Paxos[] pax = initPaxos(n);
    pax[0].Start(0, 1000);
    pax[1].Start(0, 2000);
    pax[2].Start(0, 3000);
    assertTrue(waitn(pax, 0, n));

    pax[3].Start(1, 1500);
    pax[4].Start(1, 2500);
    assertTrue(waitn(pax, 1, n));
    cleanup(pax);
  }

  @Test
  public void test4() {
    int n = 7;
    Paxos[] pax = initPaxos(n);
    pax[0].Start(0, "ooo");
    pax[4].Start(0, "xxx");
    pax[1].Start(1, "aaa");
    pax[3].Start(2, "aaa");
    pax[2].Start(0, "bbb");
    pax[0].Start(2, "ccc");
    pax[1].Start(2, "ttt");
    pax[5].Start(3, "kkk");
    pax[6].Start(3, "ppp");
    pax[5].Start(4, "jjj");
    assertTrue(waitn(pax, 0, n));
    assertTrue(waitn(pax, 1, n));
    assertTrue(waitn(pax, 2, n));
    assertTrue(waitn(pax, 3, n));
    assertTrue(waitn(pax, 4, n));
  }

    @Test
    public void TestDeaf(){

        final int npaxos = 5;
        Paxos[] pxa = initPaxos(npaxos);

        System.out.println("Test: Deaf proposer ...");
        pxa[0].Start(0, "hello");
        waitn(pxa, 0, npaxos);

        pxa[1].ports[0]= 1; //Set to be a port unknown to others, so its deaf
        pxa[1].ports[npaxos-1]= 1;
        pxa[1].Start(1, "goodbye");
        waitn(pxa, 1, npaxos/2 + 1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        int nd = ndecided(pxa, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != npaxos-2);

        pxa[0].Start(1, "xxx");
        waitn(pxa, 1, npaxos - 1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        nd = ndecided(pxa, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != npaxos-1);

        pxa[npaxos-1].Start(1, "yyy");
        waitn(pxa, 1, npaxos);
        System.out.println("... Passed");
        cleanup(pxa);

    }

  @Test
    public void TestForget(){

        final int npaxos = 6;
        Paxos[] pxa = initPaxos(npaxos);

        System.out.println("Test: Forgetting ...");

        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong initial Min() " + m, m > 0);
        }

        pxa[0].Start(0,"00");
        pxa[1].Start(1,"11");
        pxa[2].Start(2,"22");
        pxa[0].Start(6,"66");
        pxa[1].Start(7,"77");

        waitn(pxa, 0, npaxos);
        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        waitn(pxa, 1, npaxos);
        for(int i = 0; i < npaxos; i++){
            int m = pxa[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i].Done(0);
        }

        for(int i = 1; i < npaxos; i++){
            pxa[i].Done(1);
        }

        for(int i = 0; i < npaxos; i++){
            pxa[i].Start(8+i, "xx");
        }

        boolean ok = false;
        for(int iters = 0; iters < 12; iters++){
            ok = true;
            for(int i = 0; i < npaxos; i++){
                int s = pxa[i].Min();
                if(s != 1){
                    ok = false;
                }
            }
            if(ok) break;
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        assertFalse("Min() did not advance after Done()", ok != true);
        System.out.println("... Passed");

        cleanup(pxa);
  }
}
