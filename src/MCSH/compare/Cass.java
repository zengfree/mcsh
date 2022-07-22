package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.MNindex;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.BatchSearch;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Cass {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,"smallatt.txt");
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
		int queryid = 876;
		int queryK = 5;
//		int queryM = 4;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
//		int[] vertex = {1,0,1},edge={3,0};
		int[] vertex = {0,1,0},edge={0,0};
		MetaPath queryMPath = new MetaPath(vertex, edge);

		try {
			//获取查询条件
			BufferedReader stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\nodes.txt"));
			String line;
			Map<Integer,String> he2ho = new HashMap<>();
			while((line = stdin.readLine()) != null){
				String s[] = line.split("\t");
				he2ho.put(Integer.parseInt(s[0]),s[1]);
//                he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
			}
			stdin.close();

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

			MNindex mNindex = new MNindex(graph,vertexType,edgeType,attribute,adistance,50);
			Map<Integer,Set<Integer>> map = mNindex.buildGraph1(queryMPath);
			mNindex.findKCore(map,queryK);
			Map<Integer,Float> distancemap = new HashMap<>();
			for(int key:map.keySet()){
				distancemap.put(key,adistance.cal_distance(key,queryid));
			}
			List<Map.Entry<Integer,Float>> entryList = new ArrayList<>(distancemap.entrySet());
			entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
				@Override
				public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			for (int i = 0; i < entryList.size(); i++) {
				System.out.println(he2ho.get(entryList.get(i).getKey())+":"+entryList.get(i).getValue());
			}

			adistance.setYu((float) 0.2);
			long t1 = System.nanoTime();
			E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
			Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);

			System.out.println();
			long t2 = System.nanoTime();
			System.out.println((t2-t1)/1e6);
			if(res1!=null){
				Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
				BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
				for (int curId:res1) {
					Set<Integer> pnbSet = batchSearch.collect(curId, res1);
					pnbMap.put(curId, pnbSet);
				}

				for(int key:pnbMap.keySet()){
					System.out.print(he2ho.get(key)+":");
					Set<Integer> set = pnbMap.get(key);
					for (int nei: set							) {
						System.out.print(he2ho.get(nei)+",");
					}
					System.out.println();
				}
			}

		}catch (IOException e) {
			System.out.println(e);
		}





//		System.out.println(queryMPath);
//		String root = "C:\\zxj\\CSHDS";
//
//		String dblpRoot = root + "\\small_dblp\\";
//		String dblpGraph = dblpRoot + "graph.txt";
//		String dblpVertex = dblpRoot + "vertex.txt";
//		String dblpEdge = dblpRoot + "edge.txt";
//		String contribute = dblpRoot + "author_attribute2.txt";
//
//		dataInput dataInp = new dataInput(dblpGraph,dblpVertex,dblpEdge,contribute,queryMPath);
//		long tea = System.nanoTime();
//		VAC myquery = new VAC(queryId,queryK, dataInp.G, dataInp.Att);
//		Map<Integer,Set<Integer>> my = myquery.query(dataInp.G,queryId,queryK);
//		long fgd = System.nanoTime();
//		System.out.println((fgd-tea)/1e6+"ms");




//		dataInput dataReader = new dataInput(dblpGraph,dblpVertex,dblpEdge,contribute,queryMPath);
//		ATC new_graph = new ATC(dataReader.G,dataReader.Att);
//
//		int n=5,i=0;
//		int queryK = 30;
//		int queryD = 1000;
//		Map<Integer,Set<Integer>> kcore = new_graph.K_Core(-1,queryK,dataReader.G);
//
//		long t1=System.nanoTime();
//		for(int queryid:kcore.keySet()){
////			int x=524304;
//			Set<Integer> st = new HashSet<>(dataReader.Att.get(queryid));
//			Map<Integer,Set<Integer>> result = new_graph.query(queryK,queryD, queryid,st);
//			if(result==null)System.out.println("queryID="+queryid+" null");
//			else{
//				System.out.print("queryID="+queryid);
//				System.out.print(" score="+new_graph.attribute_score(result.keySet(),st));
//				System.out.println(" size="+result.keySet().size());
//			}
//			if(++i==n)break;
//		}
//		long t2=System.nanoTime();
//		System.out.println(1.0*(t2-t1)/n/1e9);
	}

}