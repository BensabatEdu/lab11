import java.util.LinkedList;

class FilaTarefas {
    private final int nThreads;
    private final MyPoolThreads[] threads;
    private final LinkedList<Runnable> queue;
    private boolean shutdown;

    public FilaTarefas(int nThreads) {
        this.shutdown = false;
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new MyPoolThreads[nThreads];
        for (int i=0; i<nThreads; i++) {
            threads[i] = new MyPoolThreads();
            threads[i].start(); 
        } 
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            if (this.shutdown) return;
            queue.addLast(r);
            queue.notify();
        }
    }
    
    public void shutdown() {
        synchronized(queue) {
            this.shutdown=true;
            queue.notifyAll();
        }
        for (int i=0; i<nThreads; i++) {
          try { threads[i].join(); } catch (InterruptedException e) { return; }
        }
    }

    private class MyPoolThreads extends Thread {
      @Override
      public void run() {
        Runnable r;
        while (true) {
          synchronized(queue) {
            while (queue.isEmpty() && (!shutdown)) {
              try { queue.wait(); }
              catch (InterruptedException ignored){}
            }
            if (queue.isEmpty()) return;   
            r = (Runnable) queue.removeFirst();
          }
          try { r.run(); }
          catch (RuntimeException e) {}
        } 
      } 
    } 
}

class Hello implements Runnable {
    String msg;
    public Hello(String m) { msg = m; }
    @Override
    public void run() {
      System.out.println(msg);
  }
}

class Primo implements Runnable {
    private final long numero;

    public Primo(long numero) {
        this.numero = numero;
    }

    @Override
    public void run() {
        if (numero <= 1) {
            System.out.printf("%d não é primo%n", numero);
            return;
        }
        if (numero == 2) {
            System.out.printf("%d é primo%n", numero);
            return;
        }
        if (numero % 2 == 0) {
            System.out.printf("%d não é primo (divisível por 2)%n", numero);
            return;
        }
        long limite = (long) Math.sqrt(numero) + 1;
        for (long i = 3; i <= limite; i += 2) {
            if (numero % i == 0) {
                System.out.printf("%d não é primo (divisível por %d)%n", numero, i);
                return;
            }
        }
        System.out.printf("%d é primo%n", numero);
    }
}


//Classe da aplicação (método main)
class MyPool {
    private static final int NTHREADS = 10;
    private static final int N = 100;
    public static void main (String[] args) {
      FilaTarefas pool = new FilaTarefas(NTHREADS); 

      for (int i = 0; i < N; i++) {
        final String m = "Hello da tarefa " + i;
        Runnable hello = new Hello(m);
        pool.execute(hello);
        Runnable primo = new Primo(i);
        pool.execute(primo);
      }

      pool.shutdown();
      System.out.println("Terminou");
  }
}