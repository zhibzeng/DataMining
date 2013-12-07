package com.swjtu.apriori;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class GetDataFromExcel {
	public static List<String> data = new ArrayList<String>();
	public static List<String> getData() throws BiffException, IOException{
		File f=new File("F:"+File.separator+"课内文件"+File.separator+"数据挖掘"
				+File.separator+"apriori"+File.separator+"1000"+File.separator+"1000-out1.xls"); 
		Workbook book=Workbook.getWorkbook(f);//  
        Sheet sheet=book.getSheet(0);   //获得第一个工作表对象  
        for(int i=0;i<sheet.getRows();i++){  
        	String s = new String();
        	for(int j=1;j<9;j++){
        		Cell cell=sheet.getCell(j, i);  
        		if(!"".equals(cell.getContents().trim())){
        			s = s+cell.getContents().trim()+" ";
        		}
        	}
        	s = s.substring(0,s.lastIndexOf(" "));
        	data.add(s);
        }
		return data;
	}

}
