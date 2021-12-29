package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
		nodeList();
	}
	
	/*nodeList*
	 * ��� ����� �о��,�ش� ���� �Ľ�. ����ִ� ��� IP ����
	 */
	public static void nodeList() {
		
		try {
			ProcessBuilder pb = new ProcessBuilder("docker","node","ls","--format","{{.ID}}\t{{.Status}}\t{{.Availability}}\t{{.ManagerStatus}}");

			Process proc = pb.start();
			//��� ����Ʈ ���� �ʱ�ȭ
			int IDCnt = 0;
			int statusCnt = 0;
			int availabilityCnt = 0;
			int managerStatusCnt = 0;
			String liveNode = null;
			
			//��� �� ���� �Ľ�
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			while(true) 
			{
				String line=in.readLine();
				if (line == null) break;
				
				//��� ���� �迭�� ���
				String[] ndLst = line.split("\t");
				System.out.println("node Info:"+ndLst[0]+","+ndLst[1]+","+ndLst[2]+","+ndLst[3]);
				
				if(ndLst.length != 4) {
					System.out.println("line parse error"+" line="+line);
					System.out.println(ndLst.length);
				}
				//���� ������ �Ľ�
				String nodeID = ndLst[0];
				String nodeStatus = ndLst[1];
				String nodeAvailability = ndLst[2];
				String nodeManagerStatus = ndLst[3];
				
				
				String Ready = "Ready";
				String Active = "Active";
				String Leader = "Leader";
				String Reachable = "Reachable";
				
				//��� ���� ī��Ʈ �� ���� ī��Ʈ
				if(nodeID!=null) {
					IDCnt++;
				}
				if(nodeStatus.equals(Ready)) {
					statusCnt++;
				}
				if(nodeAvailability.equals(Active)) {
					availabilityCnt++;
				}
				if(nodeManagerStatus.equals(Leader)||nodeManagerStatus.equals(Reachable)) {
					managerStatusCnt++;
				}
				
				//����ִ� ��� �̸� �˷��ֱ�
				if(nodeID!=null&&nodeStatus.equals(Ready)
						&&nodeAvailability.equals(Active)
						&&nodeManagerStatus.equals(Leader)) {
					liveNode = nodeID;
				}
			}
			
//			�˻��Ǵ� ��尡 2�� �̻��ε� STATUS = Ready ����� 1�� ��, AVAILABILITY = Active ����� 1�� ��, AVAILABILITY = Active ����� 1�� ��
			System.out.println("live node: "+ liveNode);
			if(IDCnt>1 && (statusCnt<2||availabilityCnt<2||managerStatusCnt<2)) {
				//���� init �޼ҵ� ȣ��
				swarmInit(liveNode);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void swarmInit(String liveNode) {
		//����� IP ��������
		ProcessBuilder pb = new ProcessBuilder("docker","node","inspect",liveNode,"--format", "{{.Status.Addr}}");

		String IPaddr = null;
		
		try {
			Process proc = pb.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			IPaddr=in.readLine();
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println("IP Address :"+IPaddr);
		
		
		
		//���ο� ���� Ŭ������ ����.
		ProcessBuilder pb2 = new ProcessBuilder("docker", "swarm", "init", "--force-new-cluster", "--advertise-addr",IPaddr);
		
		try {
			pb2.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//��� ID ��� ��������
		ProcessBuilder pb3 = new ProcessBuilder("docker","node","ls","--format","{{.ID}}");
		
		try {
			Process proc = pb3.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			while(true) 
			{
				String NodeId=in.readLine();
				if (NodeId == null) break;
				//����ִ� nodeID �� �ƴ� ��� rmó��
				if((NodeId==liveNode)==false) {
					ProcessBuilder pb4 = new ProcessBuilder("docker", "node", "rm", NodeId);
					pb4.start();

				}
			}
				
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
//		������ʹ� �Ŵ����� ����
		//�׾��� ��Ƴ� ��忡 ����
		//��尡 ��Ƴ��� �׾����� ping�� �������� �ϳ�?
		//�׾��� ��尡 ��Ƴ��� �ִ� ���� Ŭ�����͸� ������. docker swarm leave --force
		//init ��ɾ�� Ŭ������ �ʱ�ȭ �� ��忡�� �Ŵ��� ��ū�� ����. docker swarm join-token manager
		//�� ��ū ���� �׾��� ��Ƴ� ��忡 �Է��Ѵ�
		//�Ŵ����� ��ϵȴ� -> �Ŵ��� ��� ����� ����ϴ� �α�.

		
		
		
		
	}
	

}
