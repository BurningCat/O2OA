package com.x.cms.assemble.control.jaxrs.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.project.cache.Cache;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.StandardJaxrsAction;
import com.x.base.core.project.organization.Person;
import com.x.base.core.project.tools.ListTools;
import com.x.cms.assemble.control.Business;
import com.x.cms.assemble.control.ThisApplication;
import com.x.cms.assemble.control.service.AppInfoServiceAdv;
import com.x.cms.assemble.control.service.CategoryInfoServiceAdv;
import com.x.cms.assemble.control.service.DocCommendPersistService;
import com.x.cms.assemble.control.service.DocumentPersistService;
import com.x.cms.assemble.control.service.DocumentQueryService;
import com.x.cms.assemble.control.service.DocumentViewRecordServiceAdv;
import com.x.cms.assemble.control.service.FileInfoServiceAdv;
import com.x.cms.assemble.control.service.FormServiceAdv;
import com.x.cms.assemble.control.service.LogService;
import com.x.cms.assemble.control.service.PermissionQueryService;
import com.x.cms.assemble.control.service.QueryViewService;
import com.x.cms.assemble.control.service.UserManagerService;
import com.x.cms.core.entity.*;
import com.x.query.core.entity.Item;
import org.apache.commons.lang3.StringUtils;

public class BaseAction extends StandardJaxrsAction {

	protected Cache.CacheCategory cacheCategory = new Cache.CacheCategory(Item.class, Document.class, DocumentCommentInfo.class);

	protected LogService logService = new LogService();
	protected QueryViewService queryViewService = new QueryViewService();
	protected DocumentViewRecordServiceAdv documentViewRecordServiceAdv = new DocumentViewRecordServiceAdv();
	protected DocumentPersistService documentPersistService = new DocumentPersistService();
	protected DocumentQueryService documentQueryService = new DocumentQueryService();

	protected DocCommendPersistService docCommendPersistService = new DocCommendPersistService();

	protected FormServiceAdv formServiceAdv = new FormServiceAdv();
	protected CategoryInfoServiceAdv categoryInfoServiceAdv = new CategoryInfoServiceAdv();
	protected AppInfoServiceAdv appInfoServiceAdv = new AppInfoServiceAdv();
	protected UserManagerService userManagerService = new UserManagerService();
	protected FileInfoServiceAdv fileInfoServiceAdv = new FileInfoServiceAdv();
	protected PermissionQueryService permissionQueryService = new PermissionQueryService();

	protected boolean modifyDocStatus( String id, String stauts, String personName ) throws Exception{
		Business business = null;
		Document document = null;

		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			business = new Business(emc);

			//??????????????????????????????
			emc.beginTransaction( Document.class );
			document = business.getDocumentFactory().get(id);
			if (null != document) {
				//??????????????????
				document.setDocStatus( stauts );
				document.setPublishTime( new Date() );
				//??????????????????
				emc.check( document, CheckPersistType.all);
			}
			emc.commit();
			return true;
		} catch (Exception th) {
			throw th;
		}
	}

