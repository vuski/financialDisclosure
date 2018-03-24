package financialDisclosure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 고위 공직자 관련 건물 재산 주소 처리 파일
 * 법정동으로 표준화하여 구분해내는 것을 목적으로 한다.
 * 고위공직자 원본 데이터에서 건물부분의 주소들만 필터링해서 복사+붙여넣기 한 txt파일을 준비해야 한다.
 * 
 * 구분자 | 와 ;로 결과를 출력한다. 맨 오른쪽 열에 참고가 될 만한 정보들을 적어놓았다.
 * 이 정보들을 토대로 2차 수동 작업을 해야 한다.
 * 원래 입력한 순서 그대로 하나도 빼놓지 않고 출력하므로, 결과 파일을 엑셀에서 원본 데이터 옆에 붙여넣으면 순서 그대로 잘 연결된다.
 * 동 이하 하위 주소 중에 '동' 글자가 있을 경우 잘 분리되지 않는다.
 * @author vuski@github
 *
 */
public class BuildingPropertiesParser {
	
	private static HashMap<String, String[]> addressMap = new HashMap<String, String[]>();   //법정동
	private static ArrayList<String> addressList = new ArrayList<String>();
	
	private static HashMap<String, String[]> hAddressMap = new HashMap<String, String[]>();  //행정동
	
	private static String[] sido = {"서울특별시",
			"부산광역시",
			"대구광역시",
			"인천광역시",
			"광주광역시",
			"대전광역시",
			"울산광역시",
			"경기도",
			"강원도",
			"충청북도",
			"충청남도",
			"전라북도",
			"전라남도",
			"경상북도",
			"경상남도",
			"제주특별자치도",
			"세종특별자치시"
	};

	private static String[][] sidoShorten3 = {
			{"서울시","서울특별시"},
			{"부산시","부산광역시"},
			{"대구시","대구광역시"},
			{"인천시","인천광역시"},
			{"광주시","광주광역시"},
			{"울산시","울산광역시"},
			{"제주도","제주특별자치도"},	
			{"세종시","세종특별자치시"}
	};
	private static String[][] sidoShorten2 = {
			{"세종","세종특별자치시"},
			{"서울","서울특별시"},
			{"부산","부산광역시"},
			{"대구","대구광역시"},
			{"대전","대전광역시"},
			{"인천","인천광역시"},
			{"광주","광주광역시"},
			{"울산","울산광역시"},
			{"제주","제주특별자치도"},
			{"경기","경기도"},
			{"강원","강원도"},
			{"충북","충청북도"},
			{"충남","충청남도"},
			{"전북","전라북도"},
			{"전남","전라남도"},
			{"경북","경상북도"},
			{"경남","경상남도"},
	};
	
	private static String description;

	public static void main(String[] args) throws IOException {
		
		String fileLocation ="D:\\고위공직자 재산\\건물 분석\\";
		String fileName = "건물_2017_sample.txt";
		
		ArrayList<String> source = readFileText(fileLocation+fileName);
		
		ArrayList<String> parsed = new ArrayList<String>();
		
		System.out.print("read 법정동...");
		readAddressList("pnu_code.txt");	
		//System.out.println(addressMap.get("서울특별시"));
		System.out.println("ok!");
		
		System.out.print("read 행정동...");
		readHAddressList("hAddress_code.txt");	
		//System.out.println(addressMap.get("서울특별시"));
		System.out.println("ok!");
		
		//한줄씩 읽으면서 파싱한다.
		for (String line : source) {
			description ="";
			String pnu="";
			
			String parsedText = parser(line); //이 parser 내부에서 연쇄적으로 하부 파싱 작업들을 수행한다.
			
			String address = parsedText.split("\\|")[0];
			pnu = getPnu(address);
			
			parsed.add(parsedText+"|"+pnu+"|"+description);
		}
		
		//기록
		FileOutputStream fos = new FileOutputStream(fileLocation+"parsedAddress_2017.txt");
		OutputStreamWriter osw = new OutputStreamWriter(fos,"euc-kr");
		BufferedWriter bw = new BufferedWriter(osw);
		
		// 기록 순서
		// 원본주소(동까지) | 원본주소(동이하) | 원본주소(면적) | 존재하는 주소로 처리한 법정동이나 행정동 주소 ; 법정동 혹은 행정동 코드 | 참고 정보들
		
		for (String line : parsed) {
			bw.write(line);
			bw.newLine();
		}
		bw.close();
		

	}
	
