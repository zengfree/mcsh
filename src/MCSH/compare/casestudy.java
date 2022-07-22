package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class casestudy {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,"smallatt.txt");
//		DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
		String queryfile ="qs.txt";
		int queryK = 5;
//		int queryM = 4;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
//		int[] vertex = {1,0,1},edge={3,0};
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
			stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\nodes.txt"));
			Map<Integer,String> he2ho = new HashMap<>();
			while((line = stdin.readLine()) != null){
				String s[] = line.split("\t");
				he2ho.put(Integer.parseInt(s[0]),s[1]);
//                he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
			}
			stdin.close();

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

//				dataInput dataInput = new dataInput(Config.dblpGraph,Config.dblpVertex,Config.dblpEdge,Config.dblpattributed,queryMPath);
				dataInput dataInput = new dataInput(Config.authorGraph,Config.authorVertex,Config.authorEdge,Config.authorattribute,queryMPath);

				adistance.setYu((float) 0.2);
				long t1 = System.nanoTime();
				E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
				Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);


//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//				Set<Integer> res1 = app.queryM_protect(queryid,queryK,queryMPath,10);

//				ATC new_graph = new ATC(dataInput.G,dataInput.Att);
//				Set<Integer> st = new HashSet<Integer>(dataInput.Att.get(queryid));
//				Map<Integer,Set<Integer>> result = new_graph.query(queryK,1000, queryid,st);
//
//				VAC myquery = new VAC(queryid,queryK, dataInput.G, dataInput.Att);
//				Map<Integer,Set<Integer>> resultvac = myquery.query(dataInput.G,queryid,queryK);
				long t2 = System.nanoTime();
//				if(result==null) continue;
//				Set<Integer> res2 = result.keySet();
//				if(resultvac==null) continue;
//				Set<Integer> res3 = resultvac.keySet();
				System.out.println((t2-t1)/1e6);
				FileWriter fileWriter = new FileWriter("case study.txt",true);
				if(res1!=null){
					fileWriter.write(he2ho.get(queryid)+"\r\n");
					StringBuilder stringBuffer = new StringBuilder();
					stringBuffer.append("OURS").append(":");
					for(int nei:res1){
						stringBuffer.append(",").append(he2ho.get(nei));
					}
					stringBuffer.append("\r\n");
					fileWriter.write(stringBuffer.toString());

					float d1 = adistance.cal_subgraph_attr_dist(res1);//平均距离
					float d2 = adistance.cal_maxdist(res1);//最大距离
					float d3 = adistance.cal_fugailv(res1,text);//覆盖率
					float d4 = adistance.maxnum(res1,text);//最大共享数s
					System.out.println(d1);
					System.out.println(d2);
					System.out.println(d3);
					System.out.println(d4);
					System.out.println(res1.size());
					fileWriter.write("avgdist:"+d1+";"+"maxdist:"+d2+";"+"fugailv:"+d3+";"+"maxshare:"+d4+";"+"size:"+res1.size()+"\r\n");

//					StringBuilder stringBuffer2 = new StringBuilder();
//					stringBuffer2.append("ATC").append(":");
//					for(int nei:res2){
//						stringBuffer2.append(",").append(he2ho.get(nei));
//					}
//					stringBuffer2.append("\r\n");
//					fileWriter.write(stringBuffer2.toString());
//
//					 d1 = adistance.cal_subgraph_attr_dist(res2);//平均距离
//					 d2 = adistance.cal_maxdist(res2);//最大距离
//					 d3 = adistance.cal_fugailv(res2,text);//覆盖率
//					 d4 = adistance.maxnum(res2,text);//最大共享数s
//					fileWriter.write("avgdist:"+d1+";"+"maxdist:"+d2+";"+"fugailv:"+d3+";"+"maxshare:"+d4+";"+"size:"+res2.size()+"\r\n");
//
//					StringBuilder stringBuffer3 = new StringBuilder();
//					stringBuffer3.append("VAC").append(":");
//					for(int nei:res3){
//						stringBuffer3.append(",").append(he2ho.get(nei));
//					}
//					stringBuffer3.append("\r\n");
//					fileWriter.write(stringBuffer3.toString());
//
//					 d1 = adistance.cal_subgraph_attr_dist(res3);//平均距离
//					 d2 = adistance.cal_maxdist(res3);//最大距离
//					 d3 = adistance.cal_fugailv(res3,text);//覆盖率
//					 d4 = adistance.maxnum(res3,text);//最大共享数s
//					fileWriter.write("avgdist:"+d1+";"+"maxdist:"+d2+";"+"fugailv:"+d3+";"+"maxshare:"+d4+";"+"size:"+res3.size()+"\r\n");
				}
				fileWriter.close();


//				long t3 = System.nanoTime();
//				Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
//				Set<Integer> res = app.queryM_protect(queryid,queryK,queryMPath,queryM);
//				long t4 = System.nanoTime();
//				System.out.println((t4-t3)/1e6);
//				avgta += t4-t3;
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