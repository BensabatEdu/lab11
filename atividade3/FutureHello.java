import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;


class MyCallable implements Callable<Long> {

  MyCallable() {
  }

  @Override
  public Long call() throws Exception {
    long s = 0;
    for (long i=1; i<=100; i++) {
      s++;
    }
    return s;
  }
}

class PrimoCallable implements Callable<Long> {
    private final long inicio;
    private final long fim;

    public PrimoCallable(long inicio, long fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        long lim = (long) Math.sqrt(n);
        for (long i = 3; i <= lim; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
    
    @Override
    public Long call() {
      long contagem = 0;
      for (long n = inicio; n <= fim; n++) {
        if (isPrime(n)) {
          contagem++;
        }
      }
      return contagem;
    }
}

//classe do método main
public class FutureHello {
  private static final int N_TOTAL = 1000000;
  private static final int N_PARTICOES = 10; 
  private static final int NTHREADS = 4;       
  
  @SuppressWarnings("CallToPrintStackTrace")
  public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(NTHREADS);
    List<Future<Long>> list = new ArrayList<Future<Long>>();

    long tamanhoParticao = N_TOTAL / N_PARTICOES;
    long inicioAtual = 1;

    for (int i = 0; i < N_PARTICOES; i++) {
        long fimAtual = inicioAtual + tamanhoParticao - 1;
        if (i == N_PARTICOES - 1) {
            fimAtual = N_TOTAL;
        }
        list.add(executor.submit(new PrimoCallable(inicioAtual, fimAtual)));
        inicioAtual = fimAtual + 1;
    }
    long totalPrimos = 0;
    for (Future<Long> future : list) {
        try {
        
            totalPrimos += future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    System.out.printf("Total de números primos no intervalo [1, %d]: %d%n", N_TOTAL, totalPrimos);
    executor.shutdown();
  }
}