//	/**
//	 * ?????????????????????ID???????????????ID???????????????????????????????????????????????????????????????????????????ID
//	 * @param inFilterAppIdList
//	 * @param inFilterAppAliasList
//	 * @param inFilterCategoryIdList
//	 * @param inFilterCategoryAliasList
//	 * @param documentType
//	 * @param personName
//	 * @param isAnonymous
//	 * @param appType
//	 * @param manager
//	 * @param maxCount
//	 * @return
//	 * @throws Exception
//	 */
//	protected List<String> listAllViewAbleCategoryIds( List<String> inFilterAppIdList, List<String> inFilterAppAliasList,
//			List<String> inFilterCategoryIdList, List<String> inFilterCategoryAliasList, String documentType, String personName,
//			Boolean isAnonymous, String appType, Boolean manager, Integer maxCount ) throws Exception{
//		List<String> categoryIds = null;
//		List<AppInfo> appInfoList = null;
//		List<CategoryInfo> categoryInfoList = null;
//
//		List<String> unitNames = null;
//		List<String>  groupNames = null;
//
//		if( !isAnonymous ) {
//			unitNames = userManagerService.listUnitNamesWithPerson( personName );
//			 groupNames = userManagerService.listGroupNamesByPerson( personName );
//		}
//
//		//?????????????????????ID???????????????????????????ID??????
//		if( inFilterAppIdList == null ) {
//			inFilterAppIdList = new ArrayList<>(); //?????????
//		}
//		//??????????????????????????????????????????
//		if( ListTools.isNotEmpty( inFilterAppAliasList ) ){
//			appInfoList = appInfoServiceAdv.listAppInfoWithAliases( inFilterAppAliasList );
//			for( AppInfo appInfo : appInfoList ){
//				if( appInfoViewable( personName, isAnonymous, unitNames, groupNames, appInfo, manager )) {
//					if( !inFilterAppIdList.contains( appInfo.getId() )){
//						inFilterAppIdList.add( appInfo.getId() );
//					}
//				}
//			}
//		}
//
//		//?????????wrapIn_categoryAliasList??????wrapIn_viewAbleCategoryIds???
//		if( inFilterCategoryIdList == null ) {
//			inFilterCategoryIdList = new ArrayList<>(); //?????????
//		}
//		if( ListTools.isNotEmpty( inFilterCategoryAliasList ) ){
//			categoryInfoList = categoryInfoServiceAdv.listCategoryInfoWithAliases( inFilterCategoryAliasList );
//			for( CategoryInfo categoryInfo : categoryInfoList ){
//				if( !inFilterCategoryIdList.contains( categoryInfo.getId() )){
//					inFilterCategoryIdList.add( categoryInfo.getId() );
//				}
//			}
//		}
//
//		//??????????????????????????????????????????????????????????????????
//		if( manager ){
//			List<String> categoryIds_result = new ArrayList<>();
//			//??????????????????????????????????????????????????????????????????????????????????????????
//			if( ListTools.isEmpty( inFilterAppIdList )) {//?????????????????????
//				if( ListTools.isNotEmpty( inFilterCategoryIdList )) {
//					//????????????????????????????????????????????????????????????????????????
//					categoryIds_result = inFilterCategoryIdList;
//				}else {
//					categoryIds_result = categoryInfoServiceAdv.listAllIds();
//				}
//			}else {//?????????????????????????????????????????????????????????????????????ID??????
//				categoryIds = categoryInfoServiceAdv.listCategoryIdsWithAppIds( inFilterAppIdList, documentType, manager, maxCount );
//				for( String id : categoryIds ){
//					if( !categoryIds_result.contains( id )){
//						categoryIds_result.add( id );
//					}
//				}
//				if( ListTools.isNotEmpty( inFilterCategoryIdList )) {
//					//???????????????????????????????????????, ??????????????????????????????
//					categoryIds_result.retainAll(inFilterCategoryIdList  );
//				}
//			}
//			return categoryIds_result;
//		}else{
//			//????????????????????????????????????????????????????????????????????????
//			//????????????????????????????????????????????????
//			categoryIds = permissionQueryService.listViewableCategoryIdByPerson(
//					personName, isAnonymous, unitNames, groupNames, inFilterAppIdList, inFilterCategoryIdList, null, documentType, appType, maxCount, manager );
//			return categoryIds;
//		}
//	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 * @param personName
	 * @param isAnonymous
	 * @param unitNames
	 * @param groupNames
	 * @param appInfo
	 * @return
	 * @throws Exception
	 */
	private boolean appInfoViewable(String personName, Boolean isAnonymous, List<String> unitNames, List<String> groupNames, AppInfo appInfo, Boolean manager) throws Exception {

		if( appInfo.getAllPeopleView() || appInfo.getAllPeoplePublish() ) {
			return true;
		}
		if( !isAnonymous ) {
			if( manager ) {
				return true;
			}

			if( ListTools.isNotEmpty( appInfo.getManageablePersonList() )) {
				if( appInfo.getManageablePersonList().contains( personName )) {
					return true;
				}
			}
			if( ListTools.isNotEmpty( appInfo.getViewableUnitList() )) {
				if( ListTools.containsAny( unitNames, appInfo.getViewableUnitList())) {
					return true;
				}
			}
			if( ListTools.isNotEmpty( appInfo.getViewableGroupList() )) {
				if( ListTools.containsAny( groupNames, appInfo.getViewableGroupList())) {
					return true;
				}
			}
			if( ListTools.isNotEmpty( appInfo.getPublishableUnitList() )) {
				if( ListTools.containsAny( unitNames, appInfo.getPublishableUnitList())) {
					return true;
				}
			}
			if( ListTools.isNotEmpty( appInfo.getPublishableGroupList() )) {
				if( ListTools.containsAny( groupNames, appInfo.getPublishableGroupList())) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean hasReadPermission(Business business, Document document, List<String> unitNames, List<String> groupNames, EffectivePerson effectivePerson, String queryPerson) throws Exception{
		if("??????".equals(document.getDocumentType())){
			return true;
		}

		String personName = effectivePerson.getDistinguishedName();
		if(effectivePerson.isManager()){
			if(StringUtils.isNotEmpty(queryPerson)){
				Person person = userManagerService.getPerson(queryPerson);
				if(person!=null){
					personName = person.getDistinguishedName();
				}else{
					return false;
				}
			}else {
				return true;
			}
		}

		if(ListTools.isEmpty(document.getReadPersonList())
				&& ListTools.isEmpty(document.getReadUnitList())
				&& ListTools.isEmpty(document.getReadGroupList())){
			return true;
		}

		//???????????????
		if(ListTools.contains(document.getReadPersonList(), getShortTargetFlag(personName)) ||
				ListTools.contains(document.getReadPersonList(), "?????????")){
			return true;
		}
		if(unitNames == null){
			unitNames = userManagerService.listUnitNamesWithPerson(personName);
		}
		for(String unitName : unitNames){
			if(ListTools.contains(document.getReadUnitList(), getShortTargetFlag(unitName))){
				return true;
			}
		}
		if (groupNames == null){
			groupNames = userManagerService.listGroupNamesByPerson(personName);
		}
		for(String groupName : groupNames){
			if(ListTools.contains(document.getReadGroupList(), getShortTargetFlag(groupName))){
				return true;
			}
		}

		if(business.isHasPlatformRole(personName, ThisApplication.ROLE_CMSManager)){
			return true;
		}

		return false;
	}

	protected String getShortTargetFlag(String distinguishedName) {
		String target = null;
		if( StringUtils.isNotEmpty( distinguishedName ) ){
			String[] array = distinguishedName.split("@");
			StringBuffer sb = new StringBuffer();
			if( array.length == 3 ){
				target = sb.append(array[1]).append("@").append(array[2]).toString();
			}else if( array.length == 2 ){
				//2???
				target = sb.append(array[0]).append("@").append(array[1]).toString();
			}else{
				target = array[0];
			}
		}
		return target;
	}

	protected List<String> getShortTargetFlag(List<String> nameList) {
		List<String> targetList = new ArrayList<>();
		if( ListTools.isNotEmpty( nameList ) ){
			for(String distinguishedName : nameList) {
				String target = distinguishedName;
				String[] array = distinguishedName.split("@");
				StringBuffer sb = new StringBuffer();
				if (array.length == 3) {
					target = sb.append(array[1]).append("@").append(array[2]).toString();
				} else if (array.length == 2) {
					//2???
					target = sb.append(array[0]).append("@").append(array[1]).toString();
				} else {
					target = array[0];
				}
				targetList.add(target);
			}
		}
		return targetList;
	}



//	/**
//	 * ??????????????????????????????????????????
//	 * @param personName
//	 * @param groupNames
//	 * @param unitNames
//	 * @return
//	 */
//	protected List<String> getPermissionObjs(String personName, List<String> unitNames, List<String> groupNames) {
//		List<String> permissionObjs = new ArrayList<>();
//		permissionObjs.add( personName );
//		if( ListTools.isNotEmpty( unitNames )) {
//			for( String unitName : unitNames ) {
//				permissionObjs.add( unitName );
//			}
//		}
//		if( ListTools.isNotEmpty( groupNames )) {
//			for( String groupName : groupNames ) {
//				permissionObjs.add( groupName );
//			}
//		}
//		return permissionObjs;
//	}
}
