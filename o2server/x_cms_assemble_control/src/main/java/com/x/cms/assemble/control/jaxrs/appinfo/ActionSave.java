package com.x.cms.assemble.control.jaxrs.appinfo;

import com.google.gson.JsonElement;
import com.x.base.core.project.annotation.AuditLog;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.config.Token;
import com.x.base.core.project.exception.ExceptionAccessDenied;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.tools.ListTools;
import com.x.cms.assemble.control.Business;
import com.x.cms.assemble.control.service.CmsBatchOperationPersistService;
import com.x.cms.assemble.control.service.CmsBatchOperationProcessService;
import com.x.cms.assemble.control.service.LogService;
import com.x.cms.core.entity.AppInfo;
import com.x.cms.core.entity.element.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class ActionSave extends BaseAction {

	private static  Logger logger = LoggerFactory.getLogger(ActionSave.class);

	protected ActionResult<Wo> execute(HttpServletRequest request, EffectivePerson effectivePerson, JsonElement jsonElement ) throws Exception {
		ActionResult<Wo> result = new ActionResult<>();
		AppInfo old_appInfo = null;
		AppInfo appInfo = null;
		List<String> ids = null;
		String identityName = null;
		String unitName = null;
		String topUnitName = null;
		Boolean check = true;

		Business business = new Business(null);
		if (!business.isManager( effectivePerson)) {
			throw new ExceptionAccessDenied(effectivePerson);
		}

		Wi wi = this.convertToWrapIn( jsonElement, Wi.class );
		identityName = wi.getIdentity();

		if ( StringUtils.isEmpty( wi.getAppName() ) ) {
			throw new ExceptionAppInfoNameEmpty();
		}

		try {
			ids = appInfoServiceAdv.listByAppName( wi.getAppName());
			if ( ListTools.isNotEmpty( ids ) ) {
				for( String _id : ids ) {
					if( !_id.equalsIgnoreCase( wi.getId() )) {
						check = false;
						Exception exception = new ExceptionAppInfoNameAlreadyExists( wi.getAppName());
						result.error(exception);
					}
				}
			}
		} catch (Exception e) {
			check = false;
			Exception exception = new ExceptionAppInfoProcess(e, "??????????????????????????????????????????????????????????????????????????????AppName:" + wi.getAppName());
			result.error(exception);
			logger.error(e, effectivePerson, request, null);
		}

		if (check) {
			if ( !Token.defaultInitialManager.equalsIgnoreCase( effectivePerson.getDistinguishedName()) ) {
				try {
					identityName = userManagerService.getPersonIdentity( effectivePerson.getDistinguishedName(), identityName );
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionAppInfoProcess( e, "???????????????????????????????????????????????????????????????" + identityName );
					result.error(exception);
					logger.error(e, effectivePerson, request, null);
				}
			}else {
				identityName = Token.defaultInitialManager;
				unitName = Token.defaultInitialManager;
				topUnitName = Token.defaultInitialManager;
			}
		}

		if (check && !Token.defaultInitialManager.equals(identityName)) {
			try {
				unitName = userManagerService.getUnitNameByIdentity( identityName );
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionAppInfoProcess(e, "???????????????????????????????????????????????????????????????????????????Identity:" + identityName);
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}
		if (check && !Token.defaultInitialManager.equals(identityName)) {
			try {
				topUnitName = userManagerService.getTopUnitNameByIdentity( identityName );
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionAppInfoProcess(e, "?????????????????????????????????????????????????????????????????????????????????Identity:" + identityName);
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}
		if (check) {
			if( StringUtils.isEmpty( wi.getDocumentType() ) ) {
				wi.setDocumentType( "??????" );
			}else {
				if( !"??????".equals(wi.getDocumentType()) && !"??????".equals( wi.getDocumentType() )) {
					wi.setDocumentType( "??????" );
				}
			}
		}

		if (check) {//?????????????????????
			if( StringUtils.isEmpty( wi.getId() )) {
				wi.setId( AppInfo.createId() );
			}
			try {
				old_appInfo = appInfoServiceAdv.get( wi.getId() );
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionAppInfoProcess(e, "????????????????????????ID????????????????????????????????????????????????ID:" + wi.getId());
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}

		if (check) {
			wi.setCreatorIdentity(identityName);
			wi.setCreatorPerson(effectivePerson.getDistinguishedName());
			wi.setCreatorUnitName( unitName );
			wi.setCreatorTopUnitName( topUnitName );

			if( StringUtils.equals( "??????", wi.getDocumentType() ) && wi.getSendNotify() == null ) {
				wi.setSendNotify( true );
			}

			try {
				appInfo = appInfoServiceAdv.save( wi, wi.getConfig(), effectivePerson );
				Wo wo = new Wo();
				wo.setId( appInfo.getId() );
				result.setData( wo );

				if( old_appInfo != null ) {
					if( !old_appInfo.getAppName().equalsIgnoreCase( appInfo.getAppName() ) ||
						 !old_appInfo.getAppAlias().equalsIgnoreCase( appInfo.getAppAlias() )	) {
						//???????????????????????????????????????????????????????????????????????????????????????????????????
						new CmsBatchOperationPersistService().addOperation(
								CmsBatchOperationProcessService.OPT_OBJ_APPINFO,
								CmsBatchOperationProcessService.OPT_TYPE_UPDATENAME,  appInfo.getId(), old_appInfo.getAppName(), "?????????????????????ID=" + appInfo.getId() );
					}
					if(  permissionQueryService.hasDiffrentViewPermissionInAppInfo( old_appInfo, appInfo )) {
							//???????????????????????????????????????????????????????????????????????????????????????????????????
							new CmsBatchOperationPersistService().addOperation(
									CmsBatchOperationProcessService.OPT_OBJ_APPINFO,
									CmsBatchOperationProcessService.OPT_TYPE_PERMISSION,  appInfo.getId(), appInfo.getAppName(), "???????????????????????????ID=" + appInfo.getId() );
					}
					new LogService().log(null, effectivePerson.getDistinguishedName(), appInfo.getAppName(), appInfo.getId(), "", "", "", "APPINFO", "??????");
				}else {
					new LogService().log(null, effectivePerson.getDistinguishedName(), appInfo.getAppName(), appInfo.getId(), "", "", "", "APPINFO", "??????");
				}

				// ????????????
				CacheManager.notify(AppInfo.class);
				CacheManager.notify(AppDict.class);
				CacheManager.notify(AppDictItem.class);
				CacheManager.notify(View.class);
				CacheManager.notify(ViewCategory.class);
				CacheManager.notify(ViewFieldConfig.class);
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionAppInfoProcess(e, "??????????????????????????????????????????");
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}
		return result;
	}

	public static class Wi extends AppInfo {

		private static final long serialVersionUID = -6314932919066148113L;

		@FieldDescribe("??????????????????????????????????????????")
		private String identity = null;

		@FieldDescribe("???????????????????????????JSON")
		private String config = "{}";

		public String getConfig() { return this.config; }
		public void setConfig(final String config) { this.config = config; }
		public String getIdentity() {
			return identity;
		}
		public void setIdentity(String identity) {
			this.identity = identity;
		}
		public static WrapCopier<Wi, AppInfo> copier = WrapCopierFactory.wi( Wi.class, AppInfo.class, null, null );
	}

	public static class Wo extends WoId {

	}

}
