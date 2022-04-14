package MCSH;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * read all the data
 */
public class DataReader {
	private String graphFile = null;
	private String vertexFile = null;
	private String edgeFile = null;
	private String correspondfile = null;
	private String attributable = null;
	private int vertexNum = 0;
	private int edgeNum = 0;
	private Map<Integer,Integer> correspond;
	
	public DataReader(String graphFile, String vertexFile, String edgeFile, String correspondfile,String attirbutedfile){
		this.graphFile = graphFile;
		this.vertexFile = vertexFile;
		this.edgeFile = edgeFile;
		this.correspondfile = correspondfile;
		this.attributable = attirbutedfile;
		
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
	}
	
	//return the graph edge information
	public int[][] readGraph(){
		int graph[][] = new int[vertexNum][];
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
		
		return graph;
	}

	//return the type of each vertex
	public int[] readVertexType(){
		int vertexType[] = new int[vertexNum];
		
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
		return vertexType;
	}
	
	//return the type of each edge
	public int[] readEdgeType(){
		int edgeType[] = new int[edgeNum];
		
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
		
		return edgeType;
	}

	public Map<Integer,Integer> transauthor2graph(){
		Map<Integer,Integer> map = new LinkedHashMap<>();
		int i = 0;
		try {
			BufferedReader stdin = new BufferedReader(new FileReader(correspondfile));
			String line = null;
			while((line = stdin.readLine()) != null){
				String s[] = line.split("\t");
				int value = Integer.parseInt(s[0]);
				map.put(i,value);
				i++;
			}
			stdin.close();
		}catch (Exception e){
			e.printStackTrace();
		}

		return  map;
	}

	public Map<Integer,double[]> readattributed(){
		Map<Integer,double[]> map = new HashMap<>();
		this.correspond = new HashMap<>();
		//int i = 0;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(attributable));
			String line = null;
			line = stdin.readLine();

			//处理第一行的数据
			String label[] = line.split(" ");
			double num_of_att[] = new double[2];
			for(int i=1;i< label.length;i = i+3){
				if(label[i].equals("text"))
					num_of_att[0] += Integer.parseInt(label[i+1]);
				else
					num_of_att[1] += Integer.parseInt(label[i+1]);
			}
			map.put(-1,num_of_att);

			//处理剩下的真实属性值
			int attnum = 0;
			while((line = stdin.readLine()) != null){
				String s[] = line.replace("[","").replace("]","").split(" ");
				double att[] = new double[s.length-1];
				for(int i=0;i<s.length-1;i++){
					att[i] = Double.parseDouble(s[i+1]);
				}
				int attid = Integer.parseInt(s[0]);
				map.put(attid,att);
				//i++;
				this.correspond.put(attnum,attid);
				attnum++;
			}
			stdin.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return map;
	}


	public Map<Integer,float[]> readattributed_float(){
		Map<Integer,float[]> map = new HashMap<>();
		this.correspond = new HashMap<>();
		//int i = 0;
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(attributable));
			String line = null;
			line = stdin.readLine();

			//处理第一行的数据
			String label[] = line.split(" ");
			float num_of_att[] = new float[2];
			for(int i=1;i< label.length;i = i+3){
				if(label[i].equals("text"))
					num_of_att[0] += Integer.parseInt(label[i+1]);
				else
					num_of_att[1] += Integer.parseInt(label[i+1]);
			}
			map.put(-1,num_of_att);

			//处理剩下的真实属性值
			int attnum = 0;
			while((line = stdin.readLine()) != null){
				String s[] = line.replace("[","").replace("]","").split(" ");
				float att[] = new float[s.length-1];
				for(int i=0;i<s.length-1;i++){
					att[i] = Float.parseFloat(s[i+1]);
				}
				int attid = Integer.parseInt(s[0]);
				map.put(attid,att);
				//i++;
				this.correspond.put(attnum,attid);
				attnum++;
			}
			stdin.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		return map;
	}

	public Map<Integer, Integer> getCorrespond() {
		return correspond;
	}

	public String trans(double[] V){
		StringBuffer str5 = new StringBuffer();
		for (double i : V) {
			str5.append(i+", ");
		}
		return str5.toString();
	}

	public String trans(Set<Integer> V){
//		if(V==null) return "";
		StringBuffer str5 = new StringBuffer();
		for (int i : V) {
			str5.append(i+", ");
		}
		return str5.toString();
	}

//	public static void main(String[] args) {
//		DataReader dataReader = new DataReader(Config.authorGraph,Config.authorVertex,Config.authorEdge,Config.authorA2G,Config.authorattribute);
//		Map<Integer,double[]> map = dataReader.readattributed();
//		for (Map.Entry<Integer,double[]> entry:map.entrySet()){
//			System.out.println(entry.getKey() + " " + dataReader.trans(entry.getValue()) );
//		}
//	}
}
