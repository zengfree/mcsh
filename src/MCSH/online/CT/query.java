package MCSH.online.CT;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//短路径
public class query {
    public static void main(String[] args) {
//        String queryfile = Config.QueryFile+"q_small.txt";
//        String queryfile ="/home/hadoop/dblequerynodes.txt";
        String queryfile ="randomfsq_0-0-3-6-0.txt";
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            System.out.println("querynodes size:"+querynodes.size());
            stdin.close();

            stdin = new BufferedReader(new FileReader("C:\\zxj\\mcsh\\src\\corr_fsq.txt"));
            Map<Integer,Integer> he2ho = new HashMap<>();
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
					he2ho.put(Integer.parseInt(s[1]),Integer.parseInt(s[0]));
//                he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
            }
            stdin.close();

            File logfile = new File(Config.QueryFile+"qfsq.txt");
            FileWriter fw = new FileWriter(logfile, false);
//            int num = 0;
            for(int queryid:querynodes){
//                if(num>100) break; num++;
                fw.write(he2ho.get(queryid)+"\r\n");
            }
            fw.close();
        }catch (IOException ioException) {
            System.out.println(ioException);
        }

    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
