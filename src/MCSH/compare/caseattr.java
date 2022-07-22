package MCSH.compare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class caseattr {

	public static void main(String[] args) {
		try{
			String line;
			BufferedReader stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\nodes.txt"));
			Map<String,Integer> he2ho = new HashMap<>();
			while((line = stdin.readLine()) != null){
				String s[] = line.split("\t");
//				he2ho.put(Integer.parseInt(s[0]),s[1]);
                he2ho.put(s[1],Integer.parseInt(s[0]));
			}
			stdin.close();

			stdin = new BufferedReader(new FileReader("C:\\zxj\\论文\\编造数据\\author\\author.txt"));
			Map<String,Integer> numattribute3 = new HashMap<>();
			while((line = stdin.readLine()) != null){
				String s[] = line.split("\t");
//				he2ho.put(Integer.parseInt(s[0]),s[1]);
				numattribute3.put(s[0],Integer.parseInt(s[2]));
			}
			stdin.close();

			stdin = new BufferedReader(new FileReader("11.txt"));
			Map<String,Integer> attmap = new HashMap<>();
			int index=0;
			Map<String,Set<Integer>> attribute = new HashMap<>();
			Map<String,Integer> numattribute1 = new HashMap<>();
			Map<String,Integer> numattribute2 = new HashMap<>();
			while((line = stdin.readLine()) != null){
				String[] s = line.split("\t");
				int id = he2ho.get(s[0]);
				attribute.put(s[0],new HashSet<>());
				numattribute1.put(s[0],Integer.parseInt(s[2]));
				numattribute2.put(s[0],Integer.parseInt(s[3]));
				if(s[1].equals("")){
					continue;
				}
				for(String s1:s[1].split(",")){
					s1 = s1.toLowerCase(Locale.ROOT).trim();
					if(attmap.containsKey(s1)){
						attribute.get(s[0]).add(attmap.get(s1));
					}else {
						attmap.put(s1,index);index++;
						attribute.get(s[0]).add(attmap.get(s1));
					}
				}

			}
			stdin.close();
			int max=0,min=Integer.MAX_VALUE;
			for (String name:numattribute1.keySet()				) {
				max = Math.max(max,numattribute3.get(name));
				min = Math.min(min, numattribute3.get(name));
			}
			System.out.println(max);
			System.out.println(min);
			System.out.println("READ FINISH!");

			FileWriter fw = new FileWriter("smallatt.txt",false);
			for (String name:numattribute1.keySet()				) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(he2ho.get(name));
				Set<Integer> a = attribute.get(name);
				for (int i = 0; i < attmap.size(); i++) {
					if(a.contains(i)){
						stringBuilder.append(" ").append(1);
					}else {
						stringBuilder.append(" ").append(0);
					}
				}

				stringBuilder.append(" ").append((1.0*numattribute1.get(name)- 1351)/(151632-1351));
				stringBuilder.append(" ").append((1.0*numattribute2.get(name)- 18)/(176-18));
				stringBuilder.append(" ").append((1.0*numattribute3.get(name)- 25)/(120-25));
				fw.write(stringBuilder.toString()+"\r\n");
			}
			fw.close();
		}catch (IOException e){
			System.out.println(e);
		}
	}

}