	/**
	 * primary parser
	 * @param line
	 * @return
	 */
	private static String parser(String line) {
		
		
		line = sidoFixer(line); //시도 이름 적당히 쓴 것 정식으로
		line = basicParser(line); //괄호제거
		line = areaParser(line);  // 주소와 면적을 분리
		//System.out.println(line); 
		String[] temp = line.split("\\|");
		String address = temp[0];		
		String area = "";		
		if (temp.length>1) area = temp[1];
		
		address = emdSpliter(address); //지번주소와 건물 이름을 분리
		
		temp = address.split("\\|");
		if (temp[0].equals("")) {
			address = emdSpliter(temp[1].replaceAll("\\s", "")); //공백제거후 다시 돌리기			
		}
		
		temp = address.split("\\|");
		if (temp[0].equals("")) {			
			address = insufficientEmdSpliter(temp[1]); //주소불충분 집어내기
			//System.out.println(address);
		}
		
		if (temp.length > 1) {
			if (isOverseas(temp[1])) description += "외국주소;";
		}
		
		String result = address +"|"+area;
		return result;
	}
	
	private static void readHAddressList(String fileName) throws IOException {
		
		InputStream fis = BuildingParser.class.getResourceAsStream(fileName);
				//new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis,"euc-kr");
		BufferedReader br = new BufferedReader(isr);
		
		String line;
		
		while ((line=br.readLine()) != null) {
			
			String[] temp = line.split(",");
			//System.out.println(temp[0]+temp[1]+temp[2]);
			String[] value = {temp[1],temp[2]};
			hAddressMap.put(temp[0], value);
			addressList.add(temp[0]);  //레벤스타인 거리용
			
		} //while
		br.close();
		
		
	}
	
	private static void readAddressList(String fileName) throws IOException {
		
		InputStream fis = BuildingParser.class.getResourceAsStream(fileName);
				//new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis,"euc-kr");
		BufferedReader br = new BufferedReader(isr);
		
		String line;
		
		while ((line=br.readLine()) != null) {
			
			String[] temp = line.split(",");
			//System.out.println(temp[0]+temp[1]+temp[2]);
			String[] value = {temp[1],temp[2]};
			addressMap.put(temp[0], value);
			addressList.add(temp[0]); //레벤스타인 거리용
			
		} //while
		br.close();
		
		
	}
	private static String getPnu(String address) {
		
		address = address.replaceAll(" ","");
		String fixedAddr="";
		String pnuCode="";
		if (addressMap.containsKey(address)) {
			fixedAddr = addressMap.get(address)[0];
			pnuCode = addressMap.get(address)[1];			
		} else if (hAddressMap.containsKey(address)){
			fixedAddr = hAddressMap.get(address)[0];
			pnuCode = hAddressMap.get(address)[1];	
			description +="행정동임;";			
		} else { //법정동 행정동에 없으면 레벤스타인 거리를 계산하여 유사한 것을 가져온다.
			
			if (!address.equals("")) {  //비어있으면 그냥 건너뛴다.
				String lastLetter = address.substring(address.length()-1, address.length());
				
				if (lastLetter.equals("로")) {				
					description ="도로명주소일 확률이 큼;";
				} else {
			
					String fixedAddress = getFixedAddr(address);	
					if (addressMap.containsKey(fixedAddress)) {
						fixedAddr = addressMap.get(fixedAddress)[0];
						pnuCode = addressMap.get(fixedAddress)[1];
						description +="법정동에서유사주소얻음;";		
					} else if (hAddressMap.containsKey(address)){
						fixedAddr = hAddressMap.get(address)[0];
						pnuCode = hAddressMap.get(address)[1];	
						description += "행정동에서유사주소얻음;";	
					} else {
						description += "유사주소도 없음";
						//System.out.println("유사주소도 없음 :"+beforeBunji);
					} 
					description +="법정동 및 행정동 주소체계에 없음;";	
				}
			}
		}
		
		return fixedAddr + ";"+ pnuCode;
	}

