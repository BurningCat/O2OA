package com.x.attendance.assemble.control.processor.thread;

import java.util.Date;
import java.util.List;

import com.x.attendance.assemble.common.date.DateOperation;
import com.x.attendance.assemble.control.processor.EntityImportDataDetail;
import com.x.attendance.assemble.control.processor.ImportOptDefine;
import com.x.attendance.assemble.control.processor.monitor.StatusImportFileDetail;
import com.x.attendance.assemble.control.processor.monitor.StatusSystemImportOpt;
import com.x.attendance.assemble.control.service.UserManagerService;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;

public class OperatorDataValidator implements Runnable {

	private static  Logger logger = LoggerFactory.getLogger( OperatorDataValidator.class );
	
	private UserManagerService userManagerService = null;
	
	private EntityImportDataDetail cacheImportRowDetail = null;
	private Boolean debugger = false;
	
	public OperatorDataValidator( EntityImportDataDetail cacheImportRowDetail, Boolean debugger ) {
		userManagerService = new UserManagerService();
		this.cacheImportRowDetail = cacheImportRowDetail ;
		this.debugger = debugger;
	}
	
	@Override
	public void run() {
		 execute( cacheImportRowDetail );
	}
	
	private void execute( EntityImportDataDetail cacheImportRowDetail ) {
		Integer curRow = cacheImportRowDetail.getCurRow();
		List<String> colmlist = cacheImportRowDetail.getColmlist();
		if( colmlist == null ) {
			return;
		}
		if( colmlist != null && !colmlist.get(0).isEmpty() && !colmlist.get(2).isEmpty()){
			try {
				check( cacheImportRowDetail.getFile_id(), curRow, colmlist );
			}catch( Exception e ) {
				logger.error( e );
			}
		}
	}
	
	private void check( String fileKey, Integer curRow, List<String> colmlist ) {

		StatusImportFileDetail cacheImportFileStatus = StatusSystemImportOpt.getInstance().getCacheImportFileStatus( fileKey );
		
		cacheImportFileStatus.setCurrentProcessName( ImportOptDefine.VALIDATE );
		
		cacheImportFileStatus.setProcessing_validate( true );
		
		cacheImportFileStatus.setProcessing( true );
		
		if( colmlist!= null && colmlist.size() > 0 ){
			
			Boolean checkSuccess = true;
			Boolean personExists = true;
			Date datetime = null;
			DateOperation dateOperation = new DateOperation();
			
			EntityImportDataDetail cacheImportRowDetail = new EntityImportDataDetail();
			
			cacheImportRowDetail.setCurRow( curRow );
			
			if( !colmlist.get(0).isEmpty() && !colmlist.get(2).isEmpty() ){
				
				cacheImportRowDetail.setEmployeeName( colmlist.get(0).trim() );  //????????????
				
				if( colmlist.get(1) != null && !colmlist.get(1).trim().isEmpty()){
					cacheImportRowDetail.setEmployeeNo( colmlist.get(1).trim() );    //?????????
				}
				
				cacheImportRowDetail.setRecordDateString( colmlist.get(2) ); //????????????
				
				if( colmlist.size() > 3 ){
					cacheImportRowDetail.setOnDutyTime( colmlist.get(3) );    //????????????????????????
				}
				if( colmlist.size() > 4 ){
					cacheImportRowDetail.setMorningOffDutyTime( colmlist.get(4) );    //????????????????????????
				}
				if( colmlist.size() > 5 ){
					cacheImportRowDetail.setAfternoonOnDutyTime( colmlist.get(5) );    //????????????????????????
				}
				
				if( colmlist.size() > 6 ){
					cacheImportRowDetail.setOffDutyTime( colmlist.get(6) );   //????????????????????????
				}
				
				cacheImportRowDetail.setCheckStatus( "success" );         //?????????????????????????????????
				
				if( checkSuccess ) {
					//????????????????????????
					personExists = checkPersonExists( cacheImportFileStatus, cacheImportRowDetail.getEmployeeName() );
					if( !personExists ) {
						checkSuccess = false;
						cacheImportRowDetail.setCheckStatus("error");
						cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + ", ??????????????????" );
						logger.info("step 2, data check on row "+curRow+", found an error! person '"+ cacheImportRowDetail.getEmployeeName() +"' not exists." );
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getEmployeeName() == null || cacheImportRowDetail.getEmployeeName().isEmpty() ) {
						checkSuccess = false;
						cacheImportRowDetail.setCheckStatus("error");
						cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + ", '????????????'?????????" );
						logger.info("step 2, data check on row "+curRow+", found an error! field 'employeeName' is null." );
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getRecordDateString() == null || cacheImportRowDetail.getRecordDateString().isEmpty() ) {
						checkSuccess = false;
						cacheImportRowDetail.setCheckStatus("error");
						cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + ", '????????????'?????????" );
						logger.info("step 2, data check on row "+curRow+", found an error! field 'recordDateString' is null." );
					}
				}
				
