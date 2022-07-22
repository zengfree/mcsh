package MCSH;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Global parameters
 */
public class Config {
	//stem file paths
	public static String stemFile = "./stemmer.lowercase.txt";
	public static String stopFile = "./stopword.txt";

	//the root of date files
//	public static String root = "D:\\MCSH\\CSH\\CSHDS";
//	public static String root = "/home_bak/zxj";
//	public static String root = "/home/star/zxj";
//	public static String root = "/home/zjlab/ANNS/zxj";
//	public static String root = "/home/hadoop/MCSH/CSHDS";
//	public static String root = "C:\\zxj\\CSHDS";
	public static String root = "/root/shiyan/";
	{
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File com=fsv.getHomeDirectory();
		root = com.getPath();//automatically obtain the path of Desktop
	}

/*	//SmallDBLP
	public static String smallDBLPRoot = root + "\\HIN\\dataset\\SmallDBLP\\";
	public static String smallDBLPGraph = smallDBLPRoot + "graph.txt";
	public static String smallDBLPVertex = smallDBLPRoot + "vertex.txt";
	public static String smallDBLPEdge = smallDBLPRoot + "edge.txt";*/

	//DBLP
	public static String dblpRoot = root + "/DBLP/";
	public static String dblpGraph = dblpRoot + "graph.txt";
	public static String dblpVertex = dblpRoot + "vertex.txt";
	public static String dblpEdge = dblpRoot + "edge.txt";
	public static String dblpattributed = dblpRoot + "DBLPdata.txt";
//	public static String dblpA2G = dblpRoot + "author.txt";
//	public static String dblpA2G1 = dblpRoot + "sorted_author.txt";


	//author
	public static String authorRoot = root + "/small_dblp/";
	public static String authorGraph = authorRoot + "graph.txt";
	public static String authorVertex = authorRoot + "vertex.txt";
	public static String authorEdge = authorRoot + "edge.txt";
//	public static String authorA2G = authorRoot + "nodes.txt";
	public static String authorattribute = authorRoot + "author_attribute2.txt";
	public static String a2 = authorRoot + "attribute.txt";


	//IMDB
	public static String IMDBRoot = root + "/IMDB/";
	public static String IMDBGraph = IMDBRoot + "IMDBgraph.txt";
	public static String IMDBVertex = IMDBRoot + "IMDBvertex.txt";
	public static String IMDBEdge = IMDBRoot + "IMDBedge.txt";
	public static String IMDBpersonattributed = IMDBRoot + "IMDBpersonattributes.txt";
	public static String IMDBmovieattributed = IMDBRoot + "IMDBmovieattributes.txt";

	//Foursquare
	public static String FsqRoot = root + "/Foursquare/";
	public static String FsqGraph = FsqRoot + "Foursquaregraph.txt";
	public static String FsqVertex = FsqRoot + "Foursquarevertex.txt";
	public static String FsqEdge = FsqRoot + "Foursquareedge.txt";
	public static String Fsqattributed = FsqRoot + "FoursquarePersonPrefernceAttributes.txt";
	public static String Fsqattributed2 = FsqRoot + "a1.txt";

	public static String machineName = "Sward";
//	public static String logFinalResultFile = Config.root + "/outdata/" + machineName;//our final experimental result data
	public static String logFinalResultFile = "./log";//our final experimental result data
	public static String logPartResultFile = Config.root + "/outdata/" + machineName + "-part";//intermediate result


	public static String ResultFile = root + "/result/";
	public static String QueryFile = root + "/query/";
	public static String IndexRoot = root +"/index/";
}
