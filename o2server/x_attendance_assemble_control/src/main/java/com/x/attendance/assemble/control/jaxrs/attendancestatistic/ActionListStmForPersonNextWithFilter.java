package com.x.attendance.assemble.control.jaxrs.attendancestatistic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.attendance.assemble.control.Business;
import com.x.attendance.assemble.control.ExceptionWrapInConvert;
import com.x.attendance.entity.StatisticPersonForMonth;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;

public class ActionListStmForPersonNextWithFilter extends BaseAction {
	
	private static  Logger logger = LoggerFactory.getLogger( ActionListStmForPersonNextWithFilter.class );
	
	protected ActionResult<List<Wo>> execute( HttpServletRequest request, EffectivePerson effectivePerson, String id, Integer count, JsonElement jsonElement ) throws Exception {
		ActionResult<List<Wo>> result = new ActionResult<>();
		List<Wo> wraps = null;
		EffectivePerson currentPerson = this.effectivePerson(request);
		long total = 0;
		List<StatisticPersonForMonth> statisticList = null;
		WrapInFilterStatisticPersonForMonth wrapIn = null;
		Boolean check = true;
		
		try {
			wrapIn = this.convertToWrapIn( jsonElement, WrapInFilterStatisticPersonForMonth.class );
		} catch (Exception e ) {
			check = false;
			Exception exception = new ExceptionWrapInConvert( e, jsonElement );
			result.error( exception );
			logger.error( e, currentPerson, request, null);
		}
		if(check ){
			try {
				EntityManagerContainer emc = EntityManagerContainerFactory.instance().create();
				Business business = new Business(emc);

				// ?????????ID??????????????????sequence
				Object sequence = null;
				if (id == null || "(0)".equals(id) || id.isEmpty()) {
				} else {
					if (!StringUtils.equalsIgnoreCase(id, StandardJaxrsAction.EMPTY_SYMBOL)) {
						sequence = PropertyUtils.getProperty(
								emc.find(id, StatisticPersonForMonth.class ),  JpaObject.sequence_FIELDNAME);
					}
				}

				//????????????????????????????????????????????????????????????
				List<String> unitNameList = getUnitNameList(wrapIn.getTopUnitName(), wrapIn.getUnitName(), effectivePerson.getDebugger() );			
				wrapIn.setUnitName(unitNameList);
				// ??????????????????????????????????????????????????????
				statisticList = business.getStatisticPersonForMonthFactory().listIdsNextWithFilter(id, count, sequence, wrapIn);

				// ????????????????????????????????????????????????
				total = business.getStatisticPersonForMonthFactory().getCountWithFilter(wrapIn);

				// ??????????????????????????????????????????????????????????????????????????????????????????
				wraps = Wo.copier.copy(statisticList);
			} catch (Throwable th) {
				th.printStackTrace();
				result.error(th);
			}
		}
		result.setCount(total);
		result.setData(wraps);
		return result;
	}

	public static class Wo extends StatisticPersonForMonth  {
		
		private static final long serialVersionUID = -5076990764713538973L;		
		
		public static WrapCopier<StatisticPersonForMonth, Wo> copier = 
				WrapCopierFactory.wo( StatisticPersonForMonth.class, Wo.class, null,JpaObject.FieldsInvisible);
	}
}