				if( checkSuccess ) {
					//????????????????????????????????????????????????????????????????????????
					try{
						datetime = dateOperation.getDateFromString( cacheImportRowDetail.getRecordDateString() );
						cacheImportRowDetail.setRecordDate( datetime );
						cacheImportRowDetail.setRecordDateStringFormated( dateOperation.getDateStringFromDate( datetime, "YYYY-MM-DD") ); //????????????
						cacheImportRowDetail.setRecordYearString( dateOperation.getYear( datetime ) );
						cacheImportRowDetail.setRecordMonthString( dateOperation.getMonth(datetime) );
						if( Integer.parseInt( cacheImportRowDetail.getRecordYearString() ) > dateOperation.getYearNumber( new Date() )
								|| Integer.parseInt( cacheImportRowDetail.getRecordYearString() ) < 2000 
						) {
							throw new Exception("record date error:" + cacheImportRowDetail.getRecordDateString() );
						}
						//???????????????????????????????????????????????????
						cacheImportFileStatus.sendStartTime(datetime);
						cacheImportFileStatus.sendEndTime( datetime );
					}catch( Exception e ){
						checkSuccess = false;
						cacheImportRowDetail.setCheckStatus("error");
						cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + "???????????????????????????" + cacheImportRowDetail.getRecordDateString() );
						logger.info("step 2, data check on row "+curRow+", found an error! format on field 'recordDate'???" + cacheImportRowDetail.getRecordDateString(), e);
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getOnDutyTime() != null && cacheImportRowDetail.getOnDutyTime().trim().length() > 0 ){
						try{
							datetime = dateOperation.getDateFromString( cacheImportRowDetail.getOnDutyTime() );
							cacheImportRowDetail.setOnDutyTimeFormated( dateOperation.getDateStringFromDate( datetime, "HH:mm:ss") ); //????????????????????????
						}catch( Exception e ){
							checkSuccess = false;
							cacheImportRowDetail.setCheckStatus("error");
							cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + "???????????????????????????????????????" + cacheImportRowDetail.getOnDutyTime() );
							logger.info("step 2, data check on row "+curRow+", found an error!format on field 'onDutyTime'???" + cacheImportRowDetail.getOnDutyTime(), e);
						}
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getMorningOffDutyTime() != null && cacheImportRowDetail.getMorningOffDutyTime().trim().length() > 0 ){
						try{
							datetime = dateOperation.getDateFromString( cacheImportRowDetail.getMorningOffDutyTime() );
							cacheImportRowDetail.setMorningOffDutyTimeFormated( dateOperation.getDateStringFromDate( datetime, "HH:mm:ss") ); //????????????????????????
						}catch( Exception e ){
							checkSuccess = false;
							cacheImportRowDetail.setCheckStatus("error");
							cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + "???????????????????????????????????????" + cacheImportRowDetail.getMorningOffDutyTime() );
							logger.info("step 2, data check on row "+curRow+", found an error!format on field 'onDutyTime'???" + cacheImportRowDetail.getMorningOffDutyTime(), e);
						}
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getAfternoonOnDutyTime() != null && cacheImportRowDetail.getAfternoonOnDutyTime().trim().length() > 0 ){
						try{
							datetime = dateOperation.getDateFromString( cacheImportRowDetail.getAfternoonOnDutyTime() );
							cacheImportRowDetail.setAfternoonOnDutyTimeFormated( dateOperation.getDateStringFromDate( datetime, "HH:mm:ss") ); //????????????????????????
						}catch( Exception e ){
							checkSuccess = false;
							cacheImportRowDetail.setCheckStatus("error");
							cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + "???????????????????????????????????????" + cacheImportRowDetail.getAfternoonOnDutyTime() );
							logger.info("step 2, data check on row "+curRow+", found an error!format on field 'onDutyTime'???" + cacheImportRowDetail.getAfternoonOnDutyTime(), e);
						}
					}
				}
				
				if( checkSuccess ) {
					if( cacheImportRowDetail.getOffDutyTime() != null && cacheImportRowDetail.getOffDutyTime().trim().length() > 0 ){
						try{
							datetime = dateOperation.getDateFromString( cacheImportRowDetail.getOffDutyTime() );
							cacheImportRowDetail.setOffDutyTimeFormated( dateOperation.getDateStringFromDate( datetime, "HH:mm:ss") ); //??????????????????
						}catch( Exception e ){
							checkSuccess = false;
							cacheImportRowDetail.setCheckStatus("error");
							cacheImportRowDetail.setDescription( cacheImportRowDetail.getDescription() + "???????????????????????????????????????" + cacheImportRowDetail.getOffDutyTime() );
							logger.info("step 2, data check on row "+curRow+", found an error!format on field 'offDutyTime'???" + cacheImportRowDetail.getOffDutyTime(), e);
						}
					}
				}
				
				if( !checkSuccess ){
					cacheImportFileStatus.setCheckStatus( "error" );
					cacheImportFileStatus.setCheckStatus( "error" );
					cacheImportFileStatus.increaseErrorCount( 1 );
					logger.debug( debugger, ">>>>>>>>>>record check error:" + cacheImportRowDetail.getDescription() );
					cacheImportFileStatus.addErrorList( cacheImportRowDetail );
				}
				
				cacheImportFileStatus.addDetailList( cacheImportRowDetail );
				cacheImportFileStatus.increaseProcess_validate_count( 1 );
			}
		}
	}

	private Boolean checkPersonExists( StatusImportFileDetail cacheImportFileStatus, String personName ) {
		if( !cacheImportFileStatus.getPersonList().isEmpty() && cacheImportFileStatus.getPersonList().contains( personName ) ) {
			//?????????????????????????????????????????????
			return true;
		}else {
			//????????????????????????
			try{
				personName = userManagerService.checkPersonExists( personName );
				if( personName == null || personName.isEmpty() ) {
					return false;
				}else {
					cacheImportFileStatus.addPersonList( personName );
					return true;
				}
			}catch(Exception e) {
				return false;
			}
		}
	}

	
}