	private static String getFixedAddr(String addrSource) {
		
		String fixedAddress ="";
		int score = 999;
		int scoreTemp;
		
		for (String addrBank : addressList) {
			
			if (addrBank.length()<=addrSource.length()) {
				scoreTemp = getDistance(addrSource,addrBank);
			} else {
				scoreTemp = getDistance(addrBank,addrSource);
			}
			
			if (scoreTemp<score) {
				
				fixedAddress = addrBank;
				score = scoreTemp;
			}
		}	
		//System.out.println(fixedAddress);
		return fixedAddress;
	}
	


	private static String insufficientEmdSpliter(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;
		
		
		pattern = 
				"([가-힣]+시[\\s]?[가-힣]+구)"
				+"|([가-힣]+시[\\s]?[가-힣]+구)"
				+"|([가-힣]+시[\\s]?[가-힣]+구)"
				+"|([가-힣]+시[\\s]?[가-힣]+군[\\s]?[가-힣]+읍)"
				+"|([가-힣]+시[\\s]?[가-힣]+군[\\s]?[가-힣]+면)"
				+"|([가-힣]+시)"
				+"|([가-힣]+시[\\s]?[가-힣]+면)"
				+"|([가-힣]+시[\\s]?[가-힣]+읍)"
				+"|([가-힣]+도[\\s]?[가-힣]+군[\\s]?[가-힣]+면)"
				+"|([가-힣]+도[\\s]?[가-힣]+군[\\s]?[가-힣]+읍)"
				+"|([가-힣]+도[\\s]?[가-힣]+시)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+면)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+읍)"
				+"|([가-힣]+도[\\s]?[가-힣]+시)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+면)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+읍)";
		
		p = Pattern.compile(pattern);
		m = p.matcher(address);	
		String bldg = "";
		String addr ="";
		if (m.find()) {
			int end;
			bldg += address.substring(0,m.start(0));
			end = m.end();
			addr += address.substring(m.start(0), m.end(0));
			//System.out.println(fixedAddr);
			while (m.find()) {
				bldg += address.substring(end,m.start());
				end = m.end();
				addr += address.substring(m.start(0), m.end(0));
				//System.out.println(fixedAddr);
			}	
			bldg += address.substring(end);
			//System.out.println(fixedAddr);
		} else {
			bldg = address;
		}
		
		if (!addr.equals("")) description += "주소부족하거나 도로명주소;";
		return addr.trim() + "|" +bldg.trim();
		
	}


	private static boolean isOverseas(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;		
		pattern = "[0-9a-zA-Z#,\\.\\s]{8,}"; //영어위주로 된 것이 최소한 8글자 이상
		
		p = Pattern.compile(pattern);
		m = p.matcher(address);	
		String outside = "";
		String addr ="";
		if (m.find()) {
			int end;
			outside += address.substring(0,m.start(0));
			end = m.end();
			addr += address.substring(m.start(0), m.end(0));
			//System.out.println(fixedAddr);
			while (m.find()) {
				outside += address.substring(end,m.start());
				end = m.end();
				addr += address.substring(m.start(0), m.end(0));
				//System.out.println(fixedAddr);
			}	
			outside += address.substring(end);
			//System.out.println(fixedAddr);
		} else {
			outside = address;
		}
		
		if (outside.replaceAll(" ","").equals("")) return true;
		
		return false;
	}


