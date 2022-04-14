package MCSH.online.basic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class T {

    public static void main(String[] args){
        Set<Integer> set = new HashSet<>();
        ExecutorService exec = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            exec.submit(()->{
                set.add(finalI);
            });
        }

        exec.shutdown();
        while (true){
            if(exec.isTerminated()){
                break;
            }else {
                try{
                    Thread.sleep(1);
                }catch (InterruptedException exception){
                    exception.printStackTrace();
                }
            }
        }
    }

}
