package MCSH.compare;

import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class dataInput {
    private String graphFile = null;
    private String vertexFile = null;
    private String edgeFile = null;
    private String contributeFile = null;
    private int vertexNum = 0;
    private int edgeNum = 0;
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private int contribute[][] = null;
    private MetaPath queryMPath = null;//the query meta-path
    public Map<Integer,Set<Integer>>G;
    public Map<Integer,Set<Integer>>Att;

    public dataInput(String graphFile,String vertexFile,String edgeFile,String contributeFile,MetaPath queryMPath){
        this.graphFile = graphFile;
        this.vertexFile = vertexFile;
        this.edgeFile = edgeFile;
        this.contributeFile=contributeFile;
        this.queryMPath=queryMPath;

        //compute the number of nodes
        try{
            File test= new File(graphFile);
            long fileLength = test.length();
            LineNumberReader rf = new LineNumberReader(new FileReader(test));
            if (rf != null) {
                rf.skip(fileLength);
                vertexNum = rf.getLineNumber();//obtain the number of nodes
            }
            rf.close();
        }catch(IOException e) {
            e.printStackTrace();
        }

        readGraph();
        readVertexType();
        readEdgeType();
        readContribute();

        //step 1: find all the vertices
        //遍历所有节点，找到与路径出发节点相同类型的节点
        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        G = new HashMap<Integer, Set<Integer>>();
        Att = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
                G.put(curId, pnbSet);
//                System.out.print(Integer.toString(curId)+" "+Integer.toString(pnbSet.size())+"\n");/////////////////
                Att.put(curId,new HashSet<Integer>());
                for(int j=0;j<contribute[curId].length;j++){
                    Att.get(curId).add(contribute[curId][j]);
                }
            }
        }
//        System.out.println(G.size());
    }

    //return the graph edge information
    public void readGraph(){
        graph= new int[vertexNum][];
        try{
            BufferedReader stdin = new BufferedReader(new FileReader(graphFile));

            String line = null;
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
                int vertexId = Integer.parseInt(s[0]);

                int nb[] = new int[s.length - 1];
                for(int i = 1;i < s.length;i ++)   nb[i - 1] = Integer.parseInt(s[i]);
                graph[vertexId] = nb;

                edgeNum += nb.length / 2;
            }
            stdin.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        System.out.println(graphFile + " |V|=" + vertexNum + " |E|=" + edgeNum / 2);//each edge is bidirectional
    }

    //return the type of each vertex
    public void readVertexType(){
        vertexType = new int[vertexNum];

        try{
            BufferedReader stdin = new BufferedReader(new FileReader(vertexFile));
            String line = null;
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
                int id = Integer.parseInt(s[0]);
                int type = Integer.parseInt(s[1]);
                vertexType[id] = type;
            }
            stdin.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //return the type of each edge
    public void readEdgeType(){
        edgeType = new int[edgeNum];

        try{
            BufferedReader stdin = new BufferedReader(new FileReader(edgeFile));
            String line = null;
            while((line = stdin.readLine()) != null){
                String s[] = line.split(" ");
                int id = Integer.parseInt(s[0]);
                int type = Integer.parseInt(s[1]);
                edgeType[id] = type;
            }
            stdin.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void readContribute(){
        contribute = new int[vertexNum][];
        try{
            BufferedReader stdin = new BufferedReader(new FileReader(contributeFile));
            String line = stdin.readLine();
            while((line = stdin.readLine()) != null){
                String s[] = line.replace('[',' ').replace(']',' ').split(" ");
                int vertexId = Integer.parseInt(s[0]);
                int cnt=0,tot=0;
                for(int i=1;i<s.length-2;i++)if(!s[i].isEmpty()&&Double.parseDouble(s[i])==1)cnt++;
                int arr[] = new int[cnt];
                cnt=0;
                for(int i=1;i<s.length-2;i++)if(!s[i].isEmpty()){
                    ++tot;
                    if(Double.parseDouble(s[i]) == 1)arr[cnt++]=tot;
                }
                contribute[vertexId] = arr;
            }
            stdin.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
