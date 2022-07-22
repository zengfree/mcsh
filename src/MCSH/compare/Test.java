package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Test {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
//		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//		DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
		String queryfile =Config.QueryFile+"q1.txt";
		int queryK = 5;
//		int queryM = 4;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
//		int[] vertex = {1,0,1},edge={21,9};
		int[] vertex = {0,1,0},edge={0,0};
		MetaPath queryMPath = new MetaPath(vertex, edge);
		try {
			//获取查询条件
			BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
			String line;
			Queue<Integer> querynodes = new LinkedList<>();
			while((line = stdin.readLine()) != null){
				int queryid = Integer.parseInt(line);
//				querynodes.add(queryid);
			}
			System.out.println("querynodes size:"+querynodes.size());
			stdin.close();
			querynodes.add(876);
			long avgte = 0,avgta=0;
			float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
			int num =0;
			int size = 0;
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
//				adistance.setYu((float) 0.2);


//				dataInput dataInput = new dataInput(Config.IMDBGraph,Config.IMDBVertex,Config.IMDBEdge,Config.IMDBpersonattributed,queryMPath);
//				dataInput dataInput = new dataInput(Config.dblpGraph,Config.dblpVertex,Config.dblpEdge,Config.dblpattributed,queryMPath);
				dataInput dataInput = new dataInput(Config.authorGraph,Config.authorVertex,Config.authorEdge,Config.authorattribute,queryMPath);

				adistance.setYu((float) 0.2);
				System.out.println(queryid);
				long t1 = System.nanoTime();
				E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
				Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);


//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//				Set<Integer> res1 = app.queryM_protect(queryid,queryK,queryMPath,10);
//
//				dataInput.Att.get(queryid).add(6);
//				ATC new_graph = new ATC(dataInput.G,dataInput.Att);
//				Set<Integer> st = new HashSet<Integer>(dataInput.Att.get(queryid));
//				Map<Integer,Set<Integer>> result = new_graph.query(queryK,1000, queryid,st);

//				VAC myquery = new VAC(queryid,queryK, dataInput.G, dataInput.Att);
//				Map<Integer,Set<Integer>> result = myquery.query(dataInput.G,queryid,queryK);
				long t2 = System.nanoTime();
//				if(result==null) continue;
//				Set<Integer> res1 = result.keySet();
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
					System.out.println("d1:"+d1);
					System.out.println("d2:"+d2);
					System.out.println("d3:"+d3);
					System.out.println("d4:"+d4);
					size+= res1.size();
				}


//				long t3 = System.nanoTime();
//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//				Set<Integer> res = app.queryM_protect(queryid,queryK,queryMPath,queryM);
//				long t4 = System.nanoTime();
//				System.out.println((t4-t3)/1e6);
//				avgta += t4-t3;
			}
			System.out.println("d1:"+avgd1/num);
			System.out.println("d2:"+avgd2/num);
			System.out.println("d3:"+avgd3/num);
			System.out.println("d4:"+avgd4/num);
			System.out.println("t:"+avgte/num/1e6);
			System.out.println("size:"+1.0*size/num);
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