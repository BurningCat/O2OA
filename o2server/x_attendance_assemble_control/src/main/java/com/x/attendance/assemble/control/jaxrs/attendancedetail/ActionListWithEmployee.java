package com.x.attendance.assemble.control.jaxrs.attendancedetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.x.attendance.assemble.control.jaxrs.attendanceschedulesetting.ActionGet;
import com.x.attendance.entity.AttendanceAppealAuditInfo;
import com.x.attendance.entity.AttendanceAppealInfo;
import com.x.attendance.entity.AttendanceScheduleSetting;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.tools.ListTools;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.attendance.assemble.common.date.DateOperation;
import com.x.attendance.assemble.control.ExceptionWrapInConvert;
import com.x.attendance.entity.AttendanceDetail;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;

public class ActionListWithEmployee extends BaseAction {

	private static  Logger logger = LoggerFactory.getLogger(ActionListWithEmployee.class);

	protected ActionResult<List<Wo>> execute(HttpServletRequest request, EffectivePerson effectivePerson,
			JsonElement jsonElement) throws Exception {
		ActionResult<List<Wo>> result = new ActionResult<>();
		EffectivePerson currentPerson = this.effectivePerson(request);
		List<Wo> wraps = new ArrayList<>();
		String q_empName = null;
		String q_year = null;
		String q_month = null;
		String cycleYear = null;
		String cycleMonth = null;
		List<String> ids = null;
		List<AttendanceDetail> attendanceDetailList = null;
		Date maxRecordDate = null;
		String maxRecordDateString = null;
		DateOperation dateOperation = new DateOperation();
		AttendanceScheduleSetting scheduleSetting = null;
		Wi wrapIn = null;
		Boolean check = true;

		try {
			wrapIn = this.convertToWrapIn(jsonElement, Wi.class);
		} catch (Exception e) {
			check = false;
			Exception exception = new ExceptionWrapInConvert(e, jsonElement);
			result.error(exception);
			logger.error(e, currentPerson, request, null);
		}

		if (check) {
			if (wrapIn == null) {
				wrapIn = new Wi();
			}
			q_empName = wrapIn.getQ_empName();
			q_year = wrapIn.getQ_year();
			q_month = wrapIn.getQ_month();
			cycleYear = wrapIn.getCycleYear();
			cycleMonth = wrapIn.getCycleMonth();
			if (q_empName == null || q_empName.isEmpty()) {
				q_empName = currentPerson.getDistinguishedName();
			}
		}
		if (check) {
			try {
				maxRecordDateString = attendanceDetailServiceAdv.getMaxRecordDate();
				maxRecordDate = dateOperation.getDateFromString(maxRecordDateString);
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionAttendanceDetailProcess(e, "????????????????????????????????????????????????????????????.");
				result.error(exception);
				logger.error(e, currentPerson, request, null);
			}
		}
		if (check) {
			if (q_year == null || q_year.isEmpty()) {
				q_year = dateOperation.getYear(maxRecordDate);
			}
			if (q_month == null || q_month.isEmpty()) {
				q_month = dateOperation.getMonth(maxRecordDate);
			}
		}

		if (check) {
			if ( StringUtils.isNotEmpty( cycleYear ) && StringUtils.isNotEmpty( cycleMonth )) {
				try {
					ids = attendanceDetailServiceAdv.listUserAttendanceDetailByCycleYearAndMonth(q_empName, cycleYear,
							cycleMonth);
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceDetailProcess(e, "??????????????????????????????????????????????????????????????????ID????????????????????????"
							+ "Name:" + q_empName + ", Year:" + cycleYear + ", Month:" + cycleMonth);
					result.error(exception);
					logger.error(e, currentPerson, request, null);
				}
			} else if ( StringUtils.isNotEmpty( q_year ) && StringUtils.isNotEmpty( q_month )) {
				try {
					ids = attendanceDetailServiceAdv.listUserAttendanceDetailByYearAndMonth(q_empName, q_year, q_month);
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceDetailProcess(e, "??????????????????????????????????????????????????????????????????ID????????????????????????"
							+ "Name:" + q_empName + ", Year:" + cycleYear + ", Month:" + cycleMonth);
					result.error(exception);
					logger.error(e, currentPerson, request, null);
				}
			}
		}
		if (check) {
			if (ids != null && !ids.isEmpty()) {
				try {
					attendanceDetailList = attendanceDetailServiceAdv.list(ids);
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceDetailProcess(e,
							"??????????????????????????????????????????????????????????????????????????????ID????????????????????????");
					result.error(exception);
					logger.error(e, currentPerson, request, null);
				}
			}
		}
		if (check) {
			if (attendanceDetailList != null) {
				try {
					wraps = Wo.copier.copy(attendanceDetailList);
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAttendanceDetailProcess(e, "???????????????????????????????????????????????????????????????.");
					result.error(exception);
					logger.error(e, currentPerson, request, null);
				}
			}
		}

		if (check && ListTools.isNotEmpty( wraps )) {
			scheduleSetting = attendanceScheduleSettingServiceAdv.getAttendanceScheduleSettingWithPerson( q_empName, effectivePerson.getDebugger() );

			Integer signProxy = 1;
			List<AttendanceAppealInfo> appealInfos = null;
			AttendanceAppealAuditInfo appealAuditInfo = null;
			List<WoAttendanceAppealInfo> woAppealInfos = null;
			for( Wo detail : wraps ){
				if ( scheduleSetting != null ) {
					signProxy = scheduleSetting.getSignProxy();
				}
				detail.setSignProxy( signProxy );

				//???????????????????????????
				if( detail.getAppealStatus() != 0 ){
					//??????????????????????????????????????????????????????
					appealInfos = attendanceAppealInfoServiceAdv.listWithDetailId( detail.getId() );
					if(ListTools.isNotEmpty( appealInfos ) ){
						woAppealInfos = WoAttendanceAppealInfo.copier.copy( appealInfos );
					}
					if(ListTools.isNotEmpty( woAppealInfos ) ){
						for( WoAttendanceAppealInfo woAppealInfo : woAppealInfos ){
							appealAuditInfo = attendanceAppealInfoServiceAdv.getAppealAuditInfo( woAppealInfo.getId() );
							if( appealAuditInfo != null ){
								woAppealInfo.setAppealAuditInfo( WoAttendanceAppealAuditInfo.copier.copy( appealAuditInfo ));
							}
						}
					}
					detail.setAppealInfos(woAppealInfos);
				}
			}
		}

		result.setData(wraps);
		return result;
	}

	public static class Wi extends WrapInFilter {
	}

	public static class Wo extends AttendanceDetail {

		private static final long serialVersionUID = -5076990764713538973L;

		@FieldDescribe("??????????????????????????????????????????1-????????????????????????????????????????????? 2-????????????????????????????????????????????????????????????????????? 3-???????????????????????????????????????????????????")
		private Integer signProxy = 1;

		@FieldDescribe("??????????????????")
		private List<WoAttendanceAppealInfo> appealInfos = null;

		public List<WoAttendanceAppealInfo> getAppealInfos() { return appealInfos; }

		public void setAppealInfos(List<WoAttendanceAppealInfo> appealInfos) { this.appealInfos = appealInfos; }

		public Integer getSignProxy() {
			return signProxy;
		}

		public void setSignProxy(Integer signProxy) {
			this.signProxy = signProxy;
		}

		public static WrapCopier<AttendanceDetail, Wo> copier = WrapCopierFactory.wo(AttendanceDetail.class, Wo.class,
				null, JpaObject.FieldsInvisible);
	}
}