	private static String emdSpliter(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;
		
		
		pattern = 
				"([가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+[\\s]?[0-9]{1,2}가)"
				+"|([가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+(([\\s][0-9]{1,2})|([0-9]{0,2}))동)"
				+"|([가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+로)"
				+"|([가-힣]+시[\\s]?[가-힣]+군[\\s]?[가-힣]+읍[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+시[\\s]?[가-힣]+군[\\s]?[가-힣]+면[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+시[\\s]?[가-힣]+(([\\s][0-9]{1,2})|([0-9]{0,2}))동)"
				+"|([가-힣]+시[\\s]?[가-힣]+면[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+시[\\s]?[가-힣]+읍[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+군[\\s]?[가-힣]+면[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+군[\\s]?[가-힣]+읍[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+[\\s]?[0-9]{1}가)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+[\\s]?[0-9]{1}가)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+(([\\s][0-9]{1,2})|([0-9]{0,2}))동[^읍면리]?)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+면[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+구[\\s]?[가-힣]+읍[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+(([\\s][0-9]{1,2})|([0-9]{0,2}))동[^읍면리]?)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+면[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)"
				+"|([가-힣]+도[\\s]?[가-힣]+시[\\s]?[가-힣]+읍[\\s]?[가-힣]+[\\s]?[0-9]{0,2}리)";
		
		p = Pattern.compile(pattern);
		m = p.matcher(address);	
		String bldg = "";
		String addr ="";
		if (m.find()) {
			int end;
			bldg += address.substring(0,m.start(0));
			end = m.end();
			addr += address.substring(m.start(0), m.end(0));
			//System.out.println(fixedAddr);
			while (m.find()) {
				bldg += address.substring(end,m.start());
				end = m.end();
				addr += address.substring(m.start(0), m.end(0));
				//System.out.println(fixedAddr);
			}	
			bldg += address.substring(end);
			//System.out.println(fixedAddr);
		} else {
			bldg = address;
		}
		
		return addr.trim() + "|" +bldg.trim();
	}

	
	private static String areaParser(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;
		
		pattern = "(대[\\s]?지[\\s]?[0-9,\\.\\s]+㎡([\\s]?중[\\s]?([0-9,\\.\\s]+㎡)?)?)"
				+ "|(건[\\s]?물[\\s]?[0-9,\\.\\s]+㎡([\\s]?중[\\s]?([0-9,\\.\\s]+㎡)?)?)"
				+ "|([0-9,\\.\\s]+㎡([\\s]?중[\\s]?([0-9,\\.\\s]+㎡)?)?)"; 			
		p = Pattern.compile(pattern);
		m = p.matcher(address);	
		String fixedAddr = "";
		String excluded ="";
		if (m.find()) {
			int end;
			fixedAddr += address.substring(0,m.start(0));
			end = m.end();
			excluded += address.substring(m.start(0), m.end(0));
			//System.out.println(fixedAddr);
			while (m.find()) {
				fixedAddr += address.substring(end,m.start());
				end = m.end();
				excluded += address.substring(m.start(0), m.end(0));
				//System.out.println(fixedAddr);
			}	
			fixedAddr += address.substring(end);
			//System.out.println(fixedAddr);
		} else {
			fixedAddr = address;
		}
		
		
		
		fixedAddr = fixedAddr.trim() +"|"+excluded.trim();
		
		return fixedAddr;
		
		
	}


	private static ArrayList<String> readFileText(String fileName) throws IOException {
		
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		
		ArrayList<String> texts = new ArrayList<String>();
		String line;
		
		
		while ((line=br.readLine()) != null) {
			texts.add(line);
			
		} //while
		br.close();
		
		return texts;
	}
	
