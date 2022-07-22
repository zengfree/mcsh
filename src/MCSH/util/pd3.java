package MCSH.util;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class pd3 {
    public static void getShortestPaths(int[][] adjMatrix) {
        for(int k = 0;k < adjMatrix.length;k++) {
            for(int i = 0;i < adjMatrix.length;i++) {
                for(int j = 0;j < adjMatrix.length;j++) {
                    if(adjMatrix[i][k] != -1 && adjMatrix[k][j] != -1) {
                        int temp = adjMatrix[i][k] + adjMatrix[k][j];  //含有中间节点k的顶点i到顶点j的距离
                        if(adjMatrix[i][j] == -1 || adjMatrix[i][j] > temp)
                            adjMatrix[i][j] = temp;
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);

        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();

        String queryfile ="randomfsq_0-0-3-6-0.txt";
        int queryK = 80;
        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];
        int[] vertex = {0,3,0},edge={0,6};
        float langda =(float) 0.3;
        int n=5700;
//        float yu =(float) 0.5;

        MetaPath queryMPath = new MetaPath(vertex, edge);
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

            String datafile = "fsq"+"/"+langda+queryMPath+"data.txt";
            String idnexfile = "fsq"+"/"+langda+queryMPath+"index.n2";
            long avgt1 = 0,avgtm=0,avgtmp=0;
            float avgd1 = 0,avgd2 = 0,avgd3 =0;
            double P_distance = 0;
            double Density = 0;
            double P_distancem = 0;
            double Densitym = 0;
            double P_distancemp = 0;
            double Densitymp = 0;
            for(int queryid:querynodes){
                int[] main = {1,1};
                int[] text2 = new int[textnum];//计算度量值
                int[] cont = new int[contnum];
                for(int i = 0;i<textnum;i++){
                    if(attribute.get(queryid)[i]==1){
                        text2[i] = 1;
                    }else {
                        text2[i] = 1;
                    }
                }
                for(int i = 0;i<contnum;i++){
                    cont[i] = 1;
                }
                Gweight_float gweight = new Gweight_float(main, text2, cont, 2);
                Adistance_float adistance = new Adistance_float(attribute, gweight);
                System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
				adistance.setYu((float) 0.5);


//                dataInput dataInput = new dataInput(Config.FsqGraph,Config.FsqVertex,Config.FsqEdge,Config.Fsqattributed,queryMPath);
//
                System.out.println(queryid);
                long t1 = System.nanoTime();
                MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                Set<Integer> res1 = mix.query(queryid,queryK,queryMPath,n,langda);
                long t2 = System.nanoTime();
                System.out.println((t2-t1)/1e6);
                if(res1!=null){
                    avgt1 += t2-t1;
                    int edges = 0;
                    Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
                    BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
                    for (int curId:res1) {
                        Set<Integer> pnbSet = batchSearch.collect(curId, res1);
                        pnbMap.put(curId, pnbSet);
                        edges+=pnbSet.size();
                    }
                    Density+=0.5*edges/ res1.size();

                    int i=0;
                    Map<Integer,Integer> corr = new HashMap<>();
                    Map<Integer,Integer> corr2 = new HashMap<>();
                    for (int key:pnbMap.keySet()) {
                        corr.put(key,i);
                        corr2.put(i,key);
                        i++;
                    }
                    int a[][] = new int[res1.size()][res1.size()];
                    for (int key:pnbMap.keySet()) {
                        int id = corr.get(key);
                        Set<Integer> set = pnbMap.get(key);
                        for(int z=0;z<a.length;z++){
                            if(set.contains(corr2.get(z))) {
                                a[id][z]= 1;
                            }
                            else
                            {
                                a[id][z]= -1;
                            }
                        }
                    }

                    getShortestPaths(a);

                    int max = 0;
                    for (int[] ints : a) {
                        max = Math.max(max, Arrays.stream(ints).max().getAsInt());
                    }
                    System.out.println(max);
                    P_distance+=max;

                    float d1 = adistance.cal_subgraph_attr_dist(res1);//平均距离
                    avgd1 +=d1;
                    System.out.println("d1:"+d1);
                }

                long t3 = System.nanoTime();
//                MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                Set<Integer> res2 = mix.query(queryid,queryK,queryMPath,n,langda);
                long t4 = System.nanoTime();
                System.out.println((t4-t3)/1e6);
                if(res2!=null){
                    avgtm += t4-t3;
                    int edges = 0;
                    Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
                    BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
                    for (int curId:res2) {
                        Set<Integer> pnbSet = batchSearch.collect(curId, res2);
                        pnbMap.put(curId, pnbSet);
                        edges+=pnbSet.size();
                    }
                    Densitym+=0.5*edges/ res2.size();

                    int i=0;
                    Map<Integer,Integer> corr = new HashMap<>();
                    Map<Integer,Integer> corr2 = new HashMap<>();
                    for (int key:pnbMap.keySet()) {
                        corr.put(key,i);
                        corr2.put(i,key);
                        i++;
                    }
                    int a[][] = new int[res2.size()][res2.size()];
                    for (int key:pnbMap.keySet()) {
                        int id = corr.get(key);
                        Set<Integer> set = pnbMap.get(key);
                        for(int z=0;z<a.length;z++){
                            if(set.contains(corr2.get(z))) {
                                a[id][z]= 1;
                            }
                            else
                            {
                                a[id][z]= -1;
                            }
                        }
                    }

                    getShortestPaths(a);

                    int max = 0;
                    for (int[] ints : a) {
                        max = Math.max(max, Arrays.stream(ints).max().getAsInt());
                    }
                    System.out.println(max);
                    P_distancem+=max;

                    float d1 = adistance.cal_subgraph_attr_dist(res2);//平均距离
                    avgd2 +=d1;
                    System.out.println("dm:"+d1);
                }


                long t5 = System.nanoTime();
//                MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                Set<Integer> res3 = mix.query(queryid,queryK,queryMPath,n,langda);
                long t6 = System.nanoTime();
                System.out.println((t6-t5)/1e6);
                if(res3!=null){
                    avgtmp += t6-t5;
                    int edges = 0;
                    Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
                    BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
                    for (int curId:res3) {
                        Set<Integer> pnbSet = batchSearch.collect(curId, res3);
                        pnbMap.put(curId, pnbSet);
                        edges+=pnbSet.size();
                    }
                    Densitymp+=0.5*edges/ res3.size();

                    int i=0;
                    Map<Integer,Integer> corr = new HashMap<>();
                    Map<Integer,Integer> corr2 = new HashMap<>();
                    for (int key:pnbMap.keySet()) {
                        corr.put(key,i);
                        corr2.put(i,key);
                        i++;
                    }
                    int a[][] = new int[res3.size()][res3.size()];
                    for (int key:pnbMap.keySet()) {
                        int id = corr.get(key);
                        Set<Integer> set = pnbMap.get(key);
                        for(int z=0;z<a.length;z++){
                            if(set.contains(corr2.get(z))) {
                                a[id][z]= 1;
                            }
                            else
                            {
                                a[id][z]= -1;
                            }
                        }
                    }

                    getShortestPaths(a);

                    int max = 0;
                    for (int[] ints : a) {
                        max = Math.max(max, Arrays.stream(ints).max().getAsInt());
                    }
                    System.out.println(max);
                    P_distancemp+=max;

                    float d1 = adistance.cal_subgraph_attr_dist(res3);//平均距离
                    avgd3 +=d1;
                    System.out.println("dmp:"+d1);
                }
            }
            int num = 10;
            System.out.println("Density:"+Density/num);
            System.out.println("P_distance:"+P_distance/num);
            System.out.println("Densitym:"+Densitym/num);
            System.out.println("P_distancem:"+P_distancem/num);
            System.out.println("Densitymp:"+Densitymp/num);
            System.out.println("P_distancemp:"+P_distancemp/num);
            System.out.println("d1:"+avgd1/num);
            System.out.println("d2:"+avgd2/num);
            System.out.println("d3:"+avgd3/num);
        }catch (IOException e) {
            System.out.println(e);
        }

    }

}
