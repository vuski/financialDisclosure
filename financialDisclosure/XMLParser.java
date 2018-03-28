package financialDisclosure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

/**
*대한민국 전자관보에서 공개되는 공직자 재산공개 파일을 txt 데이터 형식으로 변환할 수 있습니다. 국회와 대법원, 지방정부의 재산공개 역시 형식만 비슷하다면 변환 가능합니다.

*먼저 pdf로 된 원본 파일을 xml형식으로 export해야 합니다. 
*아래한글이나 다른 포맷으로 공개된 문서가 있다면 pdf로 1차 변환 후 pdf를 xml로 export해서 사용하면 됩니다.   
*이 코드를 이용해서 고위공직자 재산 xml파일을 데이터로 변환할 수 있습니다.

*셀의 테두리 선들을 확인하면서 눈으로 보는 판단과 유사하도록 인지시켜 변환하는 방식입니다. 
*'인지'라는 고 수준의 단어를 사용하였지만, 결국 if절로 도배가 되어 있는 점 양해바랍니다.

*중간에 에러가 발생하면 코드를 수정해 줄 수도 있지만 워낙 다양한 에러들이 있기 때문에 코드 수정으로 대응하기는 힘이 듭니다. 
*차라리 xml을 excel로 열어서 해당 부분의 테두리처리가 잘못 되어 있으면(사람이 눈으로 직접 봐도 표가 이상하게 되어 있으면) 해당 부분의 테두리나 데이터를 적절히 수정한 후 xml로 그대로 저장하여 다시 이 코드를 실행해보면 웬만한 경우 잘 됩니다.

*애매한 경우가 발생할때는 경고 메시지가 출력되도록 하여 수정에 참고가 되도록 했습니다.

*현재의 공직자재산공개파일변환에 최적화된 코드입니다. 내용을 살펴보면 아시겠지만, 그리 스마트한 코드가 아니니, 더 좋은 코드로 수정해주실 분이 있다면 적극 환영합니다. 

*다른 언어로 포팅할 경우, 다른 사용자들을 위해서 이 문서들과 연결시켜주세요.
 * @author vuski@github
 **/

public class XMLParser {

