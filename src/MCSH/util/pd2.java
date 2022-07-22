package MCSH.util;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.compare.ATC;
import MCSH.compare.dataInput;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class pd2 {
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
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);

//
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();

        String queryfile =Config.QueryFile + "randomfsq_0-0-3-6-0.txt";
        int queryK = 80;
        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];
//        int[] vertex = {0,1,0},edge={0,0};

//        int[] vertex = {1,0,1},edge={3,0};
//        int[] vertex = {1,0,1},edge={21,9};
        int[] vertex = {0,3,0},edge={0,6};


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


            long avgte = 0,avgta=0;
            float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
            double P_distance = 0;
            double Density = 0;
            int num=0;
            float avgpre = (float) 0;
            float avgrec = (float) 0;
            for(int queryid:querynodes){
                int[] main = {1,1};
                int[] text = new int[textnum];//查询社区
                int[] text2 = new int[textnum];//计算度量值
                int[] cont = new int[contnum];
                for(int i = 0;i<textnum;i++){
                    if(attribute.get(queryid)[i]==1){
                        text[i] = 1;
                        text2[i] = 1;
                    }else {
                        text[i] = 0;
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


//                dataInput dataInput = new dataInput(Config.IMDBGraph,Config.IMDBVertex,Config.IMDBEdge,Config.IMDBpersonattributed,queryMPath);
//				dataInput dataInput = new dataInput(Config.dblpGraph,Config.dblpVertex,Config.dblpEdge,Config.dblpattributed,queryMPath);
//				dataInput dataInput = new dataInput(Config.authorGraph,Config.authorVertex,Config.authorEdge,Config.authorattribute,queryMPath);
                dataInput dataInput = new dataInput(Config.FsqGraph,Config.FsqVertex,Config.FsqEdge,Config.Fsqattributed,queryMPath);
//                adistance.setYu((float) 0.5);
                System.out.println(queryid);
                long t1 = System.nanoTime();
//				E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
//				Set<Integer> result = e2.query(queryid,queryK,queryMPath);
//
//                MNindex app = new MNindex(graph,vertexType,edgeType,attribute,adistance,1200);
//                Set<Integer> res1 = app.query(queryid,queryK,queryMPath);
//                Set<Integer> res1 = app.queryM(queryid,queryK,queryMPath,10);
//				Set<Integer> res1 = app.queryM_protect(queryid,queryK,queryMPath,10);

//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//                Set<Integer> res1 = app.query(queryid,queryK,queryMPath);
//                Set<Integer> res1 = app.queryM(queryid,queryK,queryMPath,10);
//				Set<Integer> res1 = app.queryM_protect(queryid,queryK,queryMPath,10);



                ATC new_graph = new ATC(dataInput.G,dataInput.Att);
                Set<Integer> st = new HashSet<Integer>(dataInput.Att.get(queryid));
                Map<Integer,Set<Integer>> result = new_graph.query(queryK,1000, queryid,st);

//				VAC myquery = new VAC(queryid,queryK, dataInput.G, dataInput.Att);
//				Map<Integer,Set<Integer>> result = myquery.query(dataInput.G,queryid,queryK);
                long t2 = System.nanoTime();
                if(result==null) continue;
                Set<Integer> res1 = result.keySet();
                System.out.println((t2-t1)/1e6);
                if(res1!=null){
                    num++;
                    avgte += t2-t1;
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
                    float d2 = adistance.cal_maxdist(res1);//最大距离
                    float d3 = adistance.cal_fugailv(res1,text);//覆盖率
                    float d4 = adistance.maxnum(res1,text);//最大共享数s
                    avgd1 +=d1;avgd2+=d2;avgd3+=d3;avgd4 +=d4;
                    System.out.println("d1:"+d1);
                    System.out.println("d2:"+d2);
                    System.out.println("d3:"+d3);
                    System.out.println("d4:"+d4);
//                    Set<Integer> set = new HashSet<>(res1);
//                    set.retainAll(result);
//                    avgpre += (float) set.size()/res1.size();
//                    avgrec += (float) set.size()/result.size();
                }

            }
            System.out.println("Density:"+Density/num);
            System.out.println("P_distance:"+P_distance/num);
            System.out.println("d1:"+avgd1/num);
            System.out.println("d2:"+avgd2/num);
            System.out.println("d3:"+avgd3/num);
            System.out.println("d4:"+avgd4/num);
            System.out.println("t:"+avgte/num/1e6);
            System.out.println("pre:"+avgpre/num);
            System.out.println("rec:"+avgrec/num);
        }catch (IOException e) {
            System.out.println(e);
        }

    }

}