	private static String basicParser(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;
		
		//공백제거
		
		
		//대괄호, 소괄호 제거
		//pattern = "(\\[[가-힣0-9㎡\\s\\.,]+\\])|(\\([가-힣0-9㎡\\s\\.,]+\\))";
		pattern = "(\\[[^\\[\\]]+\\])|(\\([^\\(\\)]+\\))";
		p = Pattern.compile(pattern);
		m = p.matcher(address);	
		String fixedAddr = "";
		
		if (m.find()) {
			int end;
			fixedAddr += address.substring(0,m.start(0));
			end = m.end();
			//System.out.println(fixedAddr);
			while (m.find()) {
				fixedAddr += address.substring(end,m.start());
				end = m.end();
				//System.out.println(fixedAddr);
			}	
			fixedAddr += address.substring(end);
			//System.out.println(fixedAddr);
		} else {
			fixedAddr = address;
		}
		
		int gwalho1 = fixedAddr.indexOf("(");
		if (gwalho1 !=-1) {
			fixedAddr = fixedAddr.substring(0,gwalho1);	
		}
		
		fixedAddr = fixedAddr.trim();
		
		return fixedAddr;
	}
	
	private static String sidoFixer(String address) {
		
		//시도 단위를 처리한다.
		boolean existStandardSido = false;
		boolean existSido3 = false;
		boolean existSido2 = false;
		
		for (String o:sido) {
			if (address.indexOf(o)!=-1) {
				existStandardSido = true;
				existSido3 = true;
				existSido2 = true;
				break;
			}
		}
		
		if (!existStandardSido) {
			String first = address.substring(0, 3);
			String second = address.substring(3);
			for (String[] o:sidoShorten3) {
				if (first.equals(o[0])) {
					address = o[1]+second;
					existSido3 = true;
					existSido2 = true;
					break;
				}				
			}			
		}
		
		if (!existSido3) {
			String first = address.substring(0, 2);
			String second = address.substring(2);
			for (String[] o:sidoShorten2) {
				if (first.equals(o[0])) {
					address = o[1]+second;
					existSido2 = true;
					break;
				}				
			}
		}
		
		
		return address;
	}
	
	private static int getDistance(String longer, String shorter) { //s1이 긴 문자열
	    int longStrLen = longer.length() + 1; 
	    int shortStrLen = shorter.length() + 1; 
	  
	    // 긴 단어 만큼 크기가 나올 것이므로, 가장 긴단어 에 맞춰 Cost를 계산 
	    int[] cost = new int[longStrLen]; 
	    int[] newcost = new int[longStrLen]; 
	  
	    // 초기 비용을 가장 긴 배열에 맞춰서 초기화 시킨다. 
	    for (int i = 0; i < longStrLen; i++) { 
	        cost[i] = i; 
	    } 
	  
	    // 짧은 배열을 한바퀴 돈다. 
	    for (int j = 1; j < shortStrLen; j++) { 
	        // 초기 Cost는 1, 2, 3, 4... 
	        newcost[0] = j; 
	  
	       // 긴 배열을 한바퀴 돈다. 
	        for (int i = 1; i < longStrLen; i++) { 
	            // 원소가 같으면 0, 아니면 1 
	          int match = 0; 
	          if (longer.charAt(i - 1) != shorter.charAt(j - 1)) { 
	            match = 1; 
	          } 
	           
	            // 대체, 삽입, 삭제의 비용을 계산한다. 
	           int replace = cost[i - 1] + match; 
	            int insert = cost[i] + 1; 
	            int delete = newcost[i - 1] + 1; 
	  
	            // 가장 작은 값을 비용에 넣는다. 
	            newcost[i] = Math.min(Math.min(insert, delete), replace); 
	        } 
	  
	        // 기존 코스트 & 새 코스트 스위칭 
	        int[] temp = cost; 
	        cost = newcost; 
	        newcost = temp; 
	    } 
	  
	    // 가장 마지막값 리턴 
	    return cost[longStrLen - 1]; 
	}

}
