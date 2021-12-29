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
	 * 노드 목록을 읽어보고,해당 정보 파싱. 살아있는 노드 IP 도출
	 */
	public static void nodeList() {
		
		try {
			ProcessBuilder pb = new ProcessBuilder("docker","node","ls","--format","{{.ID}}\t{{.Status}}\t{{.Availability}}\t{{.ManagerStatus}}");

			Process proc = pb.start();
			//노드 리스트 갯수 초기화
			int IDCnt = 0;
			int statusCnt = 0;
			int availabilityCnt = 0;
			int managerStatusCnt = 0;
			String liveNode = null;
			
			//노드 별 정보 파싱
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			while(true) 
			{
				String line=in.readLine();
				if (line == null) break;
				
				//노드 정보 배열에 담기
				String[] ndLst = line.split("\t");
				System.out.println("node Info:"+ndLst[0]+","+ndLst[1]+","+ndLst[2]+","+ndLst[3]);
				
				if(ndLst.length != 4) {
					System.out.println("line parse error"+" line="+line);
					System.out.println(ndLst.length);
				}
				//정보 변수에 파싱
				String nodeID = ndLst[0];
				String nodeStatus = ndLst[1];
				String nodeAvailability = ndLst[2];
				String nodeManagerStatus = ndLst[3];
				
				
				String Ready = "Ready";
				String Active = "Active";
				String Leader = "Leader";
				String Reachable = "Reachable";
				
				//노드 갯수 카운트 및 상태 카운트
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
				
				//살아있는 노드 이름 알려주기
				if(nodeID!=null&&nodeStatus.equals(Ready)
						&&nodeAvailability.equals(Active)
						&&nodeManagerStatus.equals(Leader)) {
					liveNode = nodeID;
				}
			}
			
//			검색되는 노드가 2개 이상인데 STATUS = Ready 결과가 1일 때, AVAILABILITY = Active 결과가 1일 때, AVAILABILITY = Active 결과가 1일 때
			System.out.println("live node: "+ liveNode);
			if(IDCnt>1 && (statusCnt<2||availabilityCnt<2||managerStatusCnt<2)) {
				//스웜 init 메소드 호출
				swarmInit(liveNode);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void swarmInit(String liveNode) {
		//노드의 IP 가져오기
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
		
		
		
		//새로운 스웜 클러스터 생성.
		ProcessBuilder pb2 = new ProcessBuilder("docker", "swarm", "init", "--force-new-cluster", "--advertise-addr",IPaddr);
		
		try {
			pb2.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//노드 ID 목록 가져오기
		ProcessBuilder pb3 = new ProcessBuilder("docker","node","ls","--format","{{.ID}}");
		
		try {
			Process proc = pb3.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			while(true) 
			{
				String NodeId=in.readLine();
				if (NodeId == null) break;
				//살아있는 nodeID 가 아닐 경우 rm처리
				if((NodeId==liveNode)==false) {
					ProcessBuilder pb4 = new ProcessBuilder("docker", "node", "rm", NodeId);
					pb4.start();

				}
			}
				
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
//		여기부터는 매뉴얼대로 진행
		//죽었다 살아난 노드에 실행
		//노드가 살아났나 죽었나는 ping을 날려봐야 하나?
		//죽었던 노드가 살아나면 있던 스웜 클러스터를 떠난다. docker swarm leave --force
		//init 명령어로 클러스터 초기화 한 노드에서 매니저 토큰을 얻어낸다. docker swarm join-token manager
		//얻어낸 토큰 값을 죽었다 살아난 노드에 입력한다
		//매니저로 등록된다 -> 매니저 노드 몇개인지 출력하는 로그.

		
		
		
		
	}
	

}