	public static void main(String[] args) throws IOException {
		
		String fileLocationSource = "D:\\고위공직자 재산\\data\\2017\\"; //xml 파일 경로
		String fileName = "경기도.xml"; //xml 파일명
		
		//파일을 읽어온 후 기본적인 전처리작업을 한다.
		String text = readFileText(fileLocationSource+fileName, "UTF-8").replaceAll("&#10;","");
		text = text.replaceAll(">[\\s]+<","><").replaceAll("\r", "").replaceAll("\n","").replaceAll("     "," ");
		//System.out.println(text);
				
		FileOutputStream fos = new FileOutputStream(fileLocationSource+fileName+"_converted.txt"); //저장할 파일 이름
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		
			
		int index = text.indexOf("<Row");
		String sid ="";
		
		//xml문서 앞 부분의 스타일을 읽어온다.
		HashSet<String> bottomBorders = getStylesBottom(text); 
		HashSet<String> rightBorders = getStylesRight(text);
		//for (String o:bottomBorders) System.out.println(o);	
		
		L2 : while(true) {  //이 while loop는  사람당 한번씩 도는 것
			
			ArrayList<String> cellArray;			
			String row = getRow(text,index); //일단 한 줄을 받아온다. 시작!
			//System.out.println(row);
			
			//기본적 검증. 한 행의 끝은 아닌지, 아무 내용이 없지는 않은지
			if (row.equals("exit!")) { //더 이상 없으면 exit 리턴
				break L2;
			} else if (row.indexOf("continue")!=-1) { //아무내용없이 row가 닫히면
				index = Integer.parseInt(row.split("\\|")[1]);				
				continue L2;
			}
			
			index = index+row.length();
			//System.out.println(text.substring(index, index+4));
			String rowContents = getContents(row).replaceAll(" ","");
			//System.exit(0);;
			int checkA1 = rowContents.indexOf("소속");
			int checkA2 = rowContents.indexOf("직위");
			int checkA3 = rowContents.indexOf("성명");
			
			String[] result1 = new String[4];
			System.out.println(rowContents);
			if (checkA1!=-1 && checkA2!=-1 && checkA3!=-1) { //소속과 직위가 없으면 다음으로 넘어가고 '소속'이 있으면 첫 처리
				result1[0] = rowContents.substring(checkA1+2, checkA2); //소속
				result1[1] = rowContents.substring(checkA2+2, checkA3);  //직위
				result1[2] = rowContents.substring(checkA3+2);			//성명
			} else {
				
				continue L2;
			}
			
			String cell="", contents;			
			
			//System.out.println("여기 "+index);
			row = getRow(text,index); //'천원' 부분이므로 넘긴다
			//System.out.println(text.substring(index, index+4));
			index = index+row.length();
			
			row = getRow(text,index); //본인과의 관계 등등이므로 넘긴다
			//System.out.println(index+row);
			//System.out.println(text.substring(index, index+4));
			String rowContentTemp = getContents(row).replaceAll(" ","");
			boolean existBefore = rowContentTemp.indexOf("종전가액")!=-1? true:false;  ////간혹 신규 진입자의 경우 종전가액이 없는 경우가 있다.
			index = index+row.length();
			
			//System.out.println(index+row);
			row = getRow(text,index); //증가액,감소액이므로 넘긴다
			//System.out.println((row));
			if (row.indexOf("▶")==-1) {  //기존금액없어서 인덱스 행이 적은 신규진입자때문에 있는것임
				index = index+row.length(); 
			}
			//System.out.println(index+row);			
			
			
			L1 : while (true) {  //이 루프는 재산의 종류 하나씩 다루는 것임
				
				row = getRow(text,index);
				if (row.indexOf("continue")!=-1) { //아무내용없이 row가 닫히면
					index = Integer.parseInt(row.split("\\|")[1]);
					//System.out.println(result1[2]+"_"+row);
					continue L1;
				}
				//System.out.println(index+row);				
				//System.out.println(row);
				
				rowContents = getContents(row); 
				checkA1 = rowContents.indexOf("소속");
				checkA2 = rowContents.indexOf("직위");
				checkA3 = rowContents.indexOf("성명");
				
				//System.out.println(rowContents);
				if (checkA1!=-1 && checkA2!=-1 && checkA3!=-1) {
					System.out.println("소속등장 "+index);
					//System.out.println(text.substring(index, index+4));
					break L1; //소속이면 밖으로 나감.다음 사람으로 넘어갔다는 의미임	
					
				} else if (row.replaceAll(" ","").indexOf(">총계<")!=-1 
						||row.replaceAll(" ","").indexOf(">▶총계<")!=-1
						||getContents(row).indexOf("▶총계(소계)")!=-1) { //이것은 태그 없이 점검. 본문 중의 총계와 구분하기 위함
					index = index+row.length();
					//System.out.println("총계등장 "+index);
					break L1; //총계가 등장해도 다음으로 넘어감.
				} else {					
					rowContents = row.replaceAll(" ","").replaceAll("\\(소계\\)","");
					if (rowContents.indexOf("▶")!=-1 && rowContents.indexOf("총계")==-1){  //대법원에서는 ▶총계. 를 사용한다. ex. 2016_05.xml 양승태
						//contents = contents.replaceAll(" ","");
						result1[3]= getContents(getCellArray(row).get(0)).replaceAll("▶","").replaceAll(" ","").replaceAll("\\(소계\\)","");
						//result1[3] = rowContents.substring(rowContents.indexOf("▶")+1, rowContents.indexOf("<",rowContents.indexOf("▶")+1));
						//System.out.println("여기<"+result1[3]+">");
						index = index+row.length();
						continue L1;
					} 
				}
				
				//여기서부터 재산 상세를 읽는다.
				ArrayList<String> category = new ArrayList<String>();
				ArrayList<String> who = new ArrayList<String>();
				ArrayList<String> what = new ArrayList<String>();
				ArrayList<String> detail = new ArrayList<String>();
				ArrayList<String> priceBefore = new ArrayList<String>();
				ArrayList<String> priceRise = new ArrayList<String>();
				ArrayList<String> priceFall = new ArrayList<String>();
				ArrayList<String> priceCurrent = new ArrayList<String>();
				ArrayList<String> reason = new ArrayList<String>();
				String reason_ = "";
				
				String who_ = "";
				String what_ = "";
				String detail_ = "";
				String priceBefore_ = "";
				String priceRise_ = "";
				String priceFall_ = "";
				String priceCurrent_ ="";
				
				int countIndex = 0;
				int sixColumnCount = 0;
				
				//xml문서를 보면 셀들이 merge되어 처리된 부분들이 다수 있다. 몇백페이지의 문서가 시작부터 끝까지 같은 데이터가 동일한 열에 들어있지 않고 좌우로 왔다갔다 하기 때문이다.
				//그럼에도 불구하고 눈으로 보면 무엇이 무엇인지 구분할 수 있다. 그러한 과정을 구현하기 위해, 여기부터 아래의 두 변수를 이용하여 merge들을 세 내려가면서 각각의 변수값들이 제자리를 찾아가도록 한다.
				int[] mergedownCount = {0,0,0,0,0,0,0,0};
				int[] check = {0,0,0,0,0,0,0,0};
				
				L3 : while (true) {   // 저장되는 소단위를 다룬다.
					//여기부터 한 아이템씩 처리
					
					row = getRow(text,index);
					//System.out.println(row);
					
					if (row.indexOf("▶")!=-1&&getContents(row).indexOf("총계")==-1) {
						if (reason.size()<who.size()) { //셀이 소계까지 밀려내려가서 병합되었을 경우 2016_05.xml 의 김종영
							for (int j=0 ; j <sixColumnCount ; j++) {
								reason.add(reason_);
								//System.out.println("reason:"+reason_);
							}
							reason_="";
							sixColumnCount =0;
						}
						break L3;
					} else if (row.replaceAll(" ","").indexOf(">총계<")!=-1 
							||row.replaceAll(" ","").indexOf(">▶총계<")!=-1
							||getContents(row).indexOf("▶총계(소계)")!=-1) { //이것은 태그 없이 점검. 본문 중의 총계와 구분하기 위함 //2013_5.xml 이종석
						//System.out.println("총계등장 "+index);
						break L3; //총계가 등장해도 다음으로 넘어감.
					} else if (getContents(row).indexOf("continue")!=-1) { // 만약 빈 행이면,
						System.out.println("이게 출력되면 문제가 없는지 원본과 대조하여 확인해본다++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						index = Integer.parseInt(row.split("\\|")[1]);
						//System.out.println(text.substring(index, index+15));
						continue L3; //빈 행이 있을 때 끊고갈까말까의 문제로, continue와 break를 적절히 섞어써야 한다. 2013_4.xml 황규철. 여기서는 continue
					}
					
					
					int i=0;
					int mergedowned;
					cellArray = getCellArray(row);
					if (getContents(cellArray.get(0)).equals("") 
							&& getMergedown(cellArray.get(0))!=0
							&& cellArray.size()==9) {  //첫행이 전체적으로 밀려 있으면,
						cellArray.remove(0);
					}
					
					
					//본인 장녀 배우자
					if (mergedownCount[0]==0) {
						cell = cellArray.get(i++);
						
						sid = getSID(cell);
						mergedowned = getMergedown(cell);
						mergedownCount[0] += mergedowned;
						contents = getContents(cell);
						who_ += contents;
						if (bottomBorders.contains(sid)) check[0] = 1; //밑줄이 있으면 끝으로 기록
					
					} else {
						mergedownCount[0]--;
					}
					
					
					//재산의 종류
					if (mergedownCount[1]==0) {
						cell=cellArray.get(i++);
						sid = getSID(cell);
						mergedowned = getMergedown(cell);
						mergedownCount[1] += mergedowned;
						contents = getContents(cell);
						//System.out.println(contents);
						what_ += contents;
						if (bottomBorders.contains(sid)) check[1] = 1; //밑줄이 있으면 끝으로 기록
						
					} else {
						mergedownCount[1]--;
					}
					
					//재산 디테일
					if (mergedownCount[2]==0) {
						cell=cellArray.get(i++);
						//System.out.println(cell);
						sid = getSID(cell);
						mergedowned = getMergedown(cell);
						mergedownCount[2] += mergedowned;
						contents = getContents(cell);
						detail_ += contents;
						
						while (!rightBorders.contains(getSID(cell))) {
							System.out.println("오른쪽 테두리 없다++++++++++++++++아래 출력부분 참고하여 확인해야 한다.+++++++++++++");
							cell=cellArray.get(i++);
							contents = getContents(cell);
							System.out.println("contents:"+contents);
							detail_ += contents;
						}
						//System.out.println("detail:"+detail_);
						if (bottomBorders.contains(sid)) check[2] = 1; //밑줄이 있으면 끝으로 기록
						
					} else {
						mergedownCount[2]--;
					}
					//System.out.println(cell);
					//가액				
					if (existBefore) {  //기존해의 재산공개 내역이 있으면
						
						//종전가액
						if (mergedownCount[3]==0) {
							cell=cellArray.get(i++);
							sid = getSID(cell);
							mergedowned = getMergedown(cell);
							mergedownCount[3] += mergedowned;
							contents = getContents(cell);
							priceBefore_ += contents;
							//System.out.println("priceBefore:"+priceBefore_);
							if (bottomBorders.contains(sid)) check[3] = 1; //밑줄이 있으면 끝으로 기록
							
						} else {
							mergedownCount[3]--;
						}
						
						
						//증가액(실거래액)
						if (mergedownCount[4]==0) {
							cell=cellArray.get(i++);
							sid = getSID(cell);
							mergedowned = getMergedown(cell);
							mergedownCount[4] += mergedowned;
							contents = getContents(cell);
							priceRise_ += contents;
							//System.out.println("priceRise:"+priceRise_);
							if (bottomBorders.contains(sid)) check[4] = 1; //밑줄이 있으면 끝으로 기록
							
						} else {
							mergedownCount[4]--;
						}
						
						//감소액(실거래액)
						if (mergedownCount[5]==0) {
							//System.out.println(cell);
							cell=cellArray.get(i++);
							sid = getSID(cell);
							mergedowned = getMergedown(cell);
							mergedownCount[5] += mergedowned;
							contents = getContents(cell);
							priceFall_ += contents;
							//System.out.println("priceFall:"+priceFall_);
							if (bottomBorders.contains(sid)) check[5] = 1; //밑줄이 있으면 끝으로 기록
							
						} else {
							mergedownCount[5]--;
						}
						
					} else { //간혹 신규 진입자의 경우 종전가액이 없는 경우가 있다.
						//System.out.println("안되는데");
						priceBefore_ = "";
						priceRise_ = "";
						priceFall_ = "";
					}
					
					//현재가액
					if (mergedownCount[6]==0) {
						//System.out.println(cell);
						cell=cellArray.get(i++);
						sid = getSID(cell);
						mergedowned = getMergedown(cell);
						mergedownCount[6] += mergedowned;
						contents = getContents(cell);
						priceCurrent_ += contents;
						//System.out.println("priceCurrent:"+priceCurrent_);
						if (bottomBorders.contains(sid)) check[6] = 1; //밑줄이 있으면 끝으로 기록
						
					} else {
						mergedownCount[6]--;
					}
					
					
					//변동사유
					if (mergedownCount[7]==0) {
						cell="";
						try { //줄이 이상하게 꼬인 경우를 대비함 2016_02.xml 김중현에서 발생
							cell=cellArray.get(i++);
							sid = getSID(cell);
							mergedowned = getMergedown(cell);
							mergedownCount[7] += mergedowned;
							contents = getContents(cell);
							reason_ += contents;
							//System.out.println("reason:"+reason_);
						} catch (Exception e) {
							//System.out.println("줄이 꼬인 에러 발생");
						}
						
						//System.out.println("reason 추가:"+reason_);
						if (bottomBorders.contains(sid)) check[7] = 1; //밑줄이 있으면 끝으로 기록
						
					} else {
						mergedownCount[7]--;
					}
					
					
					
					//이제 각각 끝났는지 체크 
					if (mergedownCount[0]==0 && check[0]==1) { //첫줄이 닫혔으면 나머지는 모두 닫혔을 것이라고 간주한다.
						
						if (priceCurrent_.equals("")&&!detail_.equals("고지거부")&&!detail_.equals("등록제외")) { //이게 비어 있다면 장바꿈 에러로 본다. 따라서 이전 것을 가져옴
							
							//System.out.println(countIndex+"/"+detail_);
							detail.set(countIndex-1, detail.get(countIndex-1)+detail_);
							priceBefore.set(countIndex-1, priceBefore.get(countIndex-1)+priceBefore_);
							priceRise.set(countIndex-1, priceRise.get(countIndex-1)+priceRise_);
							priceFall.set(countIndex-1, priceFall.get(countIndex-1)+priceFall_);
							priceCurrent.set(countIndex-1, priceCurrent.get(countIndex-1)+priceCurrent_);
							
							if (reason.size()<countIndex) { //아직 하나도 없을 때 걸리면 에러남. 2013_a.xml 박성효
									reason_ ="";		
							} else {
								if (reason_.equals("")) reason_ = reason.get(countIndex-1); //전 장의 설명도 가져온다.				
							}
							//System.out.println("여기에 들어와버림");
							
						} else {
							
							category.add(result1[3]);
							
							who.add(who_);
							what.add(what_);
							detail.add(detail_);
							priceBefore.add(priceBefore_);
							priceRise.add(priceRise_);
							priceFall.add(priceFall_);
							priceCurrent.add(priceCurrent_);
							
							sixColumnCount++;
							countIndex++; // '한줄한줄'의 데이터 수를 카운트한다.
						}
						
						who_="";
						what_="";
						detail_="";
						priceBefore_="";
						priceRise_="";
						priceFall_="";
						priceCurrent_="";
					}
					
					if (mergedownCount[7]==0 && check[7]==1) {
						
						//System.out.println("기록한다");
						for (int j=0 ; j <sixColumnCount ; j++) {
							reason.add(reason_);
							//System.out.println("reason:"+reason_);
						}
						reason_="";
						sixColumnCount =0;
						
					}
					
					index = index+row.length();
					//System.out.println(text.substring(index, index+4));
					
				} //L3 재산 하나씩
				
				String reasonTemp ="이게 나오면 안된다.";
				
				for (int j=0 ; j< who.size() ; j++) {
					
					//System.out.println(j);
					bw.write(result1[0]+"|"+result1[1]+"|"+result1[2]+"|"+category.get(j)+"|");
					String priceBeforeTemp = checkNumber(priceBefore.get(j));
					String[] priceRiseTemp =checkNumber2(priceRise.get(j)).split("\\|");
					String[] priceFallTemp =checkNumber2(priceFall.get(j)).split("\\|");
					String priceCurrentTemp = checkNumber(priceCurrent.get(j));
					
/*
					System.out.print(result1[0]+"|"+result1[1]+"|"+result1[2]+"|"+category.get(j)+"|");
					System.out.print(who.get(j)+"|"+what.get(j)+"|"+detail.get(j)
					+"|"+priceBeforeTemp+"|"+priceRiseTemp[0]+"|"+priceRiseTemp[1]+"|"+priceFallTemp[0]+"|"+priceFallTemp[1]
					+priceCurrent.get(j)+"|");
*/				
					
					
					if (category.get(j).equals("채무")) {
						bw.write(who.get(j)+"|"+what.get(j)+"|"+detail.get(j)
						+"|"+(priceBeforeTemp.replaceAll(" ","").equals("")?"":("-"+priceBeforeTemp))
						+"|"+(priceRiseTemp[0].replaceAll(" ","").equals("")?"":("-"+priceRiseTemp[0]))
						+"|"+(priceRiseTemp[1].replaceAll(" ","").equals("")?"":("-"+priceRiseTemp[1]))
						+"|"+(priceFallTemp[0].replaceAll(" ","").equals("")?"":("-"+priceFallTemp[0]))
						+"|"+(priceFallTemp[1].replaceAll(" ","").equals("")?"":("-"+priceFallTemp[1]))
						+"|"+(priceCurrentTemp.replaceAll(" ","").equals("")?"":("-"+priceCurrentTemp))
						+"|");
					} else {
						bw.write(who.get(j)+"|"+what.get(j)+"|"+detail.get(j)
						+"|"+priceBeforeTemp+"|"+priceRiseTemp[0]+"|"+priceRiseTemp[1]+"|"+priceFallTemp[0]+"|"+priceFallTemp[1]+"|"
						+priceCurrentTemp+"|");
					}
					
					
					
					if (reason.get(j).equals("〃")) {
						
					} else {
						reasonTemp = reason.get(j); 
					}
					//System.out.println(reasonTemp);
					bw.write(reasonTemp);
					
					
					bw.newLine();

				}
				
			} //while L1 재산단위
			
			
			
		} //while L2 사람단위
		
		bw.close();
		
		
	

	}
	
	private static String getSID(String cell) {
		
		int iS = cell.indexOf("ss:StyleID=")+12;
		int iE = cell.indexOf("\"", iS);
		
		return cell.substring(iS,iE);
	}

	private static HashSet<String> getStylesBottom(String text) {
		
		HashSet<String> bottomBorders = new HashSet<String>();
		
		int index = text.indexOf("<Style ss:");
		String criteria = "Border ss:Position=\"Bottom\"";// ss:Color=\"#231F20\"";  //2011년은 231f20
		
		while (true) {
			String style = getStyleRow(text, index);
			if (style.equals("exit!")) break;
			//System.out.println(style);
			index = index + style.length();
			
			int iS = style.indexOf("ss:ID")+7;
			int iE = style.indexOf("\"", iS);
					
			String id = style.substring(iS, iE);
			if (style.indexOf(criteria)!=-1) bottomBorders.add(id);			
		}	
		
		return bottomBorders;
	}
	
	private static HashSet<String> getStylesRight(String text) {
		
		HashSet<String> rightBorders = new HashSet<String>();
		
		int index = text.indexOf("<Style ss:");
		String criteria = "Border ss:Position=\"Right\"";//ss:Color=\"#231F20\"";
		
		while (true) {
			String style = getStyleRow(text, index);
			if (style.equals("exit!")) break;
			//System.out.println(style);
			index = index + style.length();
			
			int iS = style.indexOf("ss:ID")+7;
			int iE = style.indexOf("\"", iS);
					
			String id = style.substring(iS, iE);
			if (style.indexOf(criteria)!=-1) rightBorders.add(id);			
		}	
		
		return rightBorders;
	}

	private static String getStyleRow(String text, int index) {
		
		int indexS = text.indexOf("<Style", index);
		if (indexS==-1) return "exit!";
		
		int indexE = text.indexOf("</Style>", indexS);
		
		String row = text.substring(indexS, indexE+8); //</row>까지 포함. 뒤의 연산을 위해.	
		
		return row;
		
	}

	private static int getMergedown(String cell) {
		
		int check1 = cell.indexOf("MergeDown");
		if (check1==-1) return 0;
		String temp = cell.substring(check1+11,cell.indexOf("\"",check1+11));		
		return Integer.parseInt(temp);
	}
	
	private static int getMergeAcross(String cell) {
		
		int check1 = cell.indexOf("MergeAcross");
		if (check1==-1) return 0;
		String temp = cell.substring(check1+13,cell.indexOf("\"",check1+13));		
		return Integer.parseInt(temp);
	}

	private static String checkNumber2(String contents) {
		
		contents = contents.replaceAll(",", "");
		contents = contents.replaceAll(" ", "");
		if (contents.replaceAll("-","").equals("")) contents = contents.replaceAll("-", "0");
		
		int i = contents.indexOf("(");
		
		if (i==-1) {
			return contents+"| ";
		} else {
			contents = contents.substring(0, contents.length()-1);
			return contents.split("\\(")[0]+"|"+contents.split("\\(")[1];
		}
		
	}

	private static String checkNumber(String contents) {
		
		contents = contents.replaceAll(",", "");
		
		if (contents.equals("-")) return "0";
		
		return contents;
		
	}

	private static String getContents(String cell) {
		
		cell = cell.replaceAll("<[^>]*>", ""); //태그제거
		return cell;
				
		
	}
	
	private static String getContents_(String cell) {
		
		int check4,check5;
		cell = cell.replaceAll("><", "");
		check4 = cell.indexOf(">");
		check5 = cell.indexOf("<",1);
		
		if (check4 == cell.length()-1) {
			return "";
		} else {
			return cell.substring(check4+1,check5).replaceAll("  "," ");					
		}			
		
	}

	private static String getRow(String text, int index) {
		
		int check1,check2;
		
		int indexS = text.indexOf("<Row",index);		
		if (indexS==-1) return "exit!";
		//System.out.println(text.substring(indexS,indexS+4));
		
		check1 = text.indexOf("<",indexS+1);
		check2 = text.indexOf("/>",indexS+1);
		if (check2!=-1 && check2<check1) {
			index = check2+2;
			return "continue|"+index; //아무 내용없이 row가 닫히면
		}
		
		int indexE = text.indexOf("</Row>", indexS+1);
		
		String row = text.substring(indexS, indexE+6); //</row>까지 포함. 뒤의 연산을 위해.	
		if (row.replaceAll("<[^>]*>", "").equals("")) return "continue|"+(indexS+row.length());
		//System.out.println("____"+ row);
		return row;
	}

	private static ArrayList<String> getCellArray(String row) {
		
		int check3,check4,check5,check6,check7;
		int indexSub = 0;
		
		check3 = row.indexOf(">")+1;
		String cells = row.substring(check3); // cell 태그들만 남기기
		ArrayList<String> cellArray = new ArrayList<String>();
		
		L1: while(true) {
			//System.out.println(indexSub);
			check4 = cells.indexOf("<Cell",indexSub);
			if (check4==-1) break L1;
			
			check5 = cells.indexOf("/>",indexSub);
			check6 = cells.indexOf("<", indexSub+1);
			check7 = cells.indexOf("</Cell>", indexSub);
			if (check5!=-1 && check5<check6) {
				cellArray.add(cells.substring(check4, check5+2));
				//System.out.println(cells.substring(check4, check5+2));
				indexSub = check5+2;
				continue L1;
			} else {
				cellArray.add(cells.substring(check4, check7+7));
				//System.out.println(cells.substring(check4, check7+7));
				indexSub = check7+7;
				continue L1;	
			}
			
		}
		return cellArray;
	}

	private static String readFileText(String fileName, String charset) throws IOException {
		
		FileInputStream fis = new FileInputStream(fileName);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		BufferedReader br = new BufferedReader(isr);
		
		String line;
		StringBuffer sb = new StringBuffer();
		
		while ((line=br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		} //while
		br.close();
		
		return sb.toString();
	}
	

}
