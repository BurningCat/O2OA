package com.x.bbs.assemble.control.jaxrs.replyinfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.x.base.core.project.cache.CacheManager;
import com.x.bbs.assemble.control.ThisApplication;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionForumInfoNotExists;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionForumInsufficientPermission;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionForumPermissionsCheck;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionReplyContentEmpty;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionReplyInfoProcess;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionReplySubjectIdEmpty;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionSectionInsufficientPermissions;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionSectionNotExists;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionSectionPermissionsCheck;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionSubjectLocked;
import com.x.bbs.assemble.control.jaxrs.replyinfo.exception.ExceptionSubjectNotExists;
import com.x.bbs.entity.BBSForumInfo;
import com.x.bbs.entity.BBSReplyInfo;
import com.x.bbs.entity.BBSSectionInfo;
import com.x.bbs.entity.BBSSubjectInfo;

public class ActionSave extends BaseAction {

	private static  Logger logger = LoggerFactory.getLogger(ActionSave.class);

	protected ActionResult<Wo> execute(HttpServletRequest request, EffectivePerson effectivePerson,
			JsonElement jsonElement) throws Exception {
		ActionResult<Wo> result = new ActionResult<>();
		BBSSubjectInfo subjectInfo = null;
		BBSReplyInfo replyInfo = null;
		BBSSectionInfo sectionInfo = null;
		BBSForumInfo forumInfo = null;
		Boolean hasPermission = false;
		Boolean check = true;
		String hostIp = request.getRemoteAddr();
		String hostName = request.getRemoteAddr();
		Wi wrapIn = null;

		try {
			wrapIn = this.convertToWrapIn(jsonElement, Wi.class);
		} catch (Exception e) {
			check = false;
			Exception exception = new ExceptionReplyInfoProcess(e,
					"????????????JSON???????????????????????????????????????JSON:" + jsonElement.toString());
			result.error(exception);
			logger.error(e, effectivePerson, request, null);
		}

		if (check) {
			wrapIn.setHostIp(request.getRemoteHost());
			if (wrapIn.getSubjectId() == null) {
				check = false;
				Exception exception = new ExceptionReplySubjectIdEmpty();
				result.error(exception);
			}
		}
		if (check) {
			if (wrapIn.getContent() == null) {
				check = false;
				Exception exception = new ExceptionReplyContentEmpty();
				result.error(exception);
			}
		}
		// ???????????????????????????????????????
		if (check) {
			try {
				subjectInfo = subjectInfoService.get(wrapIn.getSubjectId());
				if (subjectInfo == null) {
					check = false;
					Exception exception = new ExceptionSubjectNotExists(wrapIn.getSubjectId());
					result.error(exception);
				}
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionReplyInfoProcess(e, "????????????ID?????????????????????????????????.ID:" + wrapIn.getSubjectId());
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}

		// ????????????????????????????????????????????????????????????????????????????????????
		if (check) {
			if ("?????????".equals(subjectInfo.getSubjectStatus())) {
				check = false;
				Exception exception = new ExceptionSubjectLocked(wrapIn.getSubjectId());
				result.error(exception);
			}
		}

		// ???????????????????????????????????????????????????????????????????????????????????????????????????
		if (check) {
			try {
				sectionInfo = sectionInfoServiceAdv.get(subjectInfo.getSectionId());
				if (sectionInfo == null) {
					check = false;
					Exception exception = new ExceptionSectionNotExists(subjectInfo.getSectionId());
					result.error(exception);
				}
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionReplyInfoProcess(e,
						"????????????ID?????????????????????????????????.ID:" + subjectInfo.getSectionId());
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}

		// ???????????????????????????????????????????????????????????????
		if (check) {
			if ("????????????".equals(sectionInfo.getReplyPublishAble())) {
				try {
					hasPermission = UserPermissionService.hasPermission(effectivePerson.getDistinguishedName(),
							"SECTION_REPLY_PUBLISH_" + subjectInfo.getSectionId());
					if (!hasPermission) {
						check = false;
						Exception exception = new ExceptionSectionInsufficientPermissions(sectionInfo.getSectionName(),
								"SECTION_REPLY_PUBLISH");
						result.error(exception);
					}
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionSectionPermissionsCheck(e,
							effectivePerson.getDistinguishedName(), sectionInfo.getSectionName(),
							"SECTION_REPLY_PUBLISH");
					result.error(exception);
					logger.error(e, effectivePerson, request, null);
				}
			}
		}
		// ????????????????????????????????????????????????
		if ( subjectInfo != null && !subjectInfo.getMainSectionId().equals(subjectInfo.getSectionId())) {
			if (check) {
				try {
					sectionInfo = sectionInfoServiceAdv.get(subjectInfo.getMainSectionId());
					if (sectionInfo == null) {
						check = false;
						Exception exception = new ExceptionSectionNotExists(subjectInfo.getMainSectionId());
						result.error(exception);
					}
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionReplyInfoProcess(e,
							"????????????ID?????????????????????????????????.ID:" + subjectInfo.getMainSectionId());
					result.error(exception);
					logger.error(e, effectivePerson, request, null);
				}
			}
			if (check) {
				if ("????????????".equals(sectionInfo.getReplyPublishAble())) {
					// ????????????????????????????????????????????????????????????????????????
					try {
						hasPermission = UserPermissionService.hasPermission(effectivePerson.getDistinguishedName(),
								"SECTION_REPLY_PUBLISH_" + subjectInfo.getMainSectionId());
						if (!hasPermission) {
							check = false;
							Exception exception = new ExceptionSectionInsufficientPermissions( sectionInfo.getSectionName(), "SECTION_REPLY_PUBLISH");
							result.error(exception);
						}
					} catch (Exception e) {
						check = false;
						Exception exception = new ExceptionSectionPermissionsCheck(e,
								effectivePerson.getDistinguishedName(), sectionInfo.getSectionName(), "SECTION_REPLY_PUBLISH");
						result.error(exception);
						logger.error(e, effectivePerson, request, null);
					}
				}
			}
		}
		// ???????????????????????????????????????????????????????????????????????????????????????????????????
		if (check) {
			try {
				forumInfo = forumInfoServiceAdv.get(subjectInfo.getForumId());
				if (forumInfo == null) {
					check = false;
					Exception exception = new ExceptionForumInfoNotExists(subjectInfo.getForumId());
					result.error(exception);
				}
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionReplyInfoProcess(e,
						"???????????????ID??????BBS????????????????????????????????????ID:" + subjectInfo.getForumId());
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}
		if (check) {
			if ("????????????".equals(forumInfo.getReplyPublishAble())) {
				// ??????????????????????????????????????????????????????????????????????????????
				try {
					hasPermission = UserPermissionService.hasPermission(effectivePerson.getDistinguishedName(),
							"FORUM_REPLY_PUBLISH_" + subjectInfo.getForumId());
					if (!hasPermission) {
						check = false;
						Exception exception = new ExceptionForumInsufficientPermission(subjectInfo.getForumName(),
								"FORUM_REPLY_PUBLISH");
						result.error(exception);
					}
				} catch (Exception e) {
					check = false;
					Exception exception = new ExceptionForumPermissionsCheck(e, effectivePerson.getDistinguishedName(), subjectInfo.getForumName(), "FORUM_REPLY_PUBLISH");
					result.error(exception);
					logger.error(e, effectivePerson, request, null);
				}
			}
		}

		// ?????????????????????????????????????????????
		if (check) {
			wrapIn.setForumId(subjectInfo.getForumId());
			wrapIn.setForumName(subjectInfo.getForumName());
			wrapIn.setMainSectionId(subjectInfo.getMainSectionId());
			wrapIn.setMainSectionName(subjectInfo.getMainSectionName());
			wrapIn.setSectionId(subjectInfo.getSectionId());
			wrapIn.setSectionName(subjectInfo.getSectionName());
			wrapIn.setCreatorName(effectivePerson.getDistinguishedName());
		}

		if (check) {
			if ( wrapIn.getTitle() == null || wrapIn.getTitle().isEmpty()) {
				if ( StringUtils.isNotEmpty( subjectInfo.getTitle() )) {
					wrapIn.setTitle(subjectInfo.getTitle());
				} else {
					wrapIn.setTitle("?????????");
				}
			}
		}
		if (check) {
			try {
				replyInfo = Wi.copier.copy(wrapIn);
				if ( StringUtils.isNotEmpty( wrapIn.getId() )) {
					replyInfo.setId(wrapIn.getId());
				}
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionReplyInfoProcess(e, "???????????????????????????????????????????????????????????????????????????");
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}
		if (check) {
			try {
				replyInfo.setMachineName(wrapIn.getReplyMachineName());
				replyInfo.setSystemType(wrapIn.getReplySystemName());
				replyInfo = replyInfoService.save(replyInfo);

				Wo wo = new Wo();
				wo.setId(replyInfo.getId());
				result.setData(wo);

				CacheManager.notify( BBSReplyInfo.class );
				CacheManager.notify( BBSForumInfo.class );
				CacheManager.notify( BBSSectionInfo.class );
				CacheManager.notify( BBSSubjectInfo.class );
				
				operationRecordService.replyOperation(effectivePerson.getDistinguishedName(), replyInfo, "CREATE", hostIp, hostName);
			} catch (Exception e) {
				check = false;
				Exception exception = new ExceptionReplyInfoProcess(e, "?????????????????????????????????????????????");
				result.error(exception);
				logger.error(e, effectivePerson, request, null);
			}
		}

		if ( check ) {
			//????????????(Forum)?????????(Selection)???????????????(MainSelection)???????????????????????????????????????????????????????????????????????????
			try {
				ThisApplication.queueNewReplyNotify.send( replyInfo );
			} catch (Exception e) {
				logger.error(e, effectivePerson, request, null);
			}

		}
		return result;
	}

	public static class Wi extends BBSReplyInfo {

		private static final long serialVersionUID = -5076990764713538973L;

		public static List<String> Excludes = new ArrayList<String>();

		public static WrapCopier<Wi, BBSReplyInfo> copier = WrapCopierFactory.wi(Wi.class, BBSReplyInfo.class, null,
				JpaObject.FieldsUnmodify);

		private String replyMachineName = "PC";

		private String replySystemName = "Windows";

		private String userHostIp = "";

		public String getReplyMachineName() {
			return replyMachineName;
		}

		public void setReplyMachineName(String replyMachineName) {
			this.replyMachineName = replyMachineName;
		}

		public String getReplySystemName() {
			return replySystemName;
		}

		public void setReplySystemName(String replySystemName) {
			this.replySystemName = replySystemName;
		}

		public String getUserHostIp() {
			return userHostIp;
		}

		public void setUserHostIp(String userHostIp) {
			this.userHostIp = userHostIp;
		}

	}

	public static class Wo extends WoId {

	}
}