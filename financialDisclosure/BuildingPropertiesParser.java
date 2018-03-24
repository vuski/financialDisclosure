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
 * °íÀ§ °øÁ÷ÀÚ °ü·Ã °Ç¹° Àç»ê ÁÖ¼Ò Ã³¸® ÆÄÀÏ
 * ¹ıÁ¤µ¿À¸·Î Ç¥ÁØÈ­ÇÏ¿© ±¸ºĞÇØ³»´Â °ÍÀ» ¸ñÀûÀ¸·Î ÇÑ´Ù.
 * °íÀ§°øÁ÷ÀÚ ¿øº» µ¥ÀÌÅÍ¿¡¼­ °Ç¹°ºÎºĞÀÇ ÁÖ¼Òµé¸¸ ÇÊÅÍ¸µÇØ¼­ º¹»ç+ºÙ¿©³Ö±â ÇÑ txtÆÄÀÏÀ» ÁØºñÇØ¾ß ÇÑ´Ù.
 * 
 * ±¸ºĞÀÚ | ¿Í ;·Î °á°ú¸¦ Ãâ·ÂÇÑ´Ù. ¸Ç ¿À¸¥ÂÊ ¿­¿¡ Âü°í°¡ µÉ ¸¸ÇÑ Á¤º¸µéÀ» Àû¾î³õ¾Ò´Ù.
 * ÀÌ Á¤º¸µéÀ» Åä´ë·Î 2Â÷ ¼öµ¿ ÀÛ¾÷À» ÇØ¾ß ÇÑ´Ù.
 * ¿ø·¡ ÀÔ·ÂÇÑ ¼ø¼­ ±×´ë·Î ÇÏ³ªµµ »©³õÁö ¾Ê°í Ãâ·ÂÇÏ¹Ç·Î, °á°ú ÆÄÀÏÀ» ¿¢¼¿¿¡¼­ ¿øº» µ¥ÀÌÅÍ ¿·¿¡ ºÙ¿©³ÖÀ¸¸é ¼ø¼­ ±×´ë·Î Àß ¿¬°áµÈ´Ù.
 * µ¿ ÀÌÇÏ ÇÏÀ§ ÁÖ¼Ò Áß¿¡ 'µ¿' ±ÛÀÚ°¡ ÀÖÀ» °æ¿ì Àß ºĞ¸®µÇÁö ¾Ê´Â´Ù.
 * @author vuski@github
 *
 */
public class BuildingPropertiesParser {
	
	private static HashMap<String, String[]> addressMap = new HashMap<String, String[]>();   //¹ıÁ¤µ¿
	private static ArrayList<String> addressList = new ArrayList<String>();
	
	private static HashMap<String, String[]> hAddressMap = new HashMap<String, String[]>();  //ÇàÁ¤µ¿
	
	private static String[] sido = {"¼­¿ïÆ¯º°½Ã",
			"ºÎ»ê±¤¿ª½Ã",
			"´ë±¸±¤¿ª½Ã",
			"ÀÎÃµ±¤¿ª½Ã",
			"±¤ÁÖ±¤¿ª½Ã",
			"´ëÀü±¤¿ª½Ã",
			"¿ï»ê±¤¿ª½Ã",
			"°æ±âµµ",
			"°­¿øµµ",
			"ÃæÃ»ºÏµµ",
			"ÃæÃ»³²µµ",
			"Àü¶óºÏµµ",
			"Àü¶ó³²µµ",
			"°æ»óºÏµµ",
			"°æ»ó³²µµ",
			"Á¦ÁÖÆ¯º°ÀÚÄ¡µµ",
			"¼¼Á¾Æ¯º°ÀÚÄ¡½Ã"
	};

