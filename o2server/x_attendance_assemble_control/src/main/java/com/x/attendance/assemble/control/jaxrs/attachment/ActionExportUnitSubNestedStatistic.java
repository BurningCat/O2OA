package com.x.attendance.assemble.control.jaxrs.attachment;

import com.x.attendance.entity.StatisticPersonForMonth;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoFile;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.tools.ListTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ActionExportUnitSubNestedStatistic extends BaseAction {
	
	private static  Logger logger = LoggerFactory.getLogger(ActionExportUnitSubNestedStatistic.class);
	
	protected ActionResult<Wo> execute( HttpServletRequest request, EffectivePerson effectivePerson, String name, String year, String month ,Boolean stream ) throws Exception {
			ActionResult<Wo> result = new ActionResult<>();
			List<String> ids = null;
			List<String> unitNameList = new ArrayList<String>();
			List<String> unUnitNameList = new ArrayList<String>();
			List<String> personNameList = new ArrayList<String>();
			List<StatisticPersonForMonth> statisticPersonForMonth_list = null;
			Workbook wb = null;
			Wo wo = null;
			String fileName = null;
			String sheetName = null;
			Boolean check = true;

			if ("(0)".equals(year)) {
				year = null;
			}
			if ("(0)".equals(month)) {
				month = null;
			}
			if( check ){
				if( name == null || name.isEmpty() ){
					check = false;
					Exception exception = new ExceptionQueryStatisticUnitNameEmpty();
					result.error( exception );
				}
			}
			if( check ){
				try {
					unitNameList = userManagerService.listSubUnitNameWithParent( name );
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceStatisticProcess( e, "???????????????????????????????????????????????????????????????Unit:" + name );
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
			if( check ){
				if( unitNameList == null ){
					unitNameList = new ArrayList<>();
				}
				unitNameList.add( name );
				unUnitNameList = getUnUnitNameList();
				personNameList = getUnPersonNameList();
				logger.info("ActionShowStForPersonInUnitSubNested____unitNameList="+unitNameList);
				logger.info("ActionShowStForPersonInUnitSubNested____unUnitNameList="+unUnitNameList);
				logger.info("ActionShowStForPersonInUnitSubNested____personNameList="+personNameList);
			}
			if( check ){
				try {
					//ids = attendanceStatisticServiceAdv.listPersonForMonthByUnitYearAndMonth( unitNameList, year, month);
					ids = attendanceStatisticServiceAdv.listPersonForMonthByUnitYearMonthAndUn( unitNameList, unUnitNameList,personNameList,year, month);
					logger.info("ActionShowStForPersonInUnitSubNested____ids="+ids.size());
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceStatisticProcess(e,
							"??????????????????????????????????????????????????????????????????????????????ID?????????????????????.Name:"+unitNameList+", Year:"+year+", Month:" + month
					);
					result.error( exception );
					logger.error( e, effectivePerson, request, null);
				}
			}
			if( check ){
				if( ids != null && !ids.isEmpty() ){
					try {
						statisticPersonForMonth_list = attendanceStatisticServiceAdv.listPersonForMonth( ids );
					} catch (Exception e) {
						check = false;
						Exception exception = new ExceptionAttendanceStatisticProcess( e, "????????????ID???????????????????????????????????????????????????????????????." );
						result.error( exception );
						logger.error( e, effectivePerson, request, null);
					}
				}
			}
			
			// ??????????????????EXCEL		
			if( check ) {
				if(StringUtils.isNotEmpty(name) && StringUtils.contains(name,"@")){
					fileName = "" + name.split("@")[0] + "??????????????????????????????_"+year+"???"+month+"???.xls";
				}else{
					fileName = "" + name + "??????????????????????????????_"+year+"???"+month+"???.xls";
				}
				sheetName = "???????????????????????????";
				wb = composeDetail( fileName, sheetName, statisticPersonForMonth_list );
			}
			
			//??????????????????
			if( check ) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
				    wb.write(bos);
				    wo = new Wo(bos.toByteArray(), 
							this.contentType(stream, fileName), 
							this.contentDisposition(stream, fileName));
				} finally {
				    bos.close();
				}
			}		
			result.setData(wo);
			return result;
		}

		private Workbook composeDetail(String fileName, String sheetName, List<StatisticPersonForMonth> statisticPersonForMonth_list) throws Exception {
			Workbook wb = new HSSFWorkbook();
			Row row = null;
			if (ListTools.isNotEmpty(statisticPersonForMonth_list)) {
				// ??????????????????
				Sheet sheet = wb.createSheet(sheetName);
				// ???????????????
				row = sheet.createRow(0);
				row.createCell(0).setCellValue("??????????????????");
				row.createCell(1).setCellValue("????????????");
				row.createCell(2).setCellValue("??????");
				row.createCell(3).setCellValue("??????");
				row.createCell(4).setCellValue("??????????????????");
				row.createCell(5).setCellValue("??????????????????");
				row.createCell(6).setCellValue("???????????????");
				row.createCell(7).setCellValue("??????????????????????????????");
				row.createCell(8).setCellValue("???????????????");
				row.createCell(9).setCellValue("????????????");
				row.createCell(10).setCellValue("??????????????????");
				row.createCell(11).setCellValue("??????????????????");

				logger.info("?????????"+statisticPersonForMonth_list.size()+"??????????????????????????????");
				for (int i = 0; i < statisticPersonForMonth_list.size(); i++) {
					StatisticPersonForMonth statisticPersonForMonth = null;
					statisticPersonForMonth = statisticPersonForMonth_list.get(i);
					if( statisticPersonForMonth != null ){
						row = sheet.createRow(i + 1);
						String topUnitName = statisticPersonForMonth.getTopUnitName();
						String unitName = statisticPersonForMonth.getUnitName();
						String empName = statisticPersonForMonth.getEmployeeName();
						if(StringUtils.isNotEmpty(topUnitName) && StringUtils.contains(topUnitName,"@")){
							topUnitName = topUnitName.split("@")[0];
						}
						if(StringUtils.isNotEmpty(unitName) && StringUtils.contains(unitName,"@")){
							unitName = unitName.split("@")[0];
						}
						if(StringUtils.isNotEmpty(empName) && StringUtils.contains(empName,"@")){
							empName = empName.split("@")[0];
						}
						row.createCell(0).setCellValue(topUnitName);
						row.createCell(1).setCellValue(unitName);
						row.createCell(2).setCellValue(empName);
						row.createCell(3).setCellValue(statisticPersonForMonth.getStatisticYear()+"-"+statisticPersonForMonth.getStatisticMonth());
						row.createCell(4).setCellValue(statisticPersonForMonth.getOnDutyTimes());
						row.createCell(5).setCellValue(statisticPersonForMonth.getOnDutyTimes());
						row.createCell(6).setCellValue(statisticPersonForMonth.getOnDutyDayCount());
						row.createCell(7).setCellValue(statisticPersonForMonth.getOnSelfHolidayCount());
						row.createCell(8).setCellValue(statisticPersonForMonth.getAbsenceDayCount());
						row.createCell(9).setCellValue(statisticPersonForMonth.getLateTimes());
						row.createCell(10).setCellValue(statisticPersonForMonth.getLackOfTimeCount());
						row.createCell(11).setCellValue(statisticPersonForMonth.getAbNormalDutyCount());
					}
				}

			}
			return wb;
		}


		public static class Wo extends WoFile {
			public Wo(byte[] bytes, String contentType, String contentDisposition) {
				super(bytes, contentType, contentDisposition);
			}
		}
	}
