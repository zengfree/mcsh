package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.BatchSearch;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.IOException;
import java.util.*;

public class T2 {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
//
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
//		String queryfile =Config.QueryFile+"q1.txt";
		int queryK = 5;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
		int[] vertex = {0,1,0},edge={0,0};
		MetaPath queryMPath = new MetaPath(vertex, edge);
		try {
			Set<Integer> keepSet = new HashSet<Integer>();
			for(int curId = 0;curId < graph.length;curId ++) {
				if(vertexType[curId] == queryMPath.vertex[0]) {
					keepSet.add(curId);
				}
			}

			//step 2: build the graph
			//对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
			Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
			BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
			for(int curId = 0;curId < graph.length;curId ++) {
				if(vertexType[curId] == queryMPath.vertex[0]) {
					Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
					pnbMap.put(curId, pnbSet);
				}
			}
			Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

			//step 1: find the vertices can be deleted in the first round
			Set<Integer> deleteSet = new HashSet<Integer>();
			for(Map.Entry<Integer, Set<Integer>> entry : pnbMap.entrySet()) {
				int curId = entry.getKey();
				Set<Integer> pnbSet = entry.getValue();
				if(pnbSet.size() < queryK) {
					queue.add(curId);
					deleteSet.add(curId);
				}
			}

			//step 2: delete vertices whose degrees are less than k
			while(queue.size() > 0) {
				int curId = queue.poll();//delete curId
				Set<Integer> pnbSet = pnbMap.get(curId);//找到curID对应的邻居
				for(int pnb:pnbSet) {//update curId's pnb
					if(!deleteSet.contains(pnb)) {
						Set<Integer> tmpSet = pnbMap.get(pnb);
						tmpSet.remove(curId);
						if(tmpSet.size() < queryK) {
							queue.add(pnb);
							deleteSet.add(pnb);
						}
					}
				}
//            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
				pnbMap.remove(curId);
			}

			long avgte = 0,avgta=0;
			float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
			int num =0;
			int size = 0;
			Map<Integer,Float> D1=new HashMap<>();
			Map<Integer,Float> D2=new HashMap<>();
			Map<Integer,Float> D3=new HashMap<>();
			Map<Integer,Float> D4=new HashMap<>();
			for(int queryid:pnbMap.keySet()){
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
//				System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
				System.out.println(queryid);
				long t1 = System.nanoTime();
				E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
				Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);

				long t2 = System.nanoTime();
				System.out.println((t2-t1)/1e6);
				if(res1!=null){
					num++;
					avgte += t2-t1;
					System.out.println("t"+(t2-t1)/1e6);
					float d1 = adistance.cal_subgraph_attr_dist(res1);//平均距离
					float d2 = adistance.cal_maxdist(res1);//最大距离
					float d3 = adistance.cal_fugailv(res1,text);//覆盖率
					float d4 = adistance.maxnum(res1,text);//最大共享数s
					avgd1 +=d1;avgd2+=d2;avgd3+=d3;avgd4 +=d4;
					D1.put(queryid,d1);
					D2.put(queryid,d2);
					D3.put(queryid,d3);
					D4.put(queryid,d4);
					System.out.println("d1:"+d1);
					System.out.println("d2:"+d2);
					System.out.println("d3:"+d3);
					System.out.println("d4:"+d4);
					size+= res1.size();
				}
			}

			List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(D1.entrySet());
			entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
				@Override
				public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//					return o2.getValue().compareTo(o1.getValue());//降序排列
					return o1.getValue().compareTo(o2.getValue());//升序排列
				}
			});

			List<Map.Entry<Integer, Float>> entryList2 = new ArrayList<>(D2.entrySet());
			entryList2.sort(new Comparator<Map.Entry<Integer, Float>>() {
				@Override
				public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
					return o1.getValue().compareTo(o2.getValue());//升序排列
				}
			});

			List<Map.Entry<Integer, Float>> entryList3 = new ArrayList<>(D3.entrySet());
			entryList3.sort(new Comparator<Map.Entry<Integer, Float>>() {
				@Override
				public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
					return o2.getValue().compareTo(o1.getValue());//降序排列
				}
			});

			List<Map.Entry<Integer, Float>> entryList4 = new ArrayList<>(D4.entrySet());
			entryList4.sort(new Comparator<Map.Entry<Integer, Float>>() {
				@Override
				public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
					return o2.getValue().compareTo(o1.getValue());//降序排列
				}
			});

			for (int i = 10; i <= 41; i++) {
//			int i=41;
				Set<Integer> set = new HashSet<>();
				Set<Integer> set2 = new HashSet<>();
				for (int j = 0; j < i; j++) {
					set.add(entryList.get(j).getKey());
				}
				for (int z = 0; z < i; z++) {
					set2.add(entryList2.get(z).getKey());
				}
				set.retainAll(set2);
				set2 = new HashSet<>();
				for (int j = 0; j < i; j++) {
//					set2 = new HashSet<>();
					set2.add(entryList3.get(j).getKey());
				}
				set.retainAll(set2);
				set2 = new HashSet<>();
				for (int j = 0; j < i; j++) {
					set2.add(entryList4.get(j).getKey());
				}
				set.retainAll(set2);
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(i).append(" ").append(" retain2 ");
				for (int key: set					) {
					stringBuilder.append(key).append(" ");
				}
				System.out.println(stringBuilder.toString());
			}


			System.out.println("d1:"+avgd1/num);
			System.out.println("d2:"+avgd2/num);
			System.out.println("d3:"+avgd3/num);
			System.out.println("d4:"+avgd4/num);
			System.out.println("t:"+avgte/num/1e6);
			System.out.println("size:"+1.0*size/num);
		}catch (Exception e) {
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