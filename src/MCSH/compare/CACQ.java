package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CACQ {

	public static void main(String[] args) throws IOException {
		DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
//
		Map<Integer,float[]> attribute = dataReader.readattributed_float();
		int[][] graph = dataReader.readGraph();
		int[] vertexType = dataReader.readVertexType();
		int[] edgeType = dataReader.readEdgeType();
		int queryK = 5;
		int queryid = 291;
		int textnum = (int)attribute.get(-1)[0];
		int contnum = (int)attribute.get(-1)[1];
//		int[] vertex = {1,0,1},edge={21,9};
		int[] vertex = {0,1,0},edge={0,0};
		MetaPath queryMPath = new MetaPath(vertex, edge);
		try {
				File file = new File("C:\\zxj\\mcsh\\src\\corr.txt");
				String line;
				BufferedReader stdin = new BufferedReader(new FileReader(file));
				Map<Integer,Integer> he2ho = new HashMap<>();
				while((line = stdin.readLine()) != null){
					String s[] = line.split(" ");
//					ho2he.put(Integer.parseInt(s[1]),Integer.parseInt(s[0]));
					he2ho.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
				}
				stdin.close();

				stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\nodes.txt"));
				Map<Integer,String> name = new HashMap<>();
				while((line = stdin.readLine()) != null){
					String s[] = line.split("\t");
					name.put(Integer.parseInt(s[0]),s[1]);
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

				E2 e2 = new E2(graph,vertexType,edgeType,attribute,adistance,8);
				Set<Integer> res1 = e2.query(queryid,queryK,queryMPath);

				file = new File("result.txt");
				stdin = new BufferedReader(new FileReader(file));
				while((line = stdin.readLine()) != null) {
					String s[] = line.split(" ");
					Set<Integer> res2 = new HashSet<>();
					for (int i = 1; i < s.length; i++) {
						res2.add(he2ho.get(Integer.parseInt(s[i])));
					}
					FileWriter fileWriter = new FileWriter("jiaoji.txt", true);
					if (res1 != null) {
						fileWriter.write(he2ho.get(queryid) + "\r\n");
						StringBuilder stringBuffer = new StringBuilder();
						Set<Integer> set = new HashSet<>(res1);
						set.retainAll(res2);
						Set<Integer> set1 = new HashSet<>(res1);
						set1.removeAll(res2);
						res2.removeAll(res1);
						for (int i : set) {
							fileWriter.write("," + name.get(i));
						}
						fileWriter.write("\r\n");
						for (int i : set1) {
							fileWriter.write("," + name.get(i));
						}
						fileWriter.write("\r\n");
						for (int i : res2) {
							fileWriter.write("," + name.get(i));
						}
						fileWriter.write("\r\n");
					}
					fileWriter.close();

				}
			stdin.close();
		}catch (IOException e) {
			System.out.println(e);
		}
	}

}