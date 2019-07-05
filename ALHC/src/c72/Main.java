package c72;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
	private static final String pathname="C:\\Users\\13739\\OneDrive\\桌面\\src_data.txt";
	private static final String w_pathname="C:\\Users\\13739\\OneDrive\\桌面\\des_data.txt";
	private static final int k=128;
	private static class Node{
		private Node c1;
		private Node c2;
		private char value;
		private long count;
		public Node(char value,long count){
			this(value,null,null,count);
		}
		public Node(Node c1, Node c2){
			this((char) (-1),c1,c2,0);
		}
		public Node(char value, Node c1, Node c2, long count){
			this.setValue(value);
			this.setC1(c1);
			this.setC2(c2);
			this.setCount(count);;
		}
		public Node getC1() {
			return c1;
		}
		public void setC1(Node c1) {
			this.c1 = c1;
		}
		public Node getC2() {
			return c2;
		}
		public void setC2(Node c2) {
			this.c2 = c2;
		}
		public char getValue() {
			return value;
		}
		public void setValue(char value) {
			this.value = value;
		}
		public long getCount() {
			return count;
		}
		public void setCount(long count) {
			this.count = count;
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Character> origin=new ArrayList<>();
		List<Node> group=new ArrayList<>();
		int[] buckets=new int[Character.MAX_VALUE];
		try {
			File file=new File(pathname);
			if(file.isFile()&&file.exists()) {
				BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line="";
				while(line!=null) {
					line=br.readLine();
					if(line!=null&&line.equals("")) break;
					String[] token=line.split(",");
					for(String s:token) {
						//分桶
						buckets[(Integer.valueOf(s))]++;
						origin.add(((char)(Integer.valueOf(s).intValue())));
					}
				}
				br.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		//将桶里的东西生成一个个集群
		for(int i=0;i<Character.MAX_VALUE;i++) {
			if(buckets[i]!=0) {
				group.add(new Node((char) i, buckets[i]));
			}
		}
		while(group.size()>1) {
			System.out.println("group_size:"+group.size());
			combine(group);
		}
		if(group.size()==0) {
			System.out.println("0 error");
		}else if(group.size()!=1) {
			System.out.print("negative error");
		}else {
			//获得k组集群
			while(group.size()!=k) {
				System.out.println("group_size1:"+group.size());
				List<Node> temp=new ArrayList<>();
				for(int i=0;i<group.size();i++) {
					temp.add(group.get(i).getC1());
					temp.add(group.get(i).getC2());
				}
				group.clear();
				group.addAll(temp);
			}
			//取得集群各自的基准值
			int[] base = new int[k];
			for(int i=0;i<k;i++) {
				Map<Character,Long> ts=new HashMap<>();
				getValue(group.get(i), ts);
				int average=0;
				long total_count=0;
				Set<Character> ts_c=ts.keySet();
				for(Character l : ts_c) {
					long count=ts.get(l);
					average+=l*count;
					total_count+=count;
				}
				average/=total_count;
				base[i]=average;
				//将原始数据映射到对应基准值下标
				for(int j=0;j<origin.size();j++) {
					if(ts_c.contains(origin.get(j))) {
						origin.set(j, new Character((char) i));
					}
				}
			}
			//写出数据
			File file = null;
	        file = new File(w_pathname);
	        try {
	            if (!file.exists()) {
	                file.createNewFile();
	            }
		        FileOutputStream fileOutputStream=new FileOutputStream(file);
	            for(int i = 0;i < k;i++){
	            fileOutputStream.write(base[i]);
	            fileOutputStream.flush();
	            }
	            fileOutputStream.write("\n".getBytes());
	            for(int i = 0;i < origin.size();i++){
		            fileOutputStream.write(origin.get(i).toString().getBytes());
		            fileOutputStream.flush();
		        }
	            fileOutputStream.close();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }finally{
	        }
		}
	}
	private static void combine(List<Node> group) {
		// TODO Auto-generated method stub
		List<Node> ngroup=new ArrayList<>();
		System.out.println("combining...");
		for(int i=0;i<group.size();i++) {
			System.out.println("4");
			
			Map<Character,Long> ts=new HashMap<>();	//this
			getValue(group.get(i), ts);
			
			double min_dis=Double.MAX_VALUE;
			int min_ind=-1;
			for(int j=i+1;j<group.size();j++) {
				System.out.println("("+i+","+j+")");
				
				Map<Character,Long> ot=new HashMap<>();	//other
				getValue(group.get(j), ot);
				//存放欧式距离和
				double temp_dis=0;
				//总共进行多少对计算
				long total_times=0;
				Set<Character> ts_c=ts.keySet();
				Set<Character> ot_c=ot.keySet();
				for(Character t_c : ts_c) {
					for(Character o_c : ot_c) {
						//进行多少次相同的计算
						long times=ts.get(t_c)*ot.get(o_c);
						total_times+=times;
						temp_dis+=times*Math.sqrt(Math.pow(t_c-o_c, 2));
					}
				}
				if(total_times!=0)
				temp_dis/=total_times;
				if(temp_dis<min_dis) {
					min_dis=temp_dis;
					min_ind=j;
				}
			}
			if(min_ind!=-1) {
				ngroup.add(new Node(group.get(i),group.get(min_ind)));
				group.remove(min_ind);
			}
			else {
			ngroup.add(group.get(i));
			}
		}
		group.clear();
		group.addAll(ngroup);
	}
	private static void getValue(Node node, Map<Character,Long> ts){
		if(node==null) return;
		if(node.c1==null&&node.c2==null) {
			ts.put(node.getValue(),node.getCount());
		}else {
			getValue(node.getC1(), ts);
			getValue(node.getC2(), ts);
		}
	}
}
