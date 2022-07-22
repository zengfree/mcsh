package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.*;

import java.io.*;
import java.util.*;

public class ACQt {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
//		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//		DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
//		DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
		String queryfile =Config.QueryFile+"q1.txt";
		int queryK = 80;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
//		int[] vertex = {1,0,1},edge={21,9};
//		int[] vertex = {1,0,1},edge={3,0};
		int[] vertex = {0,1,0},edge={0,0};
//		int[] vertex = {0,3,0},edge={0,6};

		MetaPath queryMPath = new MetaPath(vertex, edge);
		try {
			//获取查询条件
//			BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
//			String line;
//			Queue<Integer> querynodes = new LinkedList<>();
//			while((line = stdin.readLine()) != null){
//				int queryid = Integer.parseInt(line);
//				querynodes.add(queryid);
//			}
//			System.out.println("querynodes size:"+querynodes.size());
//			stdin.close();

				File file = new File("C:\\zxj\\mcsh\\src\\corr.txt");
				String line;
				BufferedReader stdin = new BufferedReader(new FileReader(file));
				Map<Integer,Integer> he2ho = new HashMap<>();
				Map<Integer,Integer> h02he = new HashMap<>();
				while((line = stdin.readLine()) != null){
					String s[] = line.split(" ");
//					ho2he.put(Integer.parseInt(s[1]),Integer.parseInt(s[0]));
					he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
				}
				stdin.close();


//				stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\nodes.txt"));
//				Map<Integer,String> name = new HashMap<>();
//				while((line = stdin.readLine()) != null){
//					String s[] = line.split("\t");
//					name.put(Integer.parseInt(s[0]),s[1]);
//				}
//				stdin.close();
				String Res = "result1.txt";
				file = new File("C:\\zxj\\论文\\实验代码\\Effective Community Search for Large Attributed Graphs\\VLDB 2015 Effective Community Search for Large Attributed Graphs [CODE]\\ACQ public codes\\"+Res);
				stdin = new BufferedReader(new FileReader(file));
//				long avgte = 0,avgta=0;
				double P_distance = 0;
				double Density = 0;
				float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
				int num =0;
				int size = 0;
				while((line = stdin.readLine()) != null){
					String s[] = line.split(" ");
					int queryid = he2ho.get(Integer.parseInt(s[0]));
					Set<Integer> res1 = new HashSet<>();
					for (int i = 1; i < s.length; i++) {
						res1.add(he2ho.get(Integer.parseInt(s[i])));
					}
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
					num++;

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

					pd.getShortestPaths(a);

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
//					FileWriter fileWriter = new FileWriter("ACQ.txt",true);
//					fileWriter.write(name.get(queryid)+"\r\n");
//					StringBuilder stringBuffer = new StringBuilder();
//					stringBuffer.append("ACQ").append(":");
//					for(int nei:res1){
//						stringBuffer.append(",").append(name.get(nei));
//					}
//					stringBuffer.append("\r\n");
//					fileWriter.write(stringBuffer.toString());
////
//					fileWriter.write("avgdist:"+d1+";"+"maxdist:"+d2+";"+"fugailv:"+d3+";"+"maxshare:"+d4+";"+"size:"+res1.size()+"\r\n");
					avgd1 +=d1;avgd2+=d2;avgd3+=d3;avgd4 +=d4;
//					System.out.println(s[0]);
					System.out.println("d1:"+d1);
					System.out.println("d2:"+d2);
					System.out.println("d3:"+d3);
					System.out.println("d4:"+d4);
//					size+= res1.size();
//					fileWriter.close();
				}
				stdin.close();
			System.out.println("Density:"+Density/num);
			System.out.println("P_distance:"+P_distance/num);
			System.out.println("d1:"+avgd1/num);
			System.out.println("d2:"+avgd2/num);
			System.out.println("d3:"+avgd3/num);
			System.out.println("d4:"+avgd4/num);
//			System.out.println("t:"+avgte/num/1e6);
			System.out.println("size:"+1.0*size/num);



//				long t3 = System.nanoTime();
//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//				Set<Integer> res = app.queryM_protect(queryid,queryK,queryMPath,queryM);
//				long t4 = System.nanoTime();
//				System.out.println((t4-t3)/1e6);
//				avgta += t4-t3;
//			}
//			System.out.println("d1:"+avgd1/num);
//			System.out.println("d2:"+avgd2/num);
//			System.out.println("d3:"+avgd3/num);
//			System.out.println("d4:"+avgd4/num);
//			System.out.println("t:"+avgte/num/1e6);
//			System.out.println("size:"+1.0*size/num);
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