	private static String[][] sidoShorten3 = {
			{"¼­¿ï½Ã","¼­¿ïÆ¯º°½Ã"},
			{"ºÎ»ê½Ã","ºÎ»ê±¤¿ª½Ã"},
			{"´ë±¸½Ã","´ë±¸±¤¿ª½Ã"},
			{"ÀÎÃµ½Ã","ÀÎÃµ±¤¿ª½Ã"},
			{"±¤ÁÖ½Ã","±¤ÁÖ±¤¿ª½Ã"},
			{"¿ï»ê½Ã","¿ï»ê±¤¿ª½Ã"},
			{"Á¦ÁÖµµ","Á¦ÁÖÆ¯º°ÀÚÄ¡µµ"},	
			{"¼¼Á¾½Ã","¼¼Á¾Æ¯º°ÀÚÄ¡½Ã"}
	};
	private static String[][] sidoShorten2 = {
			{"¼¼Á¾","¼¼Á¾Æ¯º°ÀÚÄ¡½Ã"},
			{"¼­¿ï","¼­¿ïÆ¯º°½Ã"},
			{"ºÎ»ê","ºÎ»ê±¤¿ª½Ã"},
			{"´ë±¸","´ë±¸±¤¿ª½Ã"},
			{"´ëÀü","´ëÀü±¤¿ª½Ã"},
			{"ÀÎÃµ","ÀÎÃµ±¤¿ª½Ã"},
			{"±¤ÁÖ","±¤ÁÖ±¤¿ª½Ã"},
			{"¿ï»ê","¿ï»ê±¤¿ª½Ã"},
			{"Á¦ÁÖ","Á¦ÁÖÆ¯º°ÀÚÄ¡µµ"},
			{"°æ±â","°æ±âµµ"},
			{"°­¿ø","°­¿øµµ"},
			{"ÃæºÏ","ÃæÃ»ºÏµµ"},
			{"Ãæ³²","ÃæÃ»³²µµ"},
			{"ÀüºÏ","Àü¶óºÏµµ"},
			{"Àü³²","Àü¶ó³²µµ"},
			{"°æºÏ","°æ»óºÏµµ"},
			{"°æ³²","°æ»ó³²µµ"},
	};
	
	private static String description;

	public static void main(String[] args) throws IOException {
		
		String fileLocation ="D:\\°íÀ§°øÁ÷ÀÚ Àç»ê\\°Ç¹° ºĞ¼®\\";
		String fileName = "°Ç¹°_2017_sample.txt";
		
		ArrayList<String> source = readFileText(fileLocation+fileName);
		
		ArrayList<String> parsed = new ArrayList<String>();
		
		System.out.print("read ¹ıÁ¤µ¿...");
		readAddressList("pnu_code.txt");	
		//System.out.println(addressMap.get("¼­¿ïÆ¯º°½Ã"));
		System.out.println("ok!");
		
		System.out.print("read ÇàÁ¤µ¿...");
		readHAddressList("hAddress_code.txt");	
		//System.out.println(addressMap.get("¼­¿ïÆ¯º°½Ã"));
		System.out.println("ok!");
		
		//ÇÑÁÙ¾¿ ÀĞÀ¸¸é¼­ ÆÄ½ÌÇÑ´Ù.
		for (String line : source) {
			description ="";
			String pnu="";
			
			String parsedText = parser(line); //ÀÌ parser ³»ºÎ¿¡¼­ ¿¬¼âÀûÀ¸·Î ÇÏºÎ ÆÄ½Ì ÀÛ¾÷µéÀ» ¼öÇàÇÑ´Ù.
			
			String address = parsedText.split("\\|")[0];
			pnu = getPnu(address);
			
			parsed.add(parsedText+"|"+pnu+"|"+description);
		}
		
		//±â·Ï
		FileOutputStream fos = new FileOutputStream(fileLocation+"parsedAddress_2017.txt");
		OutputStreamWriter osw = new OutputStreamWriter(fos,"euc-kr");
		BufferedWriter bw = new BufferedWriter(osw);
		
		// ±â·Ï ¼ø¼­
		// ¿øº»ÁÖ¼Ò(µ¿±îÁö) | ¿øº»ÁÖ¼Ò(µ¿ÀÌÇÏ) | ¿øº»ÁÖ¼Ò(¸éÀû) | Á¸ÀçÇÏ´Â ÁÖ¼Ò·Î Ã³¸®ÇÑ ¹ıÁ¤µ¿ÀÌ³ª ÇàÁ¤µ¿ ÁÖ¼Ò ; ¹ıÁ¤µ¿ È¤Àº ÇàÁ¤µ¿ ÄÚµå | Âü°í Á¤º¸µé
		
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
		
		
		line = sidoFixer(line); //½Ãµµ ÀÌ¸§ Àû´çÈ÷ ¾´ °Í Á¤½ÄÀ¸·Î
		line = basicParser(line); //°ıÈ£Á¦°Å
		line = areaParser(line);  // ÁÖ¼Ò¿Í ¸éÀûÀ» ºĞ¸®
		//System.out.println(line); 
		String[] temp = line.split("\\|");
		String address = temp[0];		
		String area = "";		
		if (temp.length>1) area = temp[1];
		
		address = emdSpliter(address); //Áö¹øÁÖ¼Ò¿Í °Ç¹° ÀÌ¸§À» ºĞ¸®
		
		temp = address.split("\\|");
		if (temp[0].equals("")) {
			address = emdSpliter(temp[1].replaceAll("\\s", "")); //°ø¹éÁ¦°ÅÈÄ ´Ù½Ã µ¹¸®±â			
		}
		
		temp = address.split("\\|");
		if (temp[0].equals("")) {			
			address = insufficientEmdSpliter(temp[1]); //ÁÖ¼ÒºÒÃæºĞ Áı¾î³»±â
			//System.out.println(address);
		}
		
		if (temp.length > 1) {
			if (isOverseas(temp[1])) description += "¿Ü±¹ÁÖ¼Ò;";
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
			addressList.add(temp[0]);  //·¹º¥½ºÅ¸ÀÎ °Å¸®¿ë
			
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
			addressList.add(temp[0]); //·¹º¥½ºÅ¸ÀÎ °Å¸®¿ë
			
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
			description +="ÇàÁ¤µ¿ÀÓ;";			
		} else { //¹ıÁ¤µ¿ ÇàÁ¤µ¿¿¡ ¾øÀ¸¸é ·¹º¥½ºÅ¸ÀÎ °Å¸®¸¦ °è»êÇÏ¿© À¯»çÇÑ °ÍÀ» °¡Á®¿Â´Ù.
			
			if (!address.equals("")) {  //ºñ¾îÀÖÀ¸¸é ±×³É °Ç³Ê¶Ú´Ù.
				String lastLetter = address.substring(address.length()-1, address.length());
				
				if (lastLetter.equals("·Î")) {				
					description ="µµ·Î¸íÁÖ¼ÒÀÏ È®·üÀÌ Å­;";
				} else {
			
					String fixedAddress = getFixedAddr(address);	
					if (addressMap.containsKey(fixedAddress)) {
						fixedAddr = addressMap.get(fixedAddress)[0];
						pnuCode = addressMap.get(fixedAddress)[1];
						description +="¹ıÁ¤µ¿¿¡¼­À¯»çÁÖ¼Ò¾òÀ½;";		
					} else if (hAddressMap.containsKey(address)){
						fixedAddr = hAddressMap.get(address)[0];
						pnuCode = hAddressMap.get(address)[1];	
						description += "ÇàÁ¤µ¿¿¡¼­À¯»çÁÖ¼Ò¾òÀ½;";	
					} else {
						description += "À¯»çÁÖ¼Òµµ ¾øÀ½";
						//System.out.println("À¯»çÁÖ¼Òµµ ¾øÀ½ :"+beforeBunji);
					} 
					description +="¹ıÁ¤µ¿ ¹× ÇàÁ¤µ¿ ÁÖ¼ÒÃ¼°è¿¡ ¾øÀ½;";	
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
				"([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+À¾)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+¸é)"
				+"|([°¡-ÆR]+½Ã)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+¸é)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+À¾)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+¸é)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+À¾)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+¸é)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+À¾)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+¸é)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+À¾)";
		
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
		
		if (!addr.equals("")) description += "ÁÖ¼ÒºÎÁ·ÇÏ°Å³ª µµ·Î¸íÁÖ¼Ò;";
		return addr.trim() + "|" +bldg.trim();
		
	}


	private static boolean isOverseas(String address) {
		
		String pattern;
		Pattern p;
		Matcher m;		
		pattern = "[0-9a-zA-Z#,\\.\\s]{8,}"; //¿µ¾îÀ§ÁÖ·Î µÈ °ÍÀÌ ÃÖ¼ÒÇÑ 8±ÛÀÚ ÀÌ»ó
		
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
				"([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+[\\s]?[0-9]{1,2}°¡)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+(([\\s][0-9]{1,2})|([0-9]{0,2}))µ¿)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+·Î)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+À¾[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+¸é[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+(([\\s][0-9]{1,2})|([0-9]{0,2}))µ¿)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+¸é[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+À¾[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+¸é[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+±º[\\s]?[°¡-ÆR]+À¾[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+[\\s]?[0-9]{1}°¡)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+[\\s]?[0-9]{1}°¡)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+(([\\s][0-9]{1,2})|([0-9]{0,2}))µ¿[^À¾¸é¸®]?)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+¸é[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+±¸[\\s]?[°¡-ÆR]+À¾[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+(([\\s][0-9]{1,2})|([0-9]{0,2}))µ¿[^À¾¸é¸®]?)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+¸é[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)"
				+"|([°¡-ÆR]+µµ[\\s]?[°¡-ÆR]+½Ã[\\s]?[°¡-ÆR]+À¾[\\s]?[°¡-ÆR]+[\\s]?[0-9]{0,2}¸®)";
		
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
		
		pattern = "(´ë[\\s]?Áö[\\s]?[0-9,\\.\\s]+§³([\\s]?Áß[\\s]?([0-9,\\.\\s]+§³)?)?)"
				+ "|(°Ç[\\s]?¹°[\\s]?[0-9,\\.\\s]+§³([\\s]?Áß[\\s]?([0-9,\\.\\s]+§³)?)?)"
				+ "|([0-9,\\.\\s]+§³([\\s]?Áß[\\s]?([0-9,\\.\\s]+§³)?)?)"; 			
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
		
		//°ø¹éÁ¦°Å
		
		
		//´ë°ıÈ£, ¼Ò°ıÈ£ Á¦°Å
		//pattern = "(\\[[°¡-ÆR0-9§³\\s\\.,]+\\])|(\\([°¡-ÆR0-9§³\\s\\.,]+\\))";
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
		
		//½Ãµµ ´ÜÀ§¸¦ Ã³¸®ÇÑ´Ù.
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
	
	private static int getDistance(String longer, String shorter) { //s1ÀÌ ±ä ¹®ÀÚ¿­
	    int longStrLen = longer.length() + 1; 
	    int shortStrLen = shorter.length() + 1; 
	  
	    // ±ä ´Ü¾î ¸¸Å­ Å©±â°¡ ³ª¿Ã °ÍÀÌ¹Ç·Î, °¡Àå ±ä´Ü¾î ¿¡ ¸ÂÃç Cost¸¦ °è»ê 
	    int[] cost = new int[longStrLen]; 
	    int[] newcost = new int[longStrLen]; 
	  
	    // ÃÊ±â ºñ¿ëÀ» °¡Àå ±ä ¹è¿­¿¡ ¸ÂÃç¼­ ÃÊ±âÈ­ ½ÃÅ²´Ù. 
	    for (int i = 0; i < longStrLen; i++) { 
	        cost[i] = i; 
	    } 
	  
	    // ÂªÀº ¹è¿­À» ÇÑ¹ÙÄû µ·´Ù. 
	    for (int j = 1; j < shortStrLen; j++) { 
	        // ÃÊ±â Cost´Â 1, 2, 3, 4... 
	        newcost[0] = j; 
	  
	       // ±ä ¹è¿­À» ÇÑ¹ÙÄû µ·´Ù. 
	        for (int i = 1; i < longStrLen; i++) { 
	            // ¿ø¼Ò°¡ °°À¸¸é 0, ¾Æ´Ï¸é 1 
	          int match = 0; 
	          if (longer.charAt(i - 1) != shorter.charAt(j - 1)) { 
	            match = 1; 
	          } 
	           
	            // ´ëÃ¼, »ğÀÔ, »èÁ¦ÀÇ ºñ¿ëÀ» °è»êÇÑ´Ù. 
	           int replace = cost[i - 1] + match; 
	            int insert = cost[i] + 1; 
	            int delete = newcost[i - 1] + 1; 
	  
	            // °¡Àå ÀÛÀº °ªÀ» ºñ¿ë¿¡ ³Ö´Â´Ù. 
	            newcost[i] = Math.min(Math.min(insert, delete), replace); 
	        } 
	  
	        // ±âÁ¸ ÄÚ½ºÆ® & »õ ÄÚ½ºÆ® ½ºÀ§Äª 
	        int[] temp = cost; 
	        cost = newcost; 
	        newcost = temp; 
	    } 
	  
	    // °¡Àå ¸¶Áö¸·°ª ¸®ÅÏ 
	    return cost[longStrLen - 1]; 
